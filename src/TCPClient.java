package src;

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
        Scanner reader = new Scanner(System.in); // ler mensagens via teclado
        Socket clientSocket = null; // socket do cliente
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
            boolean userNoConnected = true;
            boolean userConnected = true;

            while (userNoConnected) {
                String buffer = "";
                System.out.print(localpath + " ");
                buffer = reader.nextLine(); // read keyboard

                String[] cmdParams = buffer.split(" ");
                if (checkAmountParams(cmdParams)) {
                    System.out.println("Parametros invalidos");
                    continue;
                }

                String user = cmdParams[1]; // use diogo
                String password = cmdParams[2];// use senha

                MessageDigest md = MessageDigest.getInstance("SHA-512");
                md.update(password.getBytes());
                byte[] bytes = md.digest();
                String hashedPassword = Base64.getEncoder().encodeToString(bytes);

                // Convert bytes to base64 to get a printable representation
                String cmd = cmdParams[0] + " " + user + " " + hashedPassword;
                out.writeUTF(cmd); // send message to server

                buffer = in.readUTF(); // await confirm
                if (buffer.contains(commands.success)) {
                    String[] response = buffer.split(" ");
                    localpath = response[1];
                    userNoConnected = false;
                    break;
                }
                System.out.println(buffer);
            }

            // while no receive exit command
            while (userConnected) {
                // read input terminal
                String buffer = "";
                System.out.print(localpath + " $ ");
                buffer = reader.nextLine();
                String storageBuffer = buffer;

                // break case command is exit
                if (buffer.equals(commands.exit))
                    break;

                // read output buffer - server response
                out.writeUTF(buffer);
                buffer = in.readUTF();

                // change localpath when command chdir return sucess
                if (buffer.contains(commands.chdir)) {
                    String[] newPath = buffer.split(" ");
                    localpath = newPath[1];
                    continue;
                }

                // check response command
                if (!buffer.contains(commands.success)) {
                    System.out.print(buffer);
                } else if (buffer.contains(commands.error)) {
                    System.out.println("Command error: " + storageBuffer);
                }
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
                reader.close();
            } catch (IOException ioe) {
                System.out.println("IO: " + ioe);
            }
        }
    }

    static boolean checkAmountParams(String[] cmdParams) {
        return cmdParams.length != 3;
    }

}