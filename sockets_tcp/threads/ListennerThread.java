package sockets_tcp.threads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import sockets_tcp.classes.Commands;
import sockets_tcp.classes.User;
import sockets_tcp.controllers.DirectoryController;

public class ListennerThread extends Thread {

    Socket clientSocket;
    DataInputStream in;
    DataOutputStream out;
    Commands commands;
    User[] users;
    User user;
    Path userPath;
    Path home;
    Path currentPath;
    DirectoryController dirController;

    public ListennerThread(Socket clientSocket, User[] users) throws IOException {
        this.users = users;
        this.clientSocket = clientSocket;

        this.in = new DataInputStream(clientSocket.getInputStream());
        this.out = new DataOutputStream(clientSocket.getOutputStream());

        this.commands = new Commands();
        this.dirController = new DirectoryController();
        this.home = FileSystems.getDefault().getPath("sockets_tcp/users", "");
    }

    @Override
    public void run() {

        try {

            this.awaitConnection();
            this.listenCommands();

        } catch (EOFException eofe) {
            System.out.println("EOF: " + eofe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        } catch (UnsupportedOperationException uoe) {
            System.out.println("UOE: " + uoe.getMessage());
        } catch (Exception e) {

            System.out.println("DEFAULT: " + e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException ioe) {
                System.err.println("IOE: " + ioe);
            }
        }

        System.out.println("Thread comunicação cliente finalizada.");
    }

    protected void awaitConnection() throws IOException {
        String buffer = "";
        Boolean userNoConnected = true;

        while (userNoConnected) {
            // read input buffer
            buffer = in.readUTF();
            String[] cmdParams = buffer.split(" ");

            // check CONNECT command
            if (cmdParams.length == 3 && cmdParams[0].equals(this.commands.connect)) {
                // extract params
                String username = cmdParams[1];
                String password = cmdParams[2];
                User user = this.getUserByName(username);

                // check if user
                if (user == null) {
                    out.writeUTF("User not found");
                    continue;
                }

                boolean passwordIsCorrect = this.checkUserPassword(password, user.password);

                if (passwordIsCorrect) {
                    System.out.format("User %s connected ...\n", user.user);

                    // update home path
                    this.home = this.home.resolve(user.user);

                    // set base route (/home/username)
                    this.userPath = this.home;
                    out.writeUTF(this.commands.success + " " + this.home);
                    break;
                }

                out.writeUTF("Password incorrect" + "\n");
            } else {
                out.writeUTF("Command incorrect" + "\n");
            }
        }
    }

    protected void listenCommands() throws IOException, EOFException, UnsupportedOperationException {
        boolean userConnected = true;

        while (userConnected) {
            // read input buffer
            String buffer = "";
            buffer = in.readUTF();

            // execute command received
            String[] cmdParams = buffer.split(" ");
            userConnected = this.executeCommands(buffer, cmdParams);
        }
    }

    protected boolean executeCommands(String buffer, String[] cmdParams) throws IOException {
        boolean keepConnected = true;
        String cmd = cmdParams[0];

        if (cmd.equals(this.commands.pwd)) {
            System.out.println("---> Executing PWD command ...");
            this.executePwd();
            System.out.println("<--- PWD executed ...");
            return keepConnected;
        }

        if (cmd.equals(this.commands.chdir)) {
            System.out.println("---> Executing CHDIR command ...");
            this.executedChdir(cmdParams[1]);
            System.out.println("<--- CHDIR executed ...");
            return keepConnected;
        }

        if (cmd.equals(this.commands.getFiles)) {
            System.out.println("---> Executing GETFILES command ...");
            this.executeGetFiles();
            System.out.println("<--- GETFILES executed ...");
            return keepConnected;
        }

        if (cmd.equals(this.commands.getDirs)) {
            System.out.println("---> Executing GETDIRS command ...");
            this.executeGetDirs();
            System.out.println("<--- GETDIRS executed ...");
            return keepConnected;
        }

        if (cmd.equals(this.commands.mkdir)) {
            System.out.println("---> Executing MKDIR command ...");
            this.executeMkdir(cmdParams[1]);
            System.out.println("<--- MKDIR executed ...");
            return keepConnected;
        }

        if (cmd.equals(this.commands.touch)) {
            System.out.println("---> Executing TOUCH command ...");
            this.executeTouch(cmdParams[1]);
            System.out.println("<--- TOUCH executed ...");
            return keepConnected;
        }

        if (buffer.equals(this.commands.exit)) {
            System.out.println("---> Executing EXIT command ...");
            out.writeUTF(this.commands.exit);
            System.out.println("<--- EXIT executed ...");
            return !keepConnected;
        }

        out.writeUTF(this.commands.error + " " + "Command not found" + "\n");
        return keepConnected;
    }

    protected boolean checkUserPassword(String password, String passoword2) {
        return password.equals(passoword2);
    }

    protected User getUserByName(String name) {

        for (int i = 0; i < 2; i++) {
            if (this.users[i].user.equals(name)) {
                return this.users[i];
            }
        }

        return null;
    }

    protected void executePwd() throws IOException {
        out.writeUTF(this.home.toString());
    }

    protected void executedChdir(String dirName) throws IOException {

        // back directory
        if (dirName.equals("..")) {
            // check if current directory is home
            if (this.userPath.equals(this.home)) {
                out.writeUTF(this.commands.chdir + " " + this.home);
                return;
            }

            // back directory
            this.home = this.home.getParent();
            System.out.println(this.home);

            // send response
            out.writeUTF(this.commands.chdir + " " + this.home);
            return;
        }

        // change to directory received
        Path nextDir = this.home.resolve(dirName);
        File homeFile = nextDir.toFile();
        boolean pathExist = homeFile.exists();

        // retur error if path not exist
        if (pathExist) {
            this.home = nextDir;
            out.writeUTF(this.commands.chdir + " " + this.home);
        } else {
            out.writeUTF(this.commands.error + " " + "Directory no exist" + "\n");
        }
    }

    protected void executeTouch(String name) throws IOException {
        this.dirController.touch(this.home.toString(), name);
        out.writeUTF(this.commands.success);
    }

    protected void executeMkdir(String dirName) throws IOException {
        this.dirController.mkdir(home.toString(), dirName);
        out.writeUTF(this.commands.success);
    }

    protected void executeGetDirs() throws IOException, UnsupportedOperationException {
        File file = this.home.toFile();
        File[] files = file.listFiles();

        String response = "";
        if (files == null) {
            out.writeUTF(response);
            return;
        }

        for (int i = 0; i < files.length; i++) {

            if (files[i].isDirectory()) {
                String name = files[i].getName();
                response += name + "\n";
            }
        }

        out.writeUTF(response);
    }

    protected void executeGetFiles() throws IOException {
        File file = this.home.toFile();
        File[] files = file.listFiles();

        String response = "";
        if (files == null) {
            out.writeUTF(response);
            return;
        }
        for (int i = 0; i < files.length; i++) {

            if (files[i].isFile()) {
                String name = files[i].getName();
                response += name + "\n";
            }
        }

        out.writeUTF(response);
    }
} // class
