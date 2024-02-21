package snake;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Map;

import me.ippolitov.fit.snakes.SnakesProto.GameAnnouncement;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage;
import me.ippolitov.fit.snakes.SnakesProto.NodeRole;
import me.ippolitov.fit.snakes.SnakesProto.PlayerType;
import snake.ReceiverAnnouncement.Address;

public class JoinGameController {
    JoinGameController() {
        messageHandler = new MessageHandler();
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            System.err.println(e.getMessage());
        }
    }

    void joinGame(Map.Entry<GameAnnouncement, Address> pair, String playerName, NodeRole role) {
        GameMessage.JoinMsg joinMsg = messageHandler.getJoinMsg(PlayerType.HUMAN, playerName, pair.getKey().getGameName(), role);
        GameMessage message = messageHandler.getGameMessageJoin(msgSeq, 0, 0, joinMsg);
        

        sendMessage(message, pair.getValue());
        GameMessage answer = receiveMessage();
        
        if (answer.hasAck()) {
            UserController normalController = new UserController(pair.getKey(), answer.getReceiverId(), socket, pair.getValue(), NodeRole.NORMAL);
            normalController.start();
        } else if (answer.hasError()) {

        }
    }

    void sendMessage(GameMessage msg, Address address) {
        try {
            byte[] message = msg.toByteArray();
            DatagramPacket packet = new DatagramPacket(message, message.length, address.getInetAddress(), address.getPort());
            socket.send(packet);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    GameMessage receiveMessage() {
        byte[] buf = new byte[4000];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(recv);

            byte[] bytes = new byte[recv.getLength()];
            System.arraycopy(recv.getData(), 0, bytes, 0, recv.getLength());
            
            return messageHandler.parseMessage(bytes);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

    private MessageHandler messageHandler;
    private int msgSeq = 0;
    private DatagramSocket socket;
}
