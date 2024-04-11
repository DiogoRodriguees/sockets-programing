package BinaryProtocol.tools;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.*;

public class ListenerThread extends Thread {

    DataInputStream input;
    DataOutputStream output;
    Socket clientSocket;

    String localPath = System.getProperty("user.dir");
    String serverPath = System.getProperty("user.dir") + "/Documents/";
    String downloadPath = System.getProperty("user.dir") + "/Downloads/";

    public ListenerThread(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            this.input = new DataInputStream(clientSocket.getInputStream());
            this.output = new DataOutputStream(clientSocket.getOutputStream());

            File directory = new File(serverPath);
            if (!directory.exists()) {
                directory.mkdir();
            }

        } catch (IOException ioe) {
            System.out.println("Connection:" + ioe.getMessage());
        }
    } // constructor

    @Override
    public void run() {

        int size = 258;

        try {

            while (true) {

                String buffer = "";

                // handle request (modularizar?)
                byte[] request = new byte[258];
                this.input.read(request);

                // Obtendo os dados do cabeçalho da requisição
                ByteBuffer header = ByteBuffer.wrap(request);       // Cria um ByteBuffer a partir do array de bytes recebido
                header.order(ByteOrder.BIG_ENDIAN);                 // Define a ordem dos bytes (BIG_ENDIAN)
                byte messageType = header.get();                    // Obtém o tipo da mensagem (1 byte)
                byte commandId = header.get();                      // Obtém o código do comando (1 byte)
                byte filenameSize = header.get();                   // Obtém o tamanho do nome do arquivo (1 byte)

                byte[] filenameBytes = new byte[filenameSize];      // Cria um array de bytes para o nome do arquivo
                header.get(filenameBytes);                          // Obtém o nome do arquivo em bytes (tamanho variável)
                String filename = new String(filenameBytes);        // Converte o nome do arquivo para String
                Integer fileSize = header.getInt();                 // Obtém o tamanho do conteúdo do arquivo

                Logger logger = Logger.getLogger("server.log");     // Associa o arquivo .log criado à variável logger

                // Verificando se o tipo da mensagem é uma requisição
                if (messageType == 1) {

                    logger.info("Message Type: "    + messageType +
                            " | Command ID: "       + commandId +
                            " | Size of FileName: " + filenameSize +
                            " | File Name: "        + filename);

                    if (commandId == 1) {
                        handleAddFile(this.output, filename, fileSize, commandId);

                    } else if (commandId == 2) {
                        // handleDelete(this.output, filename);

                    } else if (commandId == 3) {
                        // handleGetFilesList();

                    } else if (commandId == 4) {
                        // handleGetFile(this.output, filename);
                    }
                }
            }
        } catch (EOFException eofe) {
            System.out.println("EOF: " + eofe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        } finally {
            try {
                this.input.close();
                this.output.close();
                this.clientSocket.close();
            } catch (IOException ioe) {
                System.err.println("IOE: " + ioe);
            }
        }
        System.out.println("Comunication client-server finished.");
    } // run

    public void handleAddFile(DataOutputStream output, String filename, Integer fileSize, byte commandId) {
        
        // System.out.println("Tamanho do arquivo: " + fileSize);
        
        if(fileSize.intValue() > 0) {
            byte[] bytes = new byte[1];
            byte[] contentByte = new byte[fileSize];
            
            for (int i = 0; i < fileSize; i++) {
                this.input.read(bytes);
                byte b = bytes[0];
                contentByte[i] = b;
            }

            int worked = 0;
            String content = new String(contentByte);
            File file = new File(this.serverPath + filename);
            if (file.createNewFile()) {
                FileWriter writer = new FileWriter(file, true);
                BufferedWriter buf = new BufferedWriter(writer);
                buf.write(content);
                buf.flush();
                buf.close();
                worked = 1;
            }

            if(worked == 1) {
                logger.info("Arquivo " + filename + " adicionado com sucesso\n");
                logger.info("Enviando resposta para o cliente");
                sendDeleteAndAddFileResponse(this.output, (byte) 1, (byte) 1);
            } else {
                logger.info("Erro ao copiar arquivo " + filename);
                logger.info("Enviando resposta para o cliente");
                sendDeleteAndAddFileResponse(this.output, (byte) 1, (byte) 0);
            }
        } else {
            sendDeleteAndAddFileResponse(this.output, commandId, (byte) 0);
        }
    }
}
