package snake;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.DatagramSocket;

import me.ippolitov.fit.snakes.SnakesProto.*;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage.JoinMsg;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage.RoleChangeMsg;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage.SteerMsg;

import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.List;

import java.util.Map;


import java.util.concurrent.CopyOnWriteArrayList;

import java.util.concurrent.ConcurrentHashMap;

public class Master {
    public Master(String name) {
        myId = idForNewPlayer;
        Player player = new Player(name, NodeRole.MASTER, "localhost", 0, myId);
        players.add(player);
        idForNewPlayer++;
        networking = new NetworkingWithMaster();
    }

    public Master(DatagramSocket socket, int id, String name, GameConfig config, GameController game, int stateOrder) {
        networking = new NetworkingWithMaster(socket);
        myId = id;
        gameName = name;
        this.config = config;
        this.delay = config.getStateDelayMs();
        this.game = game;
        this.stateOrder = stateOrder;
        idForNewPlayer = myId + 5;
    }

    void changeRole(List <Player> gamePlayers) {
        players = gamePlayers;
        //System.out.println("CHANGE ROLE=======================");

        for (Player player: players) {
            //System.out.println("IP: " + player.getIpAddress() + " PORT: " + player.getPort());
            RoleChangeMsg roleMsg = msgHandler.getRoleChangeMsg(NodeRole.MASTER, player.getRole());
            GameMessage msg = msgHandler.getGameMessageRoleChange(msgSeq, myId, player.getId(), roleMsg);
            try {
                networking.SendMessage(new Message(new InetSocketAddress(InetAddress.getByName(player.getIpAddress()), player.getPort()), msg));
            } catch (UnknownHostException e) {
                System.err.println(e.getMessage());
            }
        }
        msgSeq++;
    }

    public void startGame(int width, int height, int foodStatic, int delay, String gameName) {
        this.gameName = gameName;
        this.delay = delay;
        config = msgHandler.getGameConfig(width, height, foodStatic, delay);
        game = new GameController(config, gameName, players.get(0));
        game.start(this);
        sendReceive();
        checkLastTime();
    }

    public void continueLogic() {
        game.continueGame(this);
        sendReceive();
        checkLastTime();
    }

// хохохо

    private void sendReceive() {
        Timer senderTimer2 = new Timer("SenderTimer2", true);


        TimerTask senderAnnouncementTask = new TimerTask() {
            @Override
            public void run() {
                GameMessage msg = msgHandler.getGameMessageAnnouncement(msgSeq, players, config, gameName, true);
                networking.SendAnnouncement(msg.toByteArray());
            }
        };

        Thread receiverTask = new Thread(() -> {
            while(true) {
                receivedMessageHandle(networking.ReciveMessage());
            }
                
        });
        receiverTask.start();

        //senderTimer.scheduleAtFixedRate(senderStateTask, 0, config.getStateDelayMs());
        senderTimer2.scheduleAtFixedRate(senderAnnouncementTask, 0, 1000);

        // Add shutdown hooks to stop the timers when the program exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            senderTimer2.cancel();
            
        }));
    }

    void receivedMessageHandle(Message message) {
        GameMessage gameMessage = message.getGameMessage();
        idAndLastTime.put(gameMessage.getSenderId(), System.currentTimeMillis());
        if (gameMessage.hasJoin()) {
            handleJoin(gameMessage, message.getSocketAddress());
        }
        if (gameMessage.hasSteer()) {
            handleSteer(gameMessage, message.getSocketAddress());
        }
        if (gameMessage.hasPing()) {
            handlePing(gameMessage, message.getSocketAddress());
        }
        if (gameMessage.hasRoleChange()) {
            handleRole(gameMessage, message.getSocketAddress());
        }
    }

    void sendState() {
        for (Player player : players) {
            if (player.getId() != myId && player.getPort() != 0) {
                System.out.println("IP : " + player.getIpAddress() + "PORT: " + player.getPort() );
                GameState state = msgHandler.getGameState(stateOrder, game.getSnakes(), game.getFoods(), msgHandler.getGamePlayers(getGamePlayersss()));
                stateOrder++;
                GameMessage gameMessage = msgHandler.getGameMessageState(msgSeq, myId, player.getId(), msgHandler.getStateMsg(state));
                try {
                    networking.SendMessage(new Message(new InetSocketAddress(InetAddress.getByName(player.getIpAddress()), player.getPort()), gameMessage));
                } catch (UnknownHostException e) {
                    System.err.println(e.getMessage());
                }
                msgSeq++;
            }
        }
    }

    void handleSteer(GameMessage gameMessage, InetSocketAddress addr) {
        SteerMsg steerMsg = gameMessage.getSteer();
        game.ChangeDirection(gameMessage.getSenderId(), Snake.getGameDirection(steerMsg.getDirection()));
        GameMessage ack = msgHandler.getGameMessageAck(gameMessage.getMsgSeq(), myId, gameMessage.getSenderId(), msgHandler.getAckMsg());
        Message newMessage = new Message(addr, ack);
        networking.SendMessage(newMessage);

        //setLastTime(gameMessage.getSenderId());
        System.out.println(idAndLastTime);
    }

    void handleRole(GameMessage gameMessage, InetSocketAddress addr) {
        RoleChangeMsg roleChangeMsg = gameMessage.getRoleChange();
        if (roleChangeMsg.getSenderRole() == NodeRole.VIEWER) {
            Player player = Player.getPlayer(players, gameMessage.getSenderId());
            player.setRole(NodeRole.VIEWER);
        }
    }

    void handlePing(GameMessage gameMessage, InetSocketAddress addr) {
        GameMessage ack = msgHandler.getGameMessageAck(gameMessage.getMsgSeq(), myId, gameMessage.getSenderId(), msgHandler.getAckMsg());
        Message newMessage = new Message(addr, ack);
        networking.SendMessage(newMessage);
    }

    void handleJoin(GameMessage gameMessage, InetSocketAddress addr) {
        JoinMsg joinMsg = gameMessage.getJoin();
            if (joinMsg.getRequestedRole() == NodeRole.NORMAL) {

                NodeRole role;
                if (players.size() == 1) {
                    role = NodeRole.DEPUTY;
                } else {
                    role = NodeRole.NORMAL;
                }

                Player player = new Player(joinMsg.getPlayerName(), role, addr.getAddress().getHostAddress(), addr.getPort(), idForNewPlayer);

                if (game.initializeNewSnake(player)) {
                    GameMessage ack = msgHandler.getGameMessageAck(gameMessage.getMsgSeq(), myId, idForNewPlayer, msgHandler.getAckMsg());
                    Message newMessage = new Message(addr, ack);
                    networking.SendMessage(newMessage);

                    idForNewPlayer++;

                    if (role == NodeRole.DEPUTY) {
                        RoleChangeMsg roleChangeMsg = msgHandler.getRoleChangeMsg(NodeRole.MASTER, NodeRole.DEPUTY);
                        GameMessage gMsg = msgHandler.getGameMessageRoleChange(msgSeq, myId, player.getId(), roleChangeMsg);
                        try {
                            networking.SendMessage(new Message(new InetSocketAddress(InetAddress.getByName(player.getIpAddress()), player.getPort()), gMsg));
                        } catch (UnknownHostException e) {
                            System.err.println(e.getMessage());
                        }
                        msgSeq++;
                    }

                    players.add(player);

                } else {
                    GameMessage error = msgHandler.getGameMessageError(gameMessage.getMsgSeq(), myId, 0, msgHandler.getErrorMsg("There's no room on the field"));
                    Message newMessage = new Message(addr, error);
                    networking.SendMessage(newMessage);
                }

            } else if (joinMsg.getRequestedRole() == NodeRole.VIEWER) {
                    GameMessage ack = msgHandler.getGameMessageAck(gameMessage.getMsgSeq(), myId, idForNewPlayer, msgHandler.getAckMsg());
                    Message newMessage = new Message(addr, ack);
                    networking.SendMessage(newMessage);
                    players.add(new Player(joinMsg.getPlayerName(), NodeRole.VIEWER, addr.getAddress().getHostAddress(), addr.getPort(), idForNewPlayer));
                    idForNewPlayer++;
            }
        }

    void checkLastTime() {
        Timer timer = new Timer();
        TimerTask checker = new TimerTask() {
            @Override
            public void run() {
                if (players.size() > 1) {
                    for (Map.Entry<Integer, Long> pair : idAndLastTime.entrySet()) {
                        if (pair.getKey() != myId && ((System.currentTimeMillis() - pair.getValue()) > 3.0 * delay)) {

                            idAndLastTime.remove(pair.getKey());

                            for (Player player: players) {
                                if (pair.getKey() == player.getId()) {
                                    if (player.getRole() == NodeRole.DEPUTY) {
                                        if (players.size() > 2) {
                                            int ind = players.indexOf(player);
                                            Player newDeputy = players.get(ind);
                                            newDeputy.setRole(NodeRole.DEPUTY);
                                            players.set(ind, newDeputy);

                                            RoleChangeMsg roleChangeMsg = msgHandler.getRoleChangeMsg(NodeRole.MASTER, NodeRole.DEPUTY);
                                            GameMessage gameMessage = msgHandler.getGameMessageRoleChange(msgSeq, myId, player.getId(), roleChangeMsg);
                                            try {
                                                networking.SendMessage(new Message(new InetSocketAddress(InetAddress.getByName(player.getIpAddress()), player.getPort()), gameMessage));
                                            } catch (UnknownHostException e) {
                                                System.err.println(e.getMessage());
                                            }
                                            msgSeq++;
                                        }
                                    }  
                                }
                                players.remove(player);
                                break;
                            }     
                        }
                    } 
                }
            }
        };
        timer.scheduleAtFixedRate(checker, 0, delay);
        
    }

    List<GamePlayer> getGamePlayersss() {
        List<GamePlayer> gamePlayers = new ArrayList<>();
        for (Player player: game.players) {
            gamePlayers.add(msgHandler.getGamePlayer(player));
        }

        return gamePlayers;
    }

    private static MessageHandler msgHandler = new MessageHandler();
    private GameConfig config;
    private int delay;
    private List<Player> players = new CopyOnWriteArrayList<>();
    private String gameName;
    private long msgSeq = 0;
    private NetworkingWithMaster networking;
    private int idForNewPlayer = 0;
    private int stateOrder = 0;
    private GameController game;
    private int myId;
    private ConcurrentHashMap<Integer, Long> idAndLastTime = new ConcurrentHashMap<>(); 
    //private ConcurrentHashMap<GameMessage, Long> messages = new ConcurrentHashMap<>();

}