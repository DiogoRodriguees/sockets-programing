package sockets_tcp.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

import sockets_tcp.classes.Commands;
import sockets_tcp.classes.User;

public class Listenner extends Thread {

    Socket clientSocket;
    DataInputStream in;
    DataOutputStream out;

    Commands commands;

    User[] users;
    volatile Path currentPath = Paths.get(System.getProperty("user.dir") + "/sockets_tcp/server/home");
    DirectoryController dirController;

    public Listenner(Socket clientSocket, User[] users) throws IOException {
        this.users = users;
        this.clientSocket = clientSocket;
        this.in = new DataInputStream(clientSocket.getInputStream());
        this.out = new DataOutputStream(clientSocket.getOutputStream());
        this.commands = new Commands();
        this.dirController = new DirectoryController();
    }

    @Override
    public void run() {

        try {
            // await user sign in
            this.awaitConnection();

            // send path user
            out.writeUTF(this.commands.success + " " + this.currentPath);

            // listening commands pwd, getfiles, getdirs ...
            this.listenCommands();

        } catch (EOFException eofe) {
            System.out.println("EOF: " + eofe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
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

    protected void listenCommands() throws IOException {
        String buffer = "";
        boolean noExit = true;

        while (noExit) {
            buffer = in.readUTF();
            String[] cmdParams = buffer.split(" ");
            noExit = this.executeCommands(buffer, cmdParams);
        }
    }

    protected boolean executeCommands(String buffer, String[] cmdParams) throws IOException {

        if (cmdParams[0].equals(this.commands.pwd)) {
            System.out.println("---> Executing PWD command ...");
            this.executePwd();
            System.out.println("<--- PWD executed ...");
            return true;
        }

        if (cmdParams[0].equals(this.commands.chdir)) {
            System.out.println("---> Executing CHDIR command ...");
            this.executedChdir(cmdParams[1]);
            System.out.println("<--- CHDIR executed ...");
            return true;
        }

        if (cmdParams[0].equals(this.commands.getFiles)) {
            System.out.println("---> Executing GETFILES command ...");
            this.executeGetFiles();
            System.out.println("<--- GETFILES executed ...");
            return true;
        }

        if (cmdParams[0].equals(this.commands.getDirs)) {
            System.out.println("---> Executing GETDIRS command ...");
            out.writeUTF(":OK");
            System.out.println("<--- GETDIRS executed ...");
            return true;
        }

        if (cmdParams[0].equals(this.commands.mkdir)) {
            System.out.println("---> Executing MKDIR command ...");
            this.executeMkdir(cmdParams[1]);
            System.out.println("<--- MKDIR executed ...");
            return true;
        }

        if (cmdParams[0].equals(this.commands.touch)) {
            System.out.println("---> Executing TOUCH command ...");
            this.executeTouch(cmdParams[1]);
            System.out.println("<--- TOUCH executed ...");
            return true;
        }

        if (buffer.equals(this.commands.exit)) {
            System.out.println("---> Executing EXIT command ...");
            System.out.println("User wish close connection");
            out.writeUTF(this.commands.exit);
            System.out.println("<--- EXIT executed ...");
            return false;
        }

        out.writeUTF("Command not found");
        return true;
    }

    protected void awaitConnection() throws IOException {
        String buffer = "";

        while (true) {

            buffer = in.readUTF();
            String[] cmdParams = buffer.split(" ");

            if (cmdParams.length == 3 && cmdParams[0].equals(this.commands.connect)) {

                User user = this.getUserByName(cmdParams[1]);

                if (user == null) {
                    out.writeUTF("User not found");
                    continue;
                }

                boolean passwordIsCorrect = this.checkUserPassword(cmdParams[2], user.password);

                if (passwordIsCorrect) {
                    System.out.println("User " + user.user + " connected ...");

                    File d = new File(this.currentPath.toString(), user.user);

                    if (!d.exists()) {
                        boolean created = d.mkdirs();
                        if (created) {
                            System.out.println("Directory created successfully.");
                        } else {
                            System.out.println("Failed to create directory.");
                        }
                    }

                    this.currentPath = this.currentPath.resolve(user.user);
                    out.writeUTF(this.commands.success + " " + this.currentPath);
                    break;
                }
                out.writeUTF("Credentials incorrects");

                continue;
            }
        }
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

    protected void executedChdir(String dirName) throws IOException {
        this.currentPath = this.currentPath.resolve(dirName);
        System.out.println(this.currentPath);
        out.writeUTF(this.commands.chdir + " " + this.currentPath);
    }

    protected void executeTouch(String name) throws IOException {
        this.dirController.touch(this.currentPath.toString(), name);
        out.writeUTF(":OK");
    }

    protected void executeMkdir(String dirName) throws IOException {
        this.dirController.mkdir(currentPath.toString(), dirName);
        out.writeUTF(":OK");
    }

    protected void executePwd() throws IOException {
        out.writeUTF(this.currentPath.toString());
    }

    protected void executeGetFiles() throws IOException {
        out.writeUTF(":OK");
    }
} // class
