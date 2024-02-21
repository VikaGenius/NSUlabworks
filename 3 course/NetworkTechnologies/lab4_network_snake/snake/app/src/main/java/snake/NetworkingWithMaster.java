package snake;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.DatagramSocket;

import me.ippolitov.fit.snakes.SnakesProto.GameMessage;

public class NetworkingWithMaster {
    public NetworkingWithMaster() {
        InetAddress mcastaddr;
        try {
            mcastaddr = InetAddress.getByName("239.192.0.4");
            group = new InetSocketAddress(mcastaddr, 9192);
            //NetworkInterface netIf = NetworkInterface.getByName("eth0");
            socket = new DatagramSocket();
            messageHandler = new MessageHandler();


        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public NetworkingWithMaster(DatagramSocket socket) {
        InetAddress mcastaddr;
        try {
            mcastaddr = InetAddress.getByName("239.192.0.4");
            group = new InetSocketAddress(mcastaddr, 9192);
            messageHandler = new MessageHandler();
            this.socket = socket;
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
        }

    }
    
    void SendAnnouncement(byte[] msg) {
        DatagramPacket hi = new DatagramPacket(msg, msg.length, group);
        try {
            socket.send(hi);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Message ReciveMessage() {
        byte[] buf = new byte[4000];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(recv);

            byte[] bytes = new byte[recv.getLength()];
            System.arraycopy(recv.getData(), 0, bytes, 0, recv.getLength());

            GameMessage message = messageHandler.parseMessage(bytes);
            Message msg = new Message(new InetSocketAddress(recv.getAddress(), recv.getPort()), message);
            return msg;

            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    void SendMessage(Message message) {
        try {
            byte[] gameMessageInBytes = message.getGameMessage().toByteArray();
            DatagramPacket ancw = new DatagramPacket(gameMessageInBytes, gameMessageInBytes.length, message.getSocketAddress());
            socket.send(ancw);
            
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }


    private InetSocketAddress group;
    DatagramSocket socket;
    private MessageHandler messageHandler;



}

