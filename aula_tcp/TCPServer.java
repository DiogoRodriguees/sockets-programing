package aula_tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    public static void main(String args[]) {
        try {

            int serverPort = 6666; // porta do servidor
            ServerSocket listenSocket = new ServerSocket(serverPort);

            String user1 = "diogo";
            String user2 = "gustavo";
            String senha1 = "TpOUtNKHa4dBsQovtGWJtg8aHBIem8TCgProWvdbda6GCdSfDkIV87aCMG3H8mKxcf/BgfiG92TWOCENb/e6KA==";
            String senha2 = "TpOUtNKHa4dBsQovtGWJtg8aHBIem8TCgProWvdbda6GCdSfDkIV87aCMG3H8mKxcf/BgfiG92TWOCENb/e6KA==";

            User[] users = new User[] { new User(user1, senha1), new User(user2, senha2) };

            while (true) {
                System.out.println("Servidor aguardando conexao ...");

                Socket clientSocket = listenSocket.accept();
                System.out.println("Cliente conectado ... Criando thread ...");

                Directory dirOrigin = new Directory(null, "/home/user");

                Listenner thread = new Listenner(clientSocket, dirOrigin, users);
                thread.start();
            }

        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        }
    }
}
