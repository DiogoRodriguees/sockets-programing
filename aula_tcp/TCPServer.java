package aula_tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    public static void main(String args[]) {
        try {

            int serverPort = 6666; // porta do servidor
            ServerSocket listenSocket = new ServerSocket(serverPort);

            while (true) {
                System.out.println("Servidor aguardando conexao ...");

                Socket clientSocket = listenSocket.accept();
                System.out.println("Cliente conectado ... Criando thread ...");

                Directory dirOrigin = new Directory(null, "/home/user");
                System.out.println(dirOrigin.directories);
                Listenner thread = new Listenner(clientSocket, dirOrigin);
                thread.start();
            }

        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        }
    }
}
