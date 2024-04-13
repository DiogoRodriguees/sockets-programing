package src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class Listenner extends Thread {

    Socket clientSocket;
    DataInputStream in;
    DataOutputStream out;

    User user;
    User[] users;

    Path home;
    Path userPath;
    Path currentPath;

    Commands commands;
    Directory dirController;

    public Listenner(Socket clientSocket, User[] users) throws IOException {
        this.users = users;
        this.clientSocket = clientSocket;

        this.in = new DataInputStream(clientSocket.getInputStream());
        this.out = new DataOutputStream(clientSocket.getOutputStream());

        this.commands = new Commands();
        this.dirController = new Directory();
        this.home = FileSystems.getDefault().getPath("src/users", "");
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
        boolean noConnected = true;

        while (noConnected) {
            // read input buffer
            buffer = in.readUTF();
            String[] cmdParams = buffer.split(" ");

            // check if params is correct
            if (cmdParams.length == 3 && cmdParams[0].equals(this.commands.connect)) {
                // extrac params
                String username = cmdParams[1];
                String password = cmdParams[2];
                User user = this.getUserByName(username);

                // return case user not exist
                if (user == null) {
                    out.writeUTF("User not found");
                    continue;
                }

                boolean passwordIsCorrect = this.checkUserPassword(password, user.password);

                if (passwordIsCorrect) {
                    System.out.format("User %s connected ...\n", user.user);

                    // update localpath and homepath
                    File file = this.dirController.mkdir(this.home.toString(), user.user);
                    this.home = file.toPath();
                    this.userPath = this.home;

                    // send response to client with status SUCCESS
                    out.writeUTF(this.commands.success + " " + this.home);
                    break;
                } else {
                    out.writeUTF("Password incorrect");
                }

            } else {
                out.writeUTF(this.commands.error + " " + "Command not found");
            }
        }
    }

    protected void listenCommands() throws IOException, EOFException, UnsupportedOperationException {
        boolean keepConnection = true;

        while (keepConnection) {
            String buffer = "";
            buffer = in.readUTF();

            String[] cmdParams = buffer.split(" ");
            keepConnection = this.executeCommands(buffer, cmdParams);
        }
    }

    protected boolean executeCommands(String buffer, String[] cmdParams) throws IOException {
        boolean keepConnection = true;
        String cmd = cmdParams[0];

        if (cmd.equals(this.commands.pwd)) {
            System.out.println("---> Executing PWD command ...");
            this.executePwd();
            System.out.println("<--- PWD executed ...");
            return keepConnection;
        }

        if (cmd.equals(this.commands.chdir)) {
            System.out.println("---> Executing CHDIR command ...");
            this.executedChdir(cmdParams[1]);
            System.out.println("<--- CHDIR executed ...");
            return keepConnection;
        }

        if (cmd.equals(this.commands.getFiles)) {
            System.out.println("---> Executing GETFILES command ...");
            this.executeGetFiles();
            System.out.println("<--- GETFILES executed ...");
            return keepConnection;
        }

        if (cmd.equals(this.commands.getDirs)) {
            System.out.println("---> Executing GETDIRS command ...");
            executeGetDirs();
            System.out.println("<--- GETDIRS executed ...");
            return keepConnection;
        }

        if (cmd.equals(this.commands.mkdir)) {
            System.out.println("---> Executing MKDIR command ...");
            this.executeMkdir(cmdParams[1]);
            System.out.println("<--- MKDIR executed ...");
            return keepConnection;
        }

        if (cmd.equals(this.commands.touch)) {
            System.out.println("---> Executing TOUCH command ...");
            this.executeTouch(cmdParams[1]);
            System.out.println("<--- TOUCH executed ...");
            return keepConnection;
        }

        if (buffer.equals(this.commands.exit)) {
            System.out.println("---> Executing EXIT command ...");
            out.writeUTF(this.commands.exit);
            System.out.println("<--- EXIT executed ...");
            return !keepConnection;
        }

        out.writeUTF("Command not found");
        return keepConnection;
    }

    protected boolean checkUserPassword(String password, String passoword2) {
        return password.equals(passoword2);
    }

    protected User getUserByName(String name) {
        User user = null;

        for (int i = 0; i < 2; i++)
            if (this.users[i].user.equals(name))
                user = this.users[i];

        return user;
    }

    protected void executePwd() throws IOException {
        out.writeUTF(this.home.toString() + " \n");
    }

    protected void executedChdir(String dirName) throws IOException {
        Path path = this.dirController.chdir(this.home, dirName, this.userPath);
        boolean pathIsValid = !(path == null);

        if (pathIsValid) {
            out.writeUTF(this.commands.chdir + " " + path);
        } else {
            out.writeUTF(this.commands.error + " \n");
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
        File[] files = this.convertPathToFileList(this.home);
        String response = this.dirController.getDirs(files); // get only directories
        out.writeUTF(response);
    }

    protected void executeGetFiles() throws IOException {
        File[] files = this.convertPathToFileList(this.home);
        String response = this.dirController.getFiles(files); // get only files
        out.writeUTF(response);
    }

    protected File[] convertPathToFileList(Path path) {
        File file = path.toFile();
        return file.listFiles();
    }
} // class
