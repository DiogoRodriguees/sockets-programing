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

                if (buffer.equals("pwd")) {
                    String response = this.directory.pwd();
                    out.writeUTF(response);
                }

                if (buffer.equals("chdir")) {
                    Directory nextDirectory = this.directory.chdir("chdirTeste");

                    if (nextDirectory == null)
                        out.writeUTF("This directory name not exist ....");

                    this.directory = nextDirectory;

                    out.writeUTF(":OK");
                    System.out.println("Complete change directory");
                }

                if (buffer.equals("files")) {
                    System.out.println("---> Trying get files");

                    Files[] files = this.directory.files;

                    if (files != null) {
                        String response = "";
                        Integer current = 0;
                        Files file = files[current++];

                        while (file != null) {
                            response += file.name + "." + file.ext + "\n";
                            file = files[current++];
                        }

                        out.writeUTF(response);
                    }
                    System.out.println("<--- Get files complete");
                }
                if (buffer.equals("dirs")) {
                    System.out.println("---> Trying get dirs");

                    Directory[] directories = this.directory.getDirs();

                    if (directories != null) {

                        String response = "\n";
                        Integer current = 0;
                        Directory directory = directories[current++];

                        while (directory != null) {
                            response = directory.name + "\n";
                            directory = directories[current++];
                        }

                        out.writeUTF(response);
                    }

                    System.out.println("<--- Get directories complete");
                }

                if (buffer.contains("mkdir")) {
                    System.out.println("---> Trying create directory");

                    Integer free = this.directory.amountDirectories;
                    this.directory.directories[free] = new Directory(this.directory, "TesteDirectory");
                    this.directory.amountDirectories++;
                    out.writeUTF(":OK");

                    System.out.println("<--- Create direcotory complete");
                }

                if (buffer.equals("touch")) {
                    System.out.println("---> Create file request");

                    Integer free = this.directory.amountFiles;
                    this.directory.files[free] = new Files("TesteFiles", "txt");
                    this.directory.amountFiles++;
                    out.writeUTF(":OK");

                    System.out.println("<--- Create file complete\n");
                }

                if (buffer.equals("exit")) {
                    System.out.println("User wish close connection");
                    out.writeUTF("exit");
                    break;
                }
                buffer = ":OK";
                out.writeUTF(buffer);
            }
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
} // class
