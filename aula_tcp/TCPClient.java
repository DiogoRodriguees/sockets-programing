package aula_tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;

public class TCPClient {
    public static void main(String args[]) {

        Socket clientSocket = null; // socket do cliente
        Scanner reader = new Scanner(System.in); // ler mensagens via teclado
        Commands commands = new Commands();
        String localpath = "$";
        try {
            /// Endereço e porta do servidor
            int serverPort = 6666;
            InetAddress serverAddr = InetAddress.getByName("127.0.0.1");

            // creating socket tcp
            clientSocket = new Socket(serverAddr, serverPort);
            // defineing input and output
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            // use buuffer to store message
            String buffer = "";

            // starting listenner keyboard
            while (true) {
                System.out.print(localpath + " ");
                buffer = reader.nextLine(); // read keyboard

                String[] cmdParams = buffer.split(" ");
                if (cmdParams.length != 3) {
                    System.out.println("Parametros invalidos");
                    continue;
                }

                String user = cmdParams[1]; // use diogo
                String password = cmdParams[2];// use senha

                // Create MessageDigest instance for SHA-512
                MessageDigest md = MessageDigest.getInstance("SHA-512");

                // Add password bytes to digest
                md.update(password.getBytes());

                // Get the hash's bytes
                byte[] bytes = md.digest();

                // Convert bytes to base64 to get a printable representation
                String hashedPassword = Base64.getEncoder().encodeToString(bytes);
                String cmd = cmdParams[0] + " " + user + " " + hashedPassword;
                out.writeUTF(cmd); // send message to server

                buffer = in.readUTF(); // await confirm
                if (buffer.contains(commands.success)) {
                    String[] response = buffer.split(" ");
                    localpath = response[1];
                    break;
                }
                System.out.println(buffer);
            }

            while (true) {
                System.out.print(localpath + " $ ");
                buffer = reader.nextLine(); // read keyboard

                out.writeUTF(buffer); // send message to server

                if (buffer.equals("exit"))
                    break;

                buffer = in.readUTF(); // await confirm
                if (buffer.contains(commands.chdir)) {
                    String newPath = buffer.split(" ")[1];
                    localpath = newPath;
                    continue;
                }
                System.out.println(buffer);
            }
        } catch (UnknownHostException ue) {
            System.out.println("Socket:" + ue.getMessage());
        } catch (EOFException eofe) {
            System.out.println("EOF:" + eofe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IO:" + ioe.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("HASH" + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ioe) {
                System.out.println("IO: " + ioe);
                reader.close();
            }
        }
    } // main
} // class
