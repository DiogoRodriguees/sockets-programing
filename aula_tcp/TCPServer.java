package aula_tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
/**
 * TCPServer: Servidor para conexao TCP com Threads Descricao: Recebe uma
 * conexao, cria uma thread, recebe uma mensagem e finaliza a conexao
 */
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    public static void main(String args[]) {
        try {
            int serverPort = 6666; // porta do servidor

            /* cria um socket e mapeia a porta para aguardar conexao */
            ServerSocket listenSocket = new ServerSocket(serverPort);

            while (true) {
                System.out.println("Servidor aguardando conexao ...");

                /* aguarda conexoes */
                Socket clientSocket = listenSocket.accept();

                System.out.println("Cliente conectado ... Criando thread ...");

                /* cria um thread para atender a conexao */
                ClientThread c = new ClientThread(clientSocket);

                /* inicializa a thread */
                c.start();
            } // while

        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        } // catch
    } // main
} // class

/**
 * Classe ClientThread: Thread responsavel pela comunicacao
 * Descricao: Rebebe um socket, cria os objetos de leitura e escrita,
 * aguarda msgs clientes e responde com a msg + :OK
 */
class ClientThread extends Thread {

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    String state = "NO_CONNECT";
    Protocol protocol;

    public ClientThread(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            this.in = new DataInputStream(clientSocket.getInputStream());
            this.out = new DataOutputStream(clientSocket.getOutputStream());
            this.protocol = new Protocol();
        } catch (IOException ioe) {
            System.out.println("Connection:" + ioe.getMessage());
        } // catch
    } // construtor

    /* metodo executado ao iniciar a thread - start() */
    @Override
    public void run() {
        try {
            String buffer = "";
            while (true) {
                buffer = in.readUTF(); /* aguarda o envio de dados */

                System.out.println("Cliente disse: " + buffer);

                if (buffer.contains("CONNECT")) {
                    this.protocol.connect();
                    System.out.println(protocol.state);
                    System.out.println("MESSAGE CONNECT");
                }
                if (buffer.contains("PWD")) {
                    System.out.println("MESSAGE PWD");
                }
                if (buffer.contains("CHDIR")) {

                    System.out.println("MESSAGE CHDIR");
                }
                if (buffer.contains("GETFILES")) {

                    System.out.println("MESSAGE GETFILES");
                }
                if (buffer.contains("GETDIRS")) {

                    System.out.println("MESSAGE GETDIRS");
                }
                if (buffer.contains("EXIT")) {

                    System.out.println("MESSAGE EXIT");
                }
                if (buffer.equals("PARAR")) {

                    break;
                }

                buffer += ":OK";
                out.writeUTF(buffer);
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
    } // run
} // class
