package aula_tcp.client;

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

import aula_tcp.classes.Commands;

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
            String buffer = "";
            boolean userNoConnected = true;

            while (userNoConnected) {
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

                // Convert bytes to base64 to get a printable representation
                String hashedPassword = Base64.getEncoder().encodeToString(bytes);
                String cmd = cmdParams[0] + " " + user + " " + hashedPassword;
                out.writeUTF(cmd); // send message to server

                buffer = in.readUTF(); // await confirm
                if (buffer.contains(commands.success)) {
                    String[] response = buffer.split(" ");
                    localpath = response[1];
                    userNoConnected = false;
                }
                System.out.println(buffer);
            }

            // while no receive exit command
            while (true) {
                System.out.print(localpath + " $ ");
                buffer = reader.nextLine();

                if (buffer.equals(commands.exit))
                    break;

                // send message and await response
                out.writeUTF(buffer);
                buffer = in.readUTF(); // await confirm

                // if receive chdir command -> update localpath
                if (buffer.contains(commands.chdir)) {
                    String newPath = buffer.split(" ")[1];
                    localpath = newPath;
                    continue;
                }

                // show when no confirm message
                if (!buffer.contains(commands.confirm)) {
                    System.out.println(buffer);
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