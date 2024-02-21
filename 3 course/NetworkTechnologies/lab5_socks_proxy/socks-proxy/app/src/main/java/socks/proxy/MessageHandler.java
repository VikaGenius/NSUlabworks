package socks.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class MessageHandler {
    DnsResolving dnsResolving;
    MessageHandler(DnsResolving dnsResolving) {
        this.dnsResolving = dnsResolving;
    }

    void handleClientGreeting(SelectionKey key, Client client) throws IOException {
        byte[] bytesMsg = client.insideBuf.array();
        if (client.bytesRead < 2) {
            return;
        }

        ByteBuffer tmp = ByteBuffer.wrap(bytesMsg);

        byte version = tmp.get();
        if (version != (byte) 0x05) {
            closeConnection(key, client);
            return;
        }

        byte nMethods = tmp.get();

        if (client.bytesRead == 2 + nMethods) {
            client.status = Status.ClientGreeting;
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
            client.insideBuf.clear(); 
            client.bytesRead = 0;
        }
    }

    public void buildServerChoice(Client client) {
        client.insideBuf.put((byte) 0x05); // Версия SOCKS5
        client.insideBuf.put((byte) 0x00); // Метод аутентификации (0x00 - без аутентификации)
    }

    public void handleRequest(SelectionKey key, Client client) throws IOException {
        byte[] bytesMsg = client.insideBuf.array();
        if (client.bytesRead < 4) {
            return;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytesMsg);

        byte version = buffer.get();
        byte cmd = buffer.get();
        buffer.get(); //rsv
        byte atyp = buffer.get();

        //String host;
        //int port;
        byte[] port = new byte[2];
        if (version != 0x05 || cmd != 0x01) {
            //closeConnection(key, client);
        } else {
            switch (atyp) {
                case 0x01: // IPv4 address
                    if (client.insideBuf.position() != 10) {
                        break;
                    } 
                    byte[] ipv4 = new byte[4];
                    buffer.get(ipv4);
                    //port = buffer.getShort() & 0xFFFF;
                    buffer.get(port);

                    client.port = port;
                    client.host = ipv4;
                    client.status = Status.Request;
                    client.domainOrIP = 1;

                    client.insideBuf.clear();
                    client.bytesRead = 0;
                    client.openOutsideSock(key.selector());

                    break;
    
                case 0x03: // Domain name
                    byte len = buffer.get();
                    if (client.insideBuf.position() != 7 + len) {
                        break;
                    } 
                    byte[] domainBytes = new byte[len];
                    buffer.get(domainBytes);
                    buffer.get(port);

                    client.port = port;
                    client.host = domainBytes;
                    dnsResolving.sendDnsQuery(client);

                    client.status = Status.Request;
                    client.domainOrIP = 3;
                    client.insideBuf.clear();

                    client.bytesRead = 0;
                    break;

                default:
                    closeConnection(key, client);
                    return;
            }
        }
    }

    public void buildReply(Client client, SelectionKey key) throws IOException {
        ByteBuffer reply;
        byte atyp;
        if (client.domainOrIP == 1) {
            reply = ByteBuffer.allocate(10);
            atyp = 0x01;
            reply.put(new byte[]{ 0x05, 0x00, 0x00, atyp });
            reply.put(client.host);
            reply.put(client.port);

            client.ip = InetAddress.getByAddress(client.host);

        } else {
            reply = ByteBuffer.allocate(7 + client.host.length);
            atyp = 0x03;
            reply.put(new byte[]{ 0x05, 0x00, 0x00, atyp });
            reply.put((byte)client.host.length);
            reply.put(client.host);
            reply.put(client.port); 
        }
        reply.flip();
        client.insideBuf.put(reply.array());
    }

    void closeConnection(SelectionKey key, Client client) throws IOException {
        if (client.clientChannel != key.channel()) {
            client.clientChannel.close();
        } else {
            key.channel().close();
            key.cancel();
        }
        if (client.outsideServer != null) {
            client.outsideServer.close();
        }
    }

}


