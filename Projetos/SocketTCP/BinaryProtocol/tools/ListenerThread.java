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

        try {

            while (true) {

                // handle request (TODO: modularizar?)
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
                Integer sizeOfContentFile = header.getInt();        // Obtém o tamanho do conteúdo do arquivo

                Logger logger = Logger.getLogger("server.log");     // Associa o arquivo .log à variável logger

                // Verificando se o tipo da mensagem é uma requisição
                if (messageType == 1) {

                    logger.info("Message Type: "    + messageType +
                            " | Command ID: "       + commandId +
                            " | Size of FileName: " + filenameSize +
                            " | File Name: "        + filename);

                    if (commandId == 1) {
                        handleAddFile(this.output, filename, sizeOfContentFile, commandId, logger);

                    } else if (commandId == 2) {
                        // handleDelete(this.output, filename);

                    } else if (commandId == 3) {
                        // handleGetFilesList();

                    } else if (commandId == 4) {
                        // handleGetFile(this.output, filename);
                    }
                } // if message type
            } // while
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

    public void handleAddFile(DataOutputStream output, String filename, Integer sizeOfContentFile, byte commandId, Logger logger) throws IOException {
        
        // System.out.println("Tamanho do arquivo: " + sizeOfContentFile); TODO: remover

        byte SUCCESS = (byte) 1;
        byte ERROR = (byte) 2;
        
        if(sizeOfContentFile.intValue() > 0) {
            byte[] byteReaded = new byte[1];
            byte[] fileContentBytes = new byte[sizeOfContentFile];
            
            // Realiza a leitura dos bytes do arquivo recebido
            for (int i = 0; i < sizeOfContentFile; i++) {
                this.input.read(byteReaded);

                byte b = byteReaded[0];
                fileContentBytes[i] = b;
                // fileContentBytes[i] = byteReaded[0]; // TODO: testar trocar (?)
            }

            String fileContentString = new String(fileContentBytes);
            File newFile = new File(this.serverPath + filename);

            if (newFile.createNewFile()) {

                FileWriter writer = new FileWriter(newFile, true);
                BufferedWriter buf = new BufferedWriter(writer);
                buf.write(fileContentString);
                buf.flush();
                buf.close();

                logger.info("File '" + filename + "'' was added successfully!\n");
                logger.info("Sending response to client...");
                commonResponse(this.output, SUCCESS, SUCCESS);

            } else {
                
                logger.info("Something went wrong when copying the file '" + filename + "'.");
                logger.info("Sending response to client...");
                commonResponse(this.output, commandId, ERROR);
            }

        } else {
            logger.info("The file '" + filename + "' has no content.");
            logger.info("Sending response to client...");
            commonResponse(this.output, commandId, ERROR);
        }
    } // handleAddFile

    /**
     * Este método envia um cabeçalho de resposta comum para os comandos que não exigem
     * um cabeçalho específico, de acordo com o protocolo estabelecido. Isto é, será 
     * utilizado como resposta aos comandos ADDFILE e DELETE.
     * 
     * TODO: add params
     */
    private void commonResponse(DataOutputStream output, byte commandId, byte status) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(3); // Alocando 3 bytes para o cabeçalho
        header.order(ByteOrder.BIG_ENDIAN);         // Definindo a ordem dos bytes como big endian
        header.put((byte) 2);                       // Tipo da mensagem (2 == Resposta)
        header.put(commandId);                      // Identificador do comando
        header.put(status);                         // Status (1 == SUCCESS || 2 == ERROR)
        output.write(header.array());               // Convertendo o cabeçalho para um array de bytes
        output.flush();
    }
}
