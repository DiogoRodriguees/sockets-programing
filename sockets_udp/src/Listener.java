package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Listener extends Thread {
    DatagramSocket datagramSocket;
    int sendPort;
    String[] types = { "normal", "emoji", "url", "ECHO" };

    public Listener(DatagramSocket socket, int sendPort) {
        this.datagramSocket = socket;
        this.sendPort = sendPort;
    }

    @Override
    public void run() {

        try {
            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket dgramPacket = new DatagramPacket(buffer, buffer.length);

                this.datagramSocket.receive(dgramPacket);

                String message = new String(dgramPacket.getData(), 0, dgramPacket.getLength());
                if (message.equals("exit")) {
                    break;
                }

                Message response = extractMessage(dgramPacket);

                if (response.typeMessage == 4) {
                    executeEcho(this.datagramSocket, dgramPacket, this.sendPort);
                }

                if (0 < response.typeMessage && response.typeMessage < 5) {
                    System.out.println(
                            response.nickname + "[" + this.types[response.typeMessage - 1] + "]: " + response.message);
                }
            }
        } catch (IOException ioe) {
            System.out.println("IO: " + ioe.getMessage());

        } catch (Exception e) {
            System.out.println("E: " + e.getMessage());
        }
    }

    protected static void executeEcho(DatagramSocket socket, DatagramPacket pkg, int port)
            throws UnknownHostException, IOException {
        byte[] data2 = pkg.getData();
        data2[0] = 1;
        InetAddress serverAddr = InetAddress.getByName("127.0.0.1");
        DatagramPacket request = new DatagramPacket(data2, data2.length, serverAddr, port);

        socket.send(request);
    }

    protected static Message extractMessage(DatagramPacket pkg) {
        byte[] data = pkg.getData();
        int offset = pkg.getOffset();

        // Extract fields from the received data
        int messageType = data[offset++];
        byte nicknameLength = data[offset++];
        byte[] nicknameBytes = new byte[nicknameLength];
        System.arraycopy(data, offset, nicknameBytes, 0, nicknameLength);
        offset += nicknameLength;
        byte messageLength = data[offset++];
        byte[] messageBytes = new byte[messageLength];
        System.arraycopy(data, offset, messageBytes, 0, messageLength);

        // Convert bytes to strings
        String nickname = new String(nicknameBytes);
        String msg = new String(messageBytes);

        return new Message(messageType, nicknameLength, nickname, messageLength, msg);
    }
}
