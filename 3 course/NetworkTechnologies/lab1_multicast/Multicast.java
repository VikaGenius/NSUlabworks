import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Multicast {
    public static ConcurrentHashMap<String, Long> copies;

    public static void main(String[] args) {
        String msg = args[0];
        try {
            InetAddress mcastaddr = InetAddress.getByName("239.192.0.4");
            InetSocketAddress group = new InetSocketAddress(mcastaddr, 6789);
            NetworkInterface netIf = NetworkInterface.getByName("eth0");

            MulticastSocket socket = new MulticastSocket(6789);
            socket.setNetworkInterface(netIf);
            socket.setSoTimeout(100); 
            socket.joinGroup(new InetSocketAddress(mcastaddr, 0), netIf);
            
            Send(msg, socket, group);
            Recive(socket);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    static void Send(String msg, MulticastSocket socket, InetSocketAddress group) {
        Thread sender = new Thread(() -> {
            while (true) {
                byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
                DatagramPacket hi = new DatagramPacket(msgBytes, msgBytes.length, group);
                try {
                    socket.send(hi);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        sender.start();
    }

    static void Recive(MulticastSocket socket) {
        copies = new ConcurrentHashMap<String, Long>();

        Thread receiver = new Thread (() -> {
            while (true) {
                byte[] buf = new byte[1000];
                DatagramPacket recv = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(recv);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                String recvMsg = new String(buf, StandardCharsets.UTF_8);

                if (!copies.containsKey(recvMsg)) {
                    copies.put(recvMsg, System.currentTimeMillis());
                    System.out.println(copies);
                } else {
                    copies.put(recvMsg, System.currentTimeMillis());
                }
                
                CleanUp();
            }
        });
        receiver.start();
    }

    static void CleanUp() {
        for (Map.Entry<String, Long> pair: copies.entrySet()) {
            if (System.currentTimeMillis() - pair.getValue() > 5000) {
                copies.remove(pair.getKey());
                System.out.println(copies);
            }
        }
    }
}