package src;

import java.io.EOFException;
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

                Listenner thread = new Listenner(clientSocket, users);
                thread.start();
            }

        } catch (EOFException eof) {
            System.out.println("Listen socket:" + eof.getMessage());
        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        } catch (UnsupportedOperationException unoe) {
            System.out.println("Listen socket:" + unoe.getMessage());
        } catch (Exception ex) {
            System.out.println("Listen socket:" + ex.getMessage());

        } finally {

        }
    }
}
