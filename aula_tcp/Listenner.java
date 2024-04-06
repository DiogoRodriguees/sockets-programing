package aula_tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

class Listenner extends Thread {

    Socket clientSocket;
    DataInputStream in;
    DataOutputStream out;
    Directory directory;

    Commands commands;

    public Listenner(Socket clientSocket, Directory origin) throws IOException {
        // client socket
        this.clientSocket = clientSocket;

        // defines input and output default
        this.in = new DataInputStream(clientSocket.getInputStream());
        this.out = new DataOutputStream(clientSocket.getOutputStream());

        // create directory to user
        this.directory = origin;

        this.commands = new Commands();
    }

    private static String hashPassword(String password) {
        try {
            // Create MessageDigest instance for SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // Add password bytes to digest
            md.update(password.getBytes());

            // Get the hash's bytes
            byte[] bytes = md.digest();

            // Convert bytes to base64 to get a printable representation
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void run() {
        try {
            String buffer = "";
            String user = "diogo";
            String senha = "TpOUtNKHa4dBsQovtGWJtg8aHBIem8TCgProWvdbda6GCdSfDkIV87aCMG3H8mKxcf/BgfiG92TWOCENb/e6KA==";

            while (true) {
                buffer = in.readUTF();
                String[] cmdParams = buffer.split(" ");

                if (cmdParams[0].equals(this.commands.connect)) {
                    // System.out.println(senha);
                    // System.out.println(hashedInputPassword);
                    if (cmdParams[2].equals(senha)) {
                        System.out.println("Passwords match.");
                        System.out.println("User connected ...");
                        break;
                    } else {
                        buffer = "Credenciais invalidas";
                        out.writeUTF(buffer);
                        System.out.println("Passwords do not match.");
                    }
                } else {
                    buffer = "Voce nao esta conectado";
                    out.writeUTF(buffer);
                }
            }

            buffer = this.commands.success;
            out.writeUTF(buffer);

            while (true) {
                buffer = in.readUTF();
                String[] cmdParams = buffer.split(" ");

                if (cmdParams[0].equals(this.commands.pwd)) {
                    System.out.println("---> Executing PWD command ...");

                    String response = this.directory.pwd();
                    out.writeUTF(response);

                    System.out.println("<--- PWD executed ...");
                    continue;
                }

                if (cmdParams[0].equals(this.commands.chdir)) {
                    System.out.println("---> Executing CHDIR command ...");

                    // get directory
                    Directory nextDirectory = this.directory.chdir(cmdParams[1]);

                    // check if directory exist
                    if (nextDirectory == null) {
                        out.writeUTF("This directory name not exist ....");
                        continue;
                    }

                    // update current directory
                    this.directory = nextDirectory;

                    // return
                    String response = this.directory.pwd();
                    out.writeUTF(response);
                    System.out.println("Complete change directory");

                    System.out.println("<--- CHDIR executed ...");
                    continue;
                }

                if (cmdParams[0].equals(this.commands.getFiles)) {
                    System.out.println("---> Executing GETFILES command ...");

                    String fileNames = this.directory.getFileNames();

                    out.writeUTF(fileNames);
                    System.out.println("<--- GETFILES executed ...");
                    continue;
                }

                if (cmdParams[0].equals(this.commands.getDirs)) {
                    System.out.println("---> Trying get dirs");

                    String response = this.directory.getDirs();

                    out.writeUTF(response);
                    System.out.println("<--- Get directories complete");
                    continue;
                }

                if (cmdParams[0].equals(this.commands.mkdir)) {
                    System.out.println("---> Trying create directory");

                    if (cmdParams.length == 1) {
                        out.writeUTF("MKDIR command need a name");
                        continue;
                    }

                    String response = this.directory.mkdir(cmdParams[1]);
                    out.writeUTF(response);
                    System.out.println("<--- Create direcotory complete");
                    continue;
                }

                if (cmdParams[0].equals(this.commands.touch)) {
                    System.out.println("---> Create file request");

                    if (cmdParams.length == 1) {
                        out.writeUTF("TOUCH command need a file name");
                        continue;
                    }

                    String response = this.directory.touch(cmdParams[1]);

                    out.writeUTF(response);
                    System.out.println("<--- Create file complete\n");
                    continue;
                }

                if (buffer.equals(this.commands.exit)) {
                    System.out.println("User wish close connection");
                    out.writeUTF("exit");
                    break;
                }
                System.out.println("Read again ...");
                buffer = "Command not found";
                out.writeUTF(buffer);
            }
        } catch (

        EOFException eofe) {
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

} // class
