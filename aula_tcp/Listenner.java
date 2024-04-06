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

    public Listenner(Socket clientSocket, Directory origin) throws IOException {
        // client socket
        this.clientSocket = clientSocket;

        // defines input and output default
        this.in = new DataInputStream(clientSocket.getInputStream());
        this.out = new DataOutputStream(clientSocket.getOutputStream());

        // create directory to user
        this.directory = origin;
    }

    @Override
    public void run() {
        try {
            String buffer = "";

            while (true) {
                buffer = in.readUTF(); /* aguarda o envio de dados */

                if (buffer.contains("connect")) {
                    System.out.println("User connected ...");
                    break;
                } else {
                    buffer = "Voce nao esta conectado";
                    out.writeUTF(buffer);
                }
            }

            buffer = "Connectado";
            out.writeUTF(buffer);

            while (true) {
                buffer = in.readUTF();
                String[] command = buffer.split(" ");

                if (command[0].equals("pwd")) {
                    System.out.println("---> Executing PWD command ...");

                    String response = this.directory.pwd();
                    out.writeUTF(response);

                    System.out.println("<--- PWD executed ...");
                    continue;
                }

                if (command[0].equals("chdir")) {
                    System.out.println("---> Executing CHDIR command ...");

                    // get directory
                    Directory nextDirectory = this.directory.chdir(command[1]);

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

                if (command[0].equals("getfiles")) {
                    System.out.println("---> Executing GETFILES command ...");

                    String fileNames = this.directory.getFileNames();

                    out.writeUTF(fileNames);
                    System.out.println("<--- GETFILES executed ...");
                    continue;
                }

                if (command[0].equals("getdirs")) {
                    System.out.println("---> Trying get dirs");

                    String response = this.directory.getDirs();

                    out.writeUTF(response);
                    System.out.println("<--- Get directories complete");
                    continue;
                }

                if (command[0].equals("mkdir")) {
                    System.out.println("---> Trying create directory");

                    if (command.length == 1) {
                        out.writeUTF("MKDIR command need a name");
                        continue;
                    }

                    String response = this.directory.mkdir(command[1]);
                    out.writeUTF(response);
                    System.out.println("<--- Create direcotory complete");
                    continue;
                }

                if (command[0].equals("touch")) {
                    System.out.println("---> Create file request");

                    if (command.length == 1) {
                        out.writeUTF("TOUCH command need a file name");
                        continue;
                    }

                    String response = this.directory.touch(command[1]);

                    out.writeUTF(response);
                    System.out.println("<--- Create file complete\n");
                    continue;
                }

                if (buffer.equals("exit")) {
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
