package ftp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    public Client(String filePath) {
        this.file = new File(filePath);
    }

    void StartWorking() {
        try (Socket clientSocket = new Socket("localhost", serverPort)) {
            byte[] fileName = file.getName().getBytes(StandardCharsets.UTF_8);
            int nameLength = fileName.length;

            try (DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
                out.writeInt(nameLength);
                out.write(fileName);
                System.out.println("Client send name");
                long fileLength = file.length();
                out.writeLong(fileLength);
                SendFile(out, fileLength);

                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                System.out.println(in.readUTF());
                
                //отправка файла
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void SendFile(DataOutputStream out, long fileLength) {
        System.out.println("Client start send file");
        int sizeOfPiece = 4096;
        Long tmp = fileLength;

        try (RandomAccessFile f = new RandomAccessFile(file, "rw")) {
            while (tmp > 0) {
                if (tmp <= sizeOfPiece) {
                    byte[] buf = new byte[tmp.intValue()];
                    f.read(buf); 
                    out.write(buf);
                    break;
                } else {
                    byte[] buf = new byte[sizeOfPiece];
                    f.read(buf);
                    out.write(buf); 
                    tmp -= sizeOfPiece;
                }
        }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private int serverPort = 6969;
    private File file;
}
