package snake;

import java.util.List;
import java.util.ArrayList;

import com.google.protobuf.InvalidProtocolBufferException;

import me.ippolitov.fit.snakes.SnakesProto.*;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage.AckMsg;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage.AnnouncementMsg;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage.ErrorMsg;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage.JoinMsg;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage.PingMsg;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage.RoleChangeMsg;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage.StateMsg;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage.SteerMsg;

public class MessageHandler {
    GamePlayer getGamePlayer(String name, int id, String ipAddress, int port, NodeRole role, PlayerType type, int score) {
        return GamePlayer.newBuilder()
            .setName(name)
            .setId(id)
            .setIpAddress(ipAddress)
            .setPort(port)
            .setRole(role)
            .setType(type)
            .setScore(score)
            .build();

    }

    GamePlayer getGamePlayer(Player player) {
        return GamePlayer.newBuilder()
            .setName(player.getName())
            .setId(player.getId())
            .setIpAddress(player.getIpAddress())
            .setPort(player.getPort())
            .setRole(player.getRole())
            .setType(PlayerType.HUMAN)
            .setScore(player.getScore())
            .build();

    }

    GameConfig getGameConfig(int width, int height, int foodStatic, int delay) {
        return GameConfig.newBuilder()
            .setWidth(width)
            .setHeight(height)
            .setFoodStatic(foodStatic)
            .setStateDelayMs(delay)
            .build();
    }

    GameState.Coord getCoord(int x, int y) {
        return GameState.Coord.newBuilder()
            .setX(x)
            .setY(y)
            .build();
    }

    GameState.Snake getSnake(int id, List<GameState.Coord> coord, GameState.Snake.SnakeState state, Direction direction) {
        return GameState.Snake.newBuilder()
            .setPlayerId(id)
            .addAllPoints(coord)
            .setState(state)
            .setHeadDirection(direction)
            .build();
    }

    GamePlayers getGamePlayers(List<GamePlayer> players) {
        return GamePlayers.newBuilder() 
            .addAllPlayers(players)
            .build();

    }

    GameState getGameState(int stateOrder, List<GameState.Snake> snakes, List<GameState.Coord> foods, GamePlayers players) {
        GameState ret = GameState.newBuilder()
        .setStateOrder(stateOrder)
        .addAllSnakes(snakes)
        .addAllFoods(foods)
        .setPlayers(players)
        .build();

        return ret;
    }

    GameAnnouncement getGameAnnouncement(GamePlayers players, GameConfig config, boolean canJoin, String name) {
        return GameAnnouncement.newBuilder() 
            .setPlayers(players)
            .setConfig(config)
            .setCanJoin(canJoin)
            .setGameName(name)
            .build();
    }

    GameMessage.PingMsg getPingMsg() {
        return GameMessage.PingMsg.newBuilder()
            .build();
    }

    GameMessage.SteerMsg getSteerMsg(Direction direction) {
        return GameMessage.SteerMsg.newBuilder()
            .setDirection(direction)
            .build();
    }

    GameMessage.AckMsg getAckMsg() {
        return GameMessage.AckMsg.newBuilder()
            .build();
    }

    GameMessage.StateMsg getStateMsg(GameState state) {
        return GameMessage.StateMsg.newBuilder()
            .setState(state)
            .build();
    }

    GameMessage.AnnouncementMsg getAnnouncementMsg(List<GameAnnouncement> games) {
        return GameMessage.AnnouncementMsg.newBuilder()
            .addAllGames(games)
            .build();
    }

    GameMessage.JoinMsg getJoinMsg(PlayerType type, String playerName, String gameName, NodeRole role) {
        return GameMessage.JoinMsg.newBuilder()
            .setPlayerType(type)
            .setPlayerName(playerName)
            .setGameName(gameName)
            .setRequestedRole(role)
            .build();
    }

    GameMessage.ErrorMsg getErrorMsg(String error) {
        return GameMessage.ErrorMsg.newBuilder()
            .setErrorMessage(error)
            .build();
    }

    GameMessage.RoleChangeMsg getRoleChangeMsg(NodeRole senderRole, NodeRole receiverRole) {
        return GameMessage.RoleChangeMsg.newBuilder()
            .setSenderRole(senderRole)
            .setReceiverRole(receiverRole)
            .build();
    }

    GameMessage getGameMessagePing(long msgSeq, int senderId, int receiverId, PingMsg msg) {
        return GameMessage.newBuilder()
            .setMsgSeq(msgSeq)
            .setSenderId(senderId)
            .setReceiverId(receiverId)
            .setPing(msg)
            .build();
    }

    GameMessage getGameMessageSteer(long msgSeq, int senderId, int receiverId, SteerMsg msg) {
        return GameMessage.newBuilder()
            .setMsgSeq(msgSeq)
            .setSenderId(senderId)
            .setReceiverId(receiverId)
            .setSteer(msg)
            .build();
    }

    GameMessage getGameMessageAck(long msgSeq, int senderId, int receiverId, AckMsg msg) {
        return GameMessage.newBuilder()
            .setMsgSeq(msgSeq)
            .setSenderId(senderId)
            .setReceiverId(receiverId)
            .setAck(msg)
            .build();
    }

    GameMessage getGameMessageState(long msgSeq, int senderId, int receiverId, StateMsg msg) {
        return GameMessage.newBuilder()
            .setMsgSeq(msgSeq)
            .setSenderId(senderId)
            .setReceiverId(receiverId)
            .setState(msg)
            .build();
    }

    GameMessage getGameMessageAnnouncement(long msgSeq, List<Player> players, GameConfig config, String gameName, boolean canJoin) {
            List<GamePlayer> gamePlayers = new ArrayList<>();
            for (Player player : players) {
                gamePlayers.add(getGamePlayer(player));
            }
            GamePlayers msgGamePlayers = getGamePlayers(gamePlayers);

            GameAnnouncement announcement = getGameAnnouncement(msgGamePlayers, config, canJoin, gameName); 
            List<GameAnnouncement> announcements = new ArrayList<>();
            announcements.add(announcement);
            AnnouncementMsg msg = getAnnouncementMsg(announcements);

            return GameMessage.newBuilder()
            .setMsgSeq(msgSeq)
            .setSenderId(0)
            .setReceiverId(0)
            .setAnnouncement(msg)
            .build();
    }

    GameMessage getGameMessageJoin(long msgSeq, int senderId, int receiverId, JoinMsg msg) {
        return GameMessage.newBuilder()
            .setMsgSeq(msgSeq)
            .setSenderId(senderId)
            .setReceiverId(receiverId)
            .setJoin(msg)
            .build();
    }

    GameMessage getGameMessageError(long msgSeq, int senderId, int receiverId, ErrorMsg msg) {
        return GameMessage.newBuilder()
            .setMsgSeq(msgSeq)
            .setSenderId(senderId)
            .setReceiverId(receiverId)
            .setError(msg)
            .build();
    }

    GameMessage getGameMessageRoleChange(long msgSeq, int senderId, int receiverId, RoleChangeMsg msg) {
        return GameMessage.newBuilder()
            .setMsgSeq(msgSeq)
            .setSenderId(senderId)
            .setReceiverId(receiverId)
            .setRoleChange(msg)
            .build();
    }

    GameAnnouncement parseAnnouncement(byte[] messageBytes) {
        try {
            GameMessage msg = GameMessage.parseFrom(messageBytes);
            if (msg.hasAnnouncement()) {
                return msg.getAnnouncement().getGamesList().get(0);
            }
        } catch (InvalidProtocolBufferException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    GameMessage parseMessage(byte[] messageBytes) {
        try {
            return GameMessage.parseFrom(messageBytes);
        } catch (InvalidProtocolBufferException e) {
            //System.err.println(e.getMessage());
        }
        return null;
    }

}
