/**
 * TCPServer: Servidor para conexões com clientes através de sockets TCP e Threads
 * Descricao: Ao receber uma conexão, mapeia uma thread para atendê-la de forma a
 * entrar em um estado não-bloqueante para atender às possíveis novas conexões.
 * 
 * Autores: Diogo Rodrigues dos Santos e Gustavo Zanzin Guerreiro Martins
 * 
 * Data de criação: 07/04/2024
 * 
 * Datas de atualização: 
**/

import java.io.*;
import java.net.*;
import java.util.logging.*;


public class TCPServer {

        public static void main(String args[]) {
    
            try {
                int serverPort = 6666; // porta do servidor
    
                // criando um socket e mapeando a porta para aguardar conexão
                ServerSocket listenSocket = new ServerSocket(serverPort);
    
                // configurando o arquivo de log do servidor
                FileHandler fileHandler = new FileHandler("server.log");  // cria um arquivo de log
                Logger logger = Logger.getLogger("server.log");              // associa o arquivo de log ao objeto logger
                logger.addHandler(fileHandler);                                   
                SimpleFormatter formatter = new SimpleFormatter();                // cria um formatador
                fileHandler.setFormatter(formatter);                              // adiciona o formatador ao arquivo de log
    
                while (true) {
                    // aguardando conexões
                    System.out.println("Waiting connections...");
                    
                    Socket clientSocket = listenSocket.accept();
                    logger.info("Client conected!");
                    
                    // cria um thread para atender a conexao
                    ListenerThread listenerThread = new ListenerThread(clientSocket);
    
                    // inicializa a thread
                    listenerThread.start();
                } // while
    
            } catch (IOException e) {
                System.out.println("Listen socket:" + e.getMessage());
            } // catch
        } // main
    } // class