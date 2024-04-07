package aula_tcp.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import aula_tcp.classes.Commands;
import aula_tcp.classes.Files;
import aula_tcp.classes.User;

public class Listenner extends Thread {

    Socket clientSocket;
    DataInputStream in;
    DataOutputStream out;
    Directory directory;

    aula_tcp.classes.Commands commands;
    User[] users;
    boolean noExit = false;

    public Listenner(Socket clientSocket, User[] users) throws IOException {
        this.users = users;
        this.clientSocket = clientSocket;

        this.in = new DataInputStream(clientSocket.getInputStream());
        this.out = new DataOutputStream(clientSocket.getOutputStream());
        this.commands = new Commands();
    }

    @Override
    public void run() {

        try {
            // await user sign in
            this.awaitConnection();

            // send path user
            out.writeUTF(this.commands.success + " " + this.directory.pwd());

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
            return noExit;
        }

        if (cmdParams[0].equals(this.commands.chdir)) {
            System.out.println("---> Executing CHDIR command ...");
            this.executedChdir(cmdParams[1]);
            System.out.println("<--- CHDIR executed ...");
            return noExit;
        }

        if (cmdParams[0].equals(this.commands.getFiles)) {
            System.out.println("---> Executing GETFILES command ...");
            this.executeGetFiles();
            System.out.println("<--- GETFILES executed ...");
            return noExit;
        }

        if (cmdParams[0].equals(this.commands.getDirs)) {
            System.out.println("---> Executing GETDIRS command ...");
            String response = this.directory.getDirs();
            out.writeUTF(response == null ? ":OK" : response);
            System.out.println("<--- GETDIRS executed ...");
            return noExit;
        }

        if (cmdParams[0].equals(this.commands.mkdir)) {
            System.out.println("---> Executing MKDIR command ...");
            this.executeMkdir(cmdParams[1]);
            System.out.println("<--- MKDIR executed ...");
            return noExit;
        }

        if (cmdParams[0].equals(this.commands.touch)) {
            System.out.println("---> Executing TOUCH command ...");
            this.executeTouch(buffer);
            System.out.println("<--- TOUCH executed ...");
            return noExit;
        }

        if (buffer.equals(this.commands.exit)) {
            System.out.println("---> Executing EXIT command ...");
            System.out.println("User wish close connection");
            out.writeUTF(this.commands.exit);
            System.out.println("<--- EXIT executed ...");
            return true;
        }

        out.writeUTF("Command not found");
        return false;
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
                    this.directory = new Directory(null, "/home/" + user.user);
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
        Directory currentDir = this.directory.chdir(dirName);

        // check if directory was found
        if (currentDir == null) {
            out.writeUTF("This directory name not exist ....");
            throw new IOException("This directory not exist");
        }

        // update current directory
        this.directory = currentDir;

        // return path to directory received
        String response = this.directory.pwd();
        out.writeUTF(this.commands.chdir + " " + response);
    }

    protected void executeTouch(String name) throws IOException {
        Files file = this.directory.touch(name);
        if (file == null) {
            System.out.println("Failed on create file");
        }
        out.writeUTF(":OK");
    }

    protected void executeMkdir(String name) throws IOException {
        Directory dir = this.directory.mkdir(name);
        out.writeUTF("Diretorio " + dir.name + " created");
    }

    protected void executePwd() throws IOException {
        String response = this.directory.pwd();
        out.writeUTF(response);
    }

    protected void executeGetFiles() throws IOException {
        String fileNames = this.directory.getFiles();
        out.writeUTF(fileNames == null ? ":OK" : fileNames);
    }
} // class
