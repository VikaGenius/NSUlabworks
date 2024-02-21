package ftp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class Server {
    final static Logger logger = Logger.getLogger(Server.class.getName());

    void StartWorking() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                timer = new Timer();
                Recive(clientSocket);  
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    void Recive(Socket clientSocket) {
        Thread clientHandler = new Thread(() -> {
            try (DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {
                int nameLength = in.readInt();
                byte[] name = Read(in, nameLength);

                Path path = Paths.get(new String(name, StandardCharsets.UTF_8));
                String nameS = path.getFileName().toString();
                logger.info("Server recive file name: " + nameS);
                file = new File("/home/kesha/Сети/FTP/uploads/" + nameS);
                
                long fileSize = in.readLong();
                if (fileSize > 0) {
                    try (DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
                        if (ReadFile(fileSize, in) == true) {
                            out.writeUTF("Success");
                        } else {
                            out.writeUTF("Fail"); 
                        }
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                }
                
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

        });
        clientHandler.start();
    }

    boolean ReadFile(long fileSize, DataInputStream in) {
        logger.info("Server start recive file");

        try (RandomAccessFile f = new RandomAccessFile(file, "rw")) {
            AtomicLong tmp = new AtomicLong(fileSize);
            final long startTime = System.currentTimeMillis();
            AtomicLong bytesTransferredInLastInterval = new AtomicLong(0);
            
            TimerTask timerTask = new TimerTask() { 
                @Override
                public void run() {
                    long bytesInInterval = bytesTransferredInLastInterval.getAndSet(0);
                    long instantSpeed = bytesInInterval; 
                    long averageSpeed = (fileSize - tmp.get()) / (System.currentTimeMillis() - startTime) * 1000;
                    logger.info("Instant file download speed: " + Long.valueOf(instantSpeed));
                    logger.info("Average file download speed: " + Long.valueOf(averageSpeed));
                }
            }; 

            try {
                timer.scheduleAtFixedRate(timerTask, 1000, 1000);

                while (tmp.get() > 0) {
                    if (tmp.get() < sizeOfPiece) {
                        byte[] buf = Read(in, tmp.intValue());
                        f.write(buf);
                        tmp.getAndSet(0);
                        bytesTransferredInLastInterval.addAndGet(tmp.get());
                        break;
                    } else {
                        byte[] buf = Read(in, sizeOfPiece);
                        f.write(buf);
                        tmp.addAndGet(-4096);
                        bytesTransferredInLastInterval.addAndGet(sizeOfPiece);
                    }
                }
                if (fileSize == file.length()) {
                    logger.info("Успех");
                    timerTask.cancel();
                    return true;
                }
                else {
                    logger.info("Это провал");
                    timerTask.cancel();
                    return false;
                }
            } finally {
                timerTask.cancel();
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    public byte[] Read(DataInputStream in, int amountOfBytes) throws IOException {
        byte[] buf = new byte[amountOfBytes]; 
        int bytesRead = 0, tmp = 0;
        while (bytesRead != amountOfBytes) {
            tmp = in.read(buf, bytesRead, amountOfBytes - bytesRead); 
            if (tmp >= 0) {
                bytesRead += tmp;
            } else {
                throw new IOException("Socket in is closed");
            }
            
        }
        return buf;
    }

    private int port = 6969;
    private File file;
    private Timer timer;
    private final static int sizeOfPiece = 4096;
    
}


