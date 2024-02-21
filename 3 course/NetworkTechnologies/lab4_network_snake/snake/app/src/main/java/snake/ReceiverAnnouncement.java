package snake;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import me.ippolitov.fit.snakes.SnakesProto.GameAnnouncement;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.DatagramSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ReceiverAnnouncement {
    ReceiverAnnouncement() {
        InetAddress mcastaddr;
        try {
            mcastaddr = InetAddress.getByName("239.192.0.4");
            group = new InetSocketAddress(mcastaddr, 9192);
            NetworkInterface netIf = NetworkInterface.getByName("eth0");
            socket = new MulticastSocket(9192);
        
            socket.setNetworkInterface(netIf);
            socket.joinGroup(group, netIf);

            messageHandler = new MessageHandler();
            copies = new ConcurrentHashMap<GameAnnouncement, Long>();
            addresses = new ArrayList<>();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    void reciveAnnouncement() {
        CurrentGames currentGames = new CurrentGames();
        Thread receiver = new Thread (() -> {
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000), event -> {
                if (reciveAnnouncementMessage()) {
                    currentGames.draw(getMapMsgAddress());
                }
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        });
        receiver.start();
    }

    boolean reciveAnnouncementMessage() {
        byte[] buf = new byte[4000];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(recv);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] bytes = new byte[recv.getLength()];
        System.arraycopy(recv.getData(), 0, bytes, 0, recv.getLength());

        GameAnnouncement msg = messageHandler.parseAnnouncement(bytes);
        if (!copies.containsKey(msg)) {
            copies.put(msg, System.currentTimeMillis());
            addresses.add(new Address(recv.getAddress(), recv.getPort()));
            CleanUp();
            return true;
        } else {
            copies.put(msg, System.currentTimeMillis());
            CleanUp();
            return false;
        }
        
    }

    static void CleanUp() {
        int i = 0;
        for (Map.Entry<GameAnnouncement, Long> pair: copies.entrySet()) {
            if (System.currentTimeMillis() - pair.getValue() > 3000) {
                copies.remove(pair.getKey());
                addresses.remove(i);
            } else {
                i++;

            }
        }
    }

    public HashMap<GameAnnouncement, Address> getMapMsgAddress() {
        HashMap<GameAnnouncement, Address> map = new HashMap<>();
        int i = 0;
        for (Map.Entry<GameAnnouncement, Long> pair: copies.entrySet()) {
            map.put(pair.getKey(), addresses.get(i));
            i++;
        }

        return map;
    }

    private static ConcurrentHashMap<GameAnnouncement, Long> copies;
    private InetSocketAddress group;
    private MulticastSocket socket;
    DatagramSocket socket1;
    private MessageHandler messageHandler;
    private static List<Address> addresses;

    public class Address {
        Address(InetAddress inetAddress, int port) {
            this.inetAddress = inetAddress;
            this.port = port;
        }
        
        public InetAddress getInetAddress() {
            return inetAddress;
        }
        public void setInetAddress(InetAddress inetAddress) {
            this.inetAddress = inetAddress;
        }
        public int getPort() {
            return port;
        }
        public void setPort(int port) {
            this.port = port;
        }

        private InetAddress inetAddress;
        private int port;

    }

}

