package aula_tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TCPClient {
    public static void main(String args[]) {

        Socket clientSocket = null; // socket do cliente
        Scanner reader = new Scanner(System.in); // ler mensagens via teclado

        try {
            /// Endereço e porta do servidor
            int serverPort = 6666;
            InetAddress serverAddr = InetAddress.getByName("127.0.0.1");

            // creating socket tcp
            clientSocket = new Socket(serverAddr, serverPort);
            System.out.flush();
            // defineing input and output
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            // use buuffer to store message
            String buffer = "";

            // starting listenner keyboard
            while (true) {
                System.out.print("user@home: ~$ ");
                buffer = reader.nextLine(); // read keyboard

                out.writeUTF(buffer); // send message to server

                if (buffer.equals("exit"))
                    break;

                buffer = in.readUTF(); // await confirm
                if (!buffer.equals(":OK"))
                    System.out.println(buffer);
            }
        } catch (UnknownHostException ue) {
            System.out.println("Socket:" + ue.getMessage());
        } catch (EOFException eofe) {
            System.out.println("EOF:" + eofe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IO:" + ioe.getMessage());
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
