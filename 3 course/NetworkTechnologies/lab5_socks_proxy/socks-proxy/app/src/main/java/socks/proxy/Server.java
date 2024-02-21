package socks.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Server {
    public  static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
    
    public void run () {
        try {
            Selector selector = Selector.open();

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(1080));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            DatagramChannel dnsResolver = DatagramChannel.open();
            dnsResolver.configureBlocking(false);
            dnsResolver.register(selector, SelectionKey.OP_READ);
            DnsResolving dnsResolving = new DnsResolving(dnsResolver);
            messageHandler = new MessageHandler(dnsResolving);

            System.out.println("SOCKS5 Proxy Server started on port 1080...");

            while (true) {
                selector.select();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    Client client = (Client)key.attachment();
                    keyIterator.remove();

                    try { 
                        if (!key.isValid()) {
                            key.cancel();
                            continue;
                        }

                        if (key.isAcceptable()) {
                            handleAccept(key, selector);
                        } else if (key.isReadable()) {
                            handleRead(key, client, dnsResolving);
                        } else if (key.isWritable()) {
                            handleWrite(key, client);
                        } else if (key.isConnectable()) {
                            handleConnect(key, client);
                        }
                        
                    } catch (Exception e) {
                        if (client.outsideServer != null && key.channel() != client.outsideServer) {
                            client.outsideServer.close();
                        }
                        key.channel().close();
                        key.cancel();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConnect(SelectionKey key, Client client) throws IOException {
        SocketChannel channel = (SocketChannel)key.channel();
        if (channel.finishConnect()) {
            key.interestOps(SelectionKey.OP_READ); 
            client.status = Status.ConnectedOutside; 

            SelectionKey keyIn = client.clientChannel.keyFor(key.selector());
            keyIn.interestOps(keyIn.interestOps() | SelectionKey.OP_WRITE);   
        } else {
            channel.close();
            key.cancel();
        }
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);

        Client client = new Client();
        clientChannel.register(selector, SelectionKey.OP_READ, client);
        client.status = Status.Connected;
        client.clientChannel = clientChannel;
    
        System.out.println("Accepted connection from: " + clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key, Client client, DnsResolving dnsResolving) throws IOException {
        if (client != null && key.channel() == client.outsideServer) {
            handleReadOutServer(client, key);
        } else if (key.channel() == dnsResolving.dnsChannel) {
            dnsResolving.handleDnsRead(key);
        } else {
            handleReadClient(key, client); 
        }  
    }

    private void handleReadClient(SelectionKey key, Client client) throws IOException {
        SocketChannel clientChannel = (SocketChannel)key.channel();
            int bytesRead = 0;

            bytesRead = clientChannel.read(client.insideBuf);

            if (bytesRead == -1) {
                client.isFinishedIn = true;
                if (client.outsideServer != null) {
                    client.outsideServer.shutdownOutput();
                }
                return;
            }

            client.bytesRead += bytesRead;

            if (client.insideBuf.position() == BUFFER_SIZE - 1) {
                key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
                client.insideBufIsFull = true;
            }

            if (bytesRead > 0) {
                if (client.status == Status.Connected) {
                    messageHandler.handleClientGreeting(key, client);
                } else if (client.status == Status.ClientGreeting) {
                    messageHandler.handleRequest(key, client);
                } else if (client.status == Status.ConnectedOutside) {
                    SelectionKey keyOut = client.outsideServer.keyFor(key.selector());
                    keyOut.interestOps(keyOut.interestOps() | SelectionKey.OP_WRITE);
                }

            }
    }

    private void handleReadOutServer(Client client, SelectionKey key) throws IOException {
        int readBytes = client.outsideServer.read(client.outsideBuf);

        if (readBytes == -1) {
            client.isFinishedOut = true;
            client.outsideServer.shutdownOutput();
            return;
        }

        client.status = Status.Transfer;
        SelectionKey keyIn = client.clientChannel.keyFor(key.selector());
        keyIn.interestOps(keyIn.interestOps() | SelectionKey.OP_WRITE);
        
    }

    private void handleWriteOutServer(Client client, SelectionKey key) throws IOException {    
        client.insideBuf.flip();
        client.outsideServer.write(client.insideBuf);
        client.insideBuf.clear();

        if (client.insideBuf.hasRemaining() == false) {
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
        }
        
    }

    private void handleWrite(SelectionKey key, Client client) throws IOException {
        if (key.channel() == client.outsideServer) {
            handleWriteOutServer(client, key);
        } else {
            handleWriteClient(key, client);
        }
    }

    private void handleWriteClient(SelectionKey key, Client client) throws IOException {
        if (client.status == Status.ClientGreeting) {
            messageHandler.buildServerChoice(client);
        } else if (client.status == Status.ConnectedOutside) {
            messageHandler.buildReply(client, key);

        } else if (client.status == Status.Transfer) {
            client.outsideBuf.flip();
            int writeBytes = client.clientChannel.write(client.outsideBuf);
            if (writeBytes < 0) {
                throw new IOException();
            }
            client.outsideBuf.clear();
            if (client.outsideBuf.hasRemaining() == false) {
                key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                if (client.isFinishedIn && client.isFinishedOut) {
                    closeConnection(key, client);
                }
            }
            

            return;
        }

        SocketChannel clientChannel = (SocketChannel)key.channel();
        client.insideBuf.flip();
        int bytesWrite = 0;

        bytesWrite = clientChannel.write(client.insideBuf);
        if (bytesWrite < 0) {
            throw new IOException(); 
        }
        
        if (client.insideBuf.hasRemaining() == false) {
            client.insideBuf.clear();
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            if (client.isFinishedIn && client.isFinishedOut) {
                closeConnection(key, client);
            }
        }
        
        if (client.insideBufIsFull && bytesWrite > 0) {
            client.insideBufIsFull = false;
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
        }
    }

    void closeConnection(SelectionKey key, Client client) throws IOException {
        if (client.outsideServer != null) {
            client.outsideServer.close();
        }
        key.channel().close();
        key.cancel();
    }

    private MessageHandler messageHandler;
    private static final int BUFFER_SIZE = 1024;

}
