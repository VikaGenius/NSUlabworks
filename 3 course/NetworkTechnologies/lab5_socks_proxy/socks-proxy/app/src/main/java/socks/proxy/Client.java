package socks.proxy;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import java.net.InetAddress;
import java.net.InetSocketAddress;


import java.io.IOException;

public class Client {
        public Client() {
            insideBuf = ByteBuffer.allocate(BUFFER_SIZE);
            outsideBuf = ByteBuffer.allocate(BUFFER_SIZE);
        }

        public void openOutsideSock(Selector selector) throws IOException {
            outsideServer = SocketChannel.open();
            ByteBuffer buf = ByteBuffer.wrap(port);
            int intPort = buf.getShort() & 0xFFFF ;

            outsideServer.configureBlocking(false);
            outsideServer.connect(new InetSocketAddress(ip, intPort));
            outsideServer.register(selector, SelectionKey.OP_CONNECT, this);
        }

        int shutIN = 0;
        int shutOUT = 0;
        SocketChannel outsideServer;
        SocketChannel clientChannel;
        boolean insideBufIsFull = false;
        boolean outsideBufIsFull = false;
        boolean isFinishedIn = false;
        boolean isFinishedOut = false;
        Status status = Status.NotConnected;
        InetAddress ip;
        ByteBuffer insideBuf;
        ByteBuffer outsideBuf;
        byte[] host;
        byte[] port;
        int bytesRead;
        int domainOrIP = 0; //если 1, то айпи, если 3, то доменное имя
        private static final int BUFFER_SIZE = 143360;
    }

    enum Status {
            NotConnected, //без связи
            Connected, //на связи
            ClientGreeting, //жмем руки
            Request, //реквест
            ConnectedOutside,
            Transfer
    };