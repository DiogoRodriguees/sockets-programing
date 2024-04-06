package aula_tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

class Listenner extends Thread {

    Socket clientSocket;
    DataInputStream in;
    DataOutputStream out;
    Directory directory;

    Commands commands;
    User[] users;

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
    } // run

    protected void listenCommands() {
        String buffer = "";

        while (true) {
            try {
                buffer = in.readUTF();
                String[] cmdParams = buffer.split(" ");
                this.executeCommands(buffer, cmdParams);
            } catch (IOException ioe) {
                System.out.println("IOE: " + ioe.getMessage());
            }
        }
    }

    protected void executeCommands(String buffer, String[] cmdParams) throws IOException {

        if (cmdParams[0].equals(this.commands.pwd)) {
            System.out.println("---> Executing PWD command ...");

            String response = this.directory.pwd();
            out.writeUTF(response);

            System.out.println("<--- PWD executed ...");
            return;
        }

        if (cmdParams[0].equals(this.commands.chdir)) {
            System.out.println("---> Executing CHDIR command ...");
            this.executedChdir(cmdParams[1]);
            System.out.println("<--- CHDIR executed ...");
            return;
        }

        if (cmdParams[0].equals(this.commands.getFiles)) {
            System.out.println("---> Executing GETFILES command ...");

            String fileNames = this.directory.getFiles();

            out.writeUTF(fileNames);
            System.out.println("<--- GETFILES executed ...");
            return;
        }

        if (cmdParams[0].equals(this.commands.getDirs)) {
            System.out.println("---> Trying get dirs");

            String response = this.directory.getDirs();

            out.writeUTF(response);
            System.out.println("<--- Get directories complete");
            return;
        }

        if (cmdParams[0].equals(this.commands.mkdir)) {
            System.out.println("---> Trying create directory");
            this.executeMkdir(cmdParams[1]);
            System.out.println("<--- Create direcotory complete");
            return;
        }

        if (cmdParams[0].equals(this.commands.touch)) {
            System.out.println("---> Create file request");
            this.directory.touch(cmdParams[1]);
            System.out.println("<--- Create file complete\n");
            return;
        }

        if (buffer.equals(this.commands.exit)) {
            System.out.println("User wish close connection");
            out.writeUTF("exit");
            return;
        }

        out.writeUTF("Command not found");
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
        // get directory
        Directory currentDir = this.directory.chdir(dirName);

        // check if directory exist
        if (currentDir == null) {
            out.writeUTF("This directory name not exist ....");
            throw new IOException("This directory not exist");
        }

        // update current directory
        this.directory = currentDir;

        // return
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

} // class
