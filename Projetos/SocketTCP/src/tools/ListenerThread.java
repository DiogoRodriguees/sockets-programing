package tools;

/**
 * Classe ClientThread: Thread responsável pela comunicação
 * Descricao: Rebebe um socket, cria os objetos de leitura e escrita,
 * aguarda msgs clientes e responde com a msg + :OK
 */

import java.net.*;
import java.io.*;

public class ListenerThread extends Thread {

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;

    String currentPath = System.getProperty("user.dir");

    public ListenerThread(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException ioe) {
            System.out.println("Connection:" + ioe.getMessage());
        } // catch
    } // construtor

    // metodo executado ao iniciar a thread - start()
    @Override
    public void run() {
        String buffer = "";
        String response = "";

        try {
            
            while (!buffer.equals("PARAR")) {
                buffer = in.readUTF();   // aguarda o envio de dados

                System.out.println("Cliente disse: " + buffer);

                response = handleCommand(buffer);
                this.out.writeUTF(response);
            }

        } catch (EOFException eofe) {
            // logger
            System.out.println("EOF: " + eofe.getMessage());
        } catch (IOException ioe) {
            // logger
            System.out.println("IOE: " + ioe.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException ioe) {
                System.err.println("IOE: " + ioe);
            }
        } // finally
        System.out.println("Thread comunicação cliente finalizada.");
    } // run

    // public void commandOptions() {}

    String handleCommand(String command) throws IOException {

        String response = "";
        
        if (command.equals("PWD")) {
            //logger
            response = getPWD();

        } else if (command.startsWith("CHDIR ")) {
            
            // logger
            // changeDir(command);
            response = "SUCCESS";

        } else {
            command += ":OK";
            out.writeUTF(command);
        }

        return response;
    } // handleCommand

    // String connect(command) {}

    String getPWD() {
        return this.currentPath;
    }

    // String changeDir(String command) {}

    // String getFiles(command) {}

    // String getDirs(command) {}

    // String disconnect(command) {}

} // class