/**
 * TCPClient: Cliente que conecta-se ao servidor através de um socket
 * para manipular arquivos no servidor com operações de leitura e escrita.
 * Descricao: A comunicação entre as entidades ocorre por meio de um
 * protocolo. O protocolo define as requisições do cliente ao servidor
 * como tipo de mensagem, identificador do comando, tamanho do nome do
 * arquivo e o nome do arquivo. As operações de leitura e escrita são
 * ADDFILE, DELETE, GETFILESLIST e GETFILE.
 * 
 * Autores: Diogo Rodrigues dos Santos e Gustavo Zanzin Guerreiro Martins
 * 
 * Data de criação: 07/04/2024
 * 
 * Datas de atualização: 
**/

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class TCPClient {
    public static void main(String args[]) {
        Socket clientSocket = null;               // socket do cliente
        Scanner reader = new Scanner(System.in);  // ler mensagens via teclado

        try {
            // Obtendo o endereço IP e a porta do servidor
            int serverPort = 6666;
            InetAddress serverAddr = InetAddress.getByName("127.0.0.1");

            // Conectando-se com o servidor
            clientSocket = new Socket(serverAddr, serverPort);

            // Criando objetos de leitura e escrita
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

            // Implementando o protocolo de comunicação
            String buffer = "";
            String filenameDown = "";
            while (true) {

                System.out.print("$ ");
                buffer = reader.nextLine(); // lê mensagem via teclado

                handleCommand(buffer, output);

                byte[] headerBytes = new byte[258];
                in.read(headerBytes);
                ByteBuffer headerBuffer = ByteBuffer.wrap(headerBytes);
                headerBuffer.order(ByteOrder.BIG_ENDIAN);
                byte messageType = headerBuffer.get();
                byte commandId = headerBuffer.get();

                // Verificando qual o tipo de comando realizado pelo servidor para tratar o cabeçalho corretamente
                if(messageType == 0x02) {
                    switch(commandId){
                        case 0x01:
                            // handleDeleteAndAddFileResponse(headerBuffer, commandId);
                            break;
                        case 0x02:
                            // handleDeleteAndAddFileResponse(headerBuffer, commandId);
                            break;
                        case 0x03:
                            // handleGetFilesListResponse(headerBuffer);
                            break;
                    }
                }
            } // while

        } catch (UnknownHostException ue) {
            System.out.println("Socket:" + ue.getMessage());
        } catch (EOFException eofe) {
            System.out.println("EOF:" + eofe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IO:" + ioe.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ioe) {
                System.out.println("IO: " + ioe);
            } // catch
        } // finally
    } // main


    /**
     * Método para tratar os comandos de ADDFILE, DELETE, GETFILESLIST e GETFILE.
     * 
     * @param command - Comando a ser tratado.
     * @param output - Stream de saída.
     * @throws IOException
     */
    static void handleCommand(String command, DataOutputStream output) throws IOException {
        String[] splitedCommand = command.split(" ");

        if (splitedCommand[0].equals("ADDFILE") && splitedCommand.length == 2) {
            String filename = splitedCommand[1];
            byte commandIdentifier = (byte) 1;
            executeAddFile(commandIdentifier, output, filename);

        } else if (splitedCommand[0].equals("DELETE") && splitedCommand.length == 2) {
            // sendCommonRequests(output, (byte) 2, splitedCommand[1]);

        } else if (splitedCommand[0].equals("GETFILESLIST") && splitedCommand.length == 1) {
            // sendCommonRequests(output, (byte) 3, "");

        } else if (splitedCommand[0].equals("GETFILE") && splitedCommand.length == 2) {
            // sendGetFileRequest(output, (byte) 4, splitedCommand[1]);
        } else {
            System.out.println("Comando inválido");
        }
    } // handleCommand


    /**
     * Método para executar o comando ADDFILE.
     * 
     * O comando ADDFILE transmitirá um arquivo para o servidor. Caso o arquivo já exista localmente, o cliente enviará
     * não só o cabeçalho da requisição, mas também o conteúdo do arquivo. Caso o arquivo não exista, o cliente enviará
     * apenas o cabeçalho da requisição.
     * 
     * 
     * @param commandIdentifier - Identificador do comando ADDFILE (0x01).
     * @param output - Objeto de escrita.
     * @param filename - Nome do arquivo a ser transmitido.
     * @throws IOException
     */
    private static void executeAddFile(byte commandIdentifier, DataOutputStream output, String filename) throws IOException {
        
        byte sizeOfFilename = (byte) filename.length();  // Tamanho do nome do arquivo em bytes
        byte[] filenameBytes = filename.getBytes();      // Nome em si do arquivo convertido para bytes
        
        // Verificando se o arquivo existe para obter seu tamanho
        File fileInCurrentDir = new File(System.getProperty("user.dir") + "/" + filename);
        long fileSize = fileInCurrentDir.exists() ? fileInCurrentDir.length() : 0;
        int fileSizeInt = (int) fileSize;

        // Criação do cabeçalho
        ByteBuffer header = createHeader(commandIdentifier, sizeOfFilename, filenameBytes, fileSizeInt);
        byte[] headerBytes = getHeaderBytes(header);

        // Envio do cabeçalho e, se o arquivo existir, envio do conteúdo do arquivo
        if (fileInCurrentDir.exists()) {
            try (FileInputStream fis = new FileInputStream(fileInCurrentDir)) {
                writeHeaderAndFileContent(output, headerBytes, fis);
            }
        } else {
            // Se o arquivo não existir, apenas o cabeçalho é enviado
            System.out.println("Arquivo não encontrado: " + fileInCurrentDir);
            output.write(headerBytes);
        }

        output.flush();
    } // executeAddFile


    /**
     * Método para criar o cabeçalho de uma requisição.
     * 
     * @param commandIdentifier - Identificador do comando.
     * @param sizeOfFilename - Tamanho do nome do arquivo.
     * @param filenameBytes - Nome do arquivo em bytes.
     * @param fileSizeInt - Tamanho do arquivo em bytes.
     * @return - Cabeçalho.
     */
    private static ByteBuffer createHeader(byte commandIdentifier, byte sizeOfFilename, byte[] filenameBytes, int fileSizeInt) {
        ByteBuffer header = ByteBuffer.allocate(262);
        header.order(ByteOrder.BIG_ENDIAN);
        header.put((byte) 1);               // Message Type (1 == Request)
        header.put(commandIdentifier);
        header.put(sizeOfFilename);
        header.put(filenameBytes);
        header.putInt(fileSizeInt);
        return header;
    } // createHeader
    

    /**
     * Método para obter o array de bytes do cabeçalho. Note que o array de bytes retornado tem o tamanho
     * exato do cabeçalho. Portanto, não é necessário especificar um comprimento ao escrever o cabeçalho
     * em output, pois o array headerBytes já contém apenas os bytes do cabeçalho, sem bytes extras.
     * 
     * @param header - Cabeçalho.
     * @return - Array de bytes do cabeçalho.
    */
    private static byte[] getHeaderBytes(ByteBuffer header) {
        int headerSize = header.position();
        return Arrays.copyOf(header.array(), headerSize);
    }


    /**
     * Método para enviar, byte a byte, o cabeçalho e o conteúdo de um arquivo ao servidor.
     * 
     * @param output - Stream de saída.
     * @param headerBytes - Cabeçalho em bytes.
     * @param fis - Stream de entrada que fará a leitura do conteúdo do arquivo a ser enviado.
     * @throws IOException
     */
    private static void writeHeaderAndFileContent(DataOutputStream output, byte[] headerBytes, FileInputStream fis) throws IOException {
        output.write(headerBytes);
        int byteReaded;
        while ((byteReaded = fis.read()) != -1) {
            output.write(byteReaded);
        }
        // TO DO: verificar se assim está correto ou
        // se é necessário concatenar todos os dados do arquivo e
        // depois enviar tudo em apenas uma escrita
    }


} // class
