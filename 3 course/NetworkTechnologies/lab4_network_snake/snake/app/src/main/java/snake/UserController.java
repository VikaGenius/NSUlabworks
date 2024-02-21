package snake;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javafx.scene.input.KeyCode;
import me.ippolitov.fit.snakes.SnakesProto.Direction;
import me.ippolitov.fit.snakes.SnakesProto.GameAnnouncement;
import me.ippolitov.fit.snakes.SnakesProto.GameConfig;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage;
import me.ippolitov.fit.snakes.SnakesProto.GamePlayer;
import me.ippolitov.fit.snakes.SnakesProto.GameState;
import me.ippolitov.fit.snakes.SnakesProto.NodeRole;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage.PingMsg;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage.RoleChangeMsg;
import me.ippolitov.fit.snakes.SnakesProto.GameMessage.SteerMsg;
import snake.GameController.GameDirection;
import snake.ReceiverAnnouncement.Address;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserController implements Controller {
    UserController(GameAnnouncement gameData, int id, DatagramSocket socket, Address masterAddress, NodeRole role) {
        this.role = role;
        this.id = id;
        this.socket = socket;
        config = gameData.getConfig();
        gameName = gameData.getGameName();
        this.masterAddress = masterAddress;
        this.delay = config.getStateDelayMs();
        timeLastMsg.set(System.currentTimeMillis());
        masterAlive.set(System.currentTimeMillis());
    }


    public void start() {
        gameWindow = new GameWindow(gameName, config.getWidth(), config.getHeight(), 20, this);
        receiveMessageHandle();
        checkTimeLastMsg();
        checkSendMsg();
    }

    void receiveMessageHandle() {
        Thread receiver = new Thread (() -> {
            while (true) {
                if (role == NodeRole.MASTER) {
                    break;
                }
                GameMessage gameMessage = receiveMessage();
                if (gameMessage.hasState()) {
                    handleState(gameMessage);
                }
                if (gameMessage.hasRoleChange()) {
                    handleRoleChange(gameMessage);
                }
                if (gameMessage.hasAck()) {
                    long msgSeq = gameMessage.getMsgSeq();
                    messages.forEach((key, value) -> {
                        if (key.getMsgSeq() == msgSeq) {
                            messages.remove(key);
                        }
                    });
                }
            }
        });
        receiver.start();
    }

    void checkSendMsg() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                for (Map.Entry<GameMessage, Long> pair : messages.entrySet()) {
                    if (System.currentTimeMillis() - pair.getValue() > (double)delay * 0.5) {
                        sendMessage(pair.getKey());
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, delay);
    }

    void handleState(GameMessage gameMessage) {
        GameMessage ack = messageHandler.getGameMessageAck(gameMessage.getMsgSeq(), id, gameMessage.getSenderId(), messageHandler.getAckMsg());
        sendMessage(ack);
        GameState state = gameMessage.getState().getState();
        stateOrder = Math.max(stateOrder, state.getStateOrder());
            if (state.getStateOrder() >= stateOrder) {
                List<Snake> snakes = new ArrayList<>();
                List<GameState.Snake> snakesProto = state.getSnakesList();
                for (GameState.Snake snakeProto: snakesProto) {
                    snakes.add(new Snake(snakeProto));
                }

                findMySnake(snakes);

                List<Point> foods = new ArrayList<>();
                List<GameState.Coord> foodsProto = state.getFoodsList();
                for (GameState.Coord coord: foodsProto) {
                    foods.add(new Point(coord.getX(), coord.getY()));
                }

                allFoods = foods;
                //allSnakes = snakes;

                List<GamePlayer> gamePlayers = state.getPlayers().getPlayersList();
                List<Player> newPlayers = new ArrayList<>();
                for (GamePlayer gamePlayer: gamePlayers) {
                    Player player = new Player(gamePlayer);
                    player.snake = Snake.getSnake(snakes, player.getId());
                    newPlayers.add(player);
                }
                players = newPlayers;
                gameWindow.draw(players, foods);
                
            }
    }

    void handleRoleChange(GameMessage gameMessage) {
        RoleChangeMsg msg = gameMessage.getRoleChange();
        if (role != NodeRole.MASTER && msg.getReceiverRole() == NodeRole.DEPUTY) {
            role = NodeRole.DEPUTY;
        }

        if (msg.getSenderRole() == NodeRole.MASTER && msg.getReceiverRole() != NodeRole.DEPUTY) {
            role = msg.getReceiverRole();
        }
    }

    void findMySnake(List<Snake> snakes) {
        for (Snake snake: snakes) {
            if (snake.getPlayerId() == id) {
                mySnake = snake;
            }
        }
    }
    
    @Override
    public void handleUserInput(KeyCode code) {
        if (role != NodeRole.VIEWER) {
            Direction direction = null;
            GameDirection myDirection = mySnake.getDirection();
            if (code == KeyCode.UP && myDirection != GameDirection.DOWN) {
                direction = Direction.UP;
            } else if (code == KeyCode.DOWN && myDirection != GameDirection.UP) {
                direction = Direction.DOWN;
            } else if (code == KeyCode.LEFT && myDirection != GameDirection.RIGHT) {
                direction = Direction.LEFT;
            } else if (code == KeyCode.RIGHT && myDirection != GameDirection.LEFT) {
                direction = Direction.RIGHT;
            } 

            if (direction != null) {
                SteerMsg steerMsg = messageHandler.getSteerMsg(direction);
                GameMessage message = messageHandler.getGameMessageSteer(msgSeq, id, 0, steerMsg);
                msgSeq++;
                sendMessage(message);
                messages.put(message, System.currentTimeMillis());
            }
        }
    }
        
    void checkTimeLastMsg() {
        Thread checker = new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (role == NodeRole.MASTER) {
                    break;
                }
                if (System.currentTimeMillis() - timeLastMsg.get() > ((double)delay * 0.5)) {
                    PingMsg ping = messageHandler.getPingMsg();
                    GameMessage message = messageHandler.getGameMessagePing(msgSeq, id, masterId, ping);
                    sendMessage(message);
                    messages.put(message, System.currentTimeMillis());
                    msgSeq++;
                }

                if (System.currentTimeMillis() - masterAlive.get() > (double)delay * 4.0) {
                    if (role == NodeRole.DEPUTY) {
                        role = NodeRole.MASTER;
                        GameController gameController = new GameController(gameWindow, players, allFoods, config, gameName, id);
                        Master master = new Master(socket, id, gameName, config, gameController, stateOrder);
                        master.changeRole(players);
                        master.continueLogic();
                    }
                }
            }
        });
        checker.start();
    }

    void sendMessage(GameMessage msg) {
        try {
            byte[] message = msg.toByteArray();
            DatagramPacket packet = new DatagramPacket(message, message.length, masterAddress.getInetAddress(), masterAddress.getPort());
            socket.send(packet);
            timeLastMsg.set(System.currentTimeMillis());
            
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
            
            masterAlive.set(System.currentTimeMillis());
            masterAddress.setInetAddress(recv.getAddress());
            masterAddress.setPort(recv.getPort());
            return messageHandler.parseMessage(bytes);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

    private int id;
    private DatagramSocket socket;
    private MessageHandler messageHandler = new MessageHandler();
    private GameWindow gameWindow;
    private String gameName;
    private GameConfig config;
    private int delay;
    private Snake mySnake;
    private List<Point> allFoods;
    private Address masterAddress;
    private int msgSeq = 1;
    private AtomicLong timeLastMsg = new AtomicLong();
    private AtomicLong masterAlive = new AtomicLong();
    volatile private int masterId = 0;
    private int stateOrder = -1;
    volatile private NodeRole role;
    private List<Player> players = new CopyOnWriteArrayList<>();
    private ConcurrentHashMap<GameMessage, Long> messages = new ConcurrentHashMap<>();
}
