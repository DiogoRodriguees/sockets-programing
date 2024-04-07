package SocketTCP;

/**
 * TCPServer: Servidor para conexao TCP com Threads Descricao: Recebe uma
 * conexao, cria uma thread, recebe uma mensagem e finaliza a conexao
 */
import java.net.*;
import java.io.*;

public class TCPServer {

    public static void main(String args[]) {
        try {
            int serverPort = 6666; // porta do servidor

            /* cria um socket e mapeia a porta para aguardar conexao */
            ServerSocket listenSocket = new ServerSocket(serverPort);

            while (true) {
                System.out.println("Waiting connections...");

                /* aguarda conexoes */
                Socket clientSocket = listenSocket.accept();

                System.out.println("Client connected!");

                /* cria um thread para atender a conexao */
                ClientThread clientThread = new ClientThread(clientSocket);

                /* inicializa a thread */
                clientThread.start();
            } //while

        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        } //catch
    } //main
} //class

/**
 * Classe ClientThread: Thread responsavel pela comunicacao
 * Descricao: Rebebe um socket, cria os objetos de leitura e escrita,
 * aguarda msgs clientes e responde com a msg + :OK
 */
class ClientThread extends Thread {

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;

    String absolutePath = System.getProperty("user.dir");

    public ClientThread(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException ioe) {
            System.out.println("Connection:" + ioe.getMessage());
        } //catch
    } //construtor

    /* metodo executado ao iniciar a thread - start() */
    @Override
    public void run() {
        try {
            String buffer = "";
            
            while (!buffer.equals("PARAR")) {
                buffer = in.readUTF();   /* aguarda o envio de dados */

                System.out.println("Cliente disse: " + buffer);

                if (buffer.equals("PWD")) {
                    out.writeUTF(absolutePath);

                } else if (buffer.startsWith("CHDIR ")) {
                    
                    String[] parts = buffer.split(" ");

                    // verificando se há mais de 1 argumento
                    if (parts.length > 1)
                    {
                        out.writeUTF("ERROR");
                        
                    } else if (parts[1].equals("..")) {

                    }

                    // verificando se o arg é ".."

                    // verificando se o nome passado como arg é um dir e não um file
                        // obter o nome do diretório

                } else {
                    buffer += ":OK";
                    out.writeUTF(buffer);
                }
            }
        } catch (EOFException eofe) {
            System.out.println("EOF: " + eofe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException ioe) {
                System.err.println("IOE: " + ioe);
            }
        }
        System.out.println("Thread comunicação cliente finalizada.");
    } //run
} //class
