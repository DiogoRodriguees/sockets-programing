/**
 * TCPServer: Servidor para conexao TCP com Threads Descricao: Recebe uma
 * conexao, cria uma thread, recebe uma mensagem e finaliza a conexao
 */

import java.net.*;

import tools.ListenerThread;

import java.io.*;

public class TCPServer {

    public static void main(String args[]) {
        try {
            int serverPort = 6666; // porta do servidor

            // cria um socket e mapeia a porta para aguardar conexão
            ServerSocket listenSocket = new ServerSocket(serverPort);

            while (true) {
                System.out.println("Waiting connections...");

                // aguarda conexões
                Socket clientSocket = listenSocket.accept();

                System.out.println("Client connected!");

                // cria um thread para atender a conexão
                ListenerThread listenerThread = new ListenerThread(clientSocket);

                // inicializa a thread
                listenerThread.start();
            } //while
            
            // listenSocket.close();
            
        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        } //catch
    } //main
} //class
