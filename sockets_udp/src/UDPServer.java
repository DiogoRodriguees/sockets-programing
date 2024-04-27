package src;

import java.io.IOException;
/**
 * UDPServer: Servidor UDP
 * Descricao: Recebe um datagrama de um cliente, imprime o conteudo e retorna o mesmo
 * datagrama ao cliente
 */
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class UDPServer {
    public static void main(String args[]) {
        DatagramSocket dgramSocket;
        Scanner read = new Scanner(System.in);

        try {
            dgramSocket = new DatagramSocket(Integer.parseInt(args[0]));

            String nickname = args[2];

            String destinationIP = args[3];
            int sendToPort = Integer.parseInt(args[1]);

            InetAddress serverAddr = InetAddress.getByName(destinationIP);
            int serverPort = sendToPort; // porta do servidor

            boolean exit = false;

            Listener listener = new Listener(dgramSocket, sendToPort);
            listener.start();

            while (!exit) {
                String msg = read.nextLine();
                exit = msg.equals("exit");

                try {
                    Integer typeMsg = Integer.parseInt(msg.substring(0, 1));
                    msg = msg.substring(1);

                    byte[] apelidoBytes = nickname.getBytes();
                    byte[] msgBytes = msg.getBytes();

                    byte[] data = createPackage(apelidoBytes, msgBytes, typeMsg);

                    /* cria um pacote datagrama */
                    DatagramPacket request = new DatagramPacket(data, data.length, serverAddr, serverPort);

                    /* envia o pacote */
                    dgramSocket.send(request);
                } catch (Exception e) {
                    System.out.println("Inpupt incorrect - format correct: <type> <message> (1 sua menssagem)");
                }

                // Create datagram to send message
            } // while

            dgramSocket.close();
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            read.close();
        } // finally
    } // main

    protected static byte[] createPackage(byte[] nickname, byte[] message, int typeMsg) {
        int totalLength = 1 + 1 + nickname.length + 1 + message.length;
        byte[] data = new byte[totalLength];

        int offset = 0;

        data[offset++] = (byte) typeMsg; // Type of message

        data[offset++] = (byte) nickname.length; // Size of nickname
        System.arraycopy(nickname, 0, data, offset, nickname.length); // Nickname
        offset += nickname.length;

        data[offset++] = (byte) message.length; // Size of message
        System.arraycopy(message, 0, data, offset, message.length); // Message

        return data;
    }

}// class
