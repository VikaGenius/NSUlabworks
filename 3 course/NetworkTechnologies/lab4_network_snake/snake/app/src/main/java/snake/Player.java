package snake;

import me.ippolitov.fit.snakes.SnakesProto.GamePlayer;
import me.ippolitov.fit.snakes.SnakesProto.NodeRole;

import java.util.List;


public class Player {
    
    public Player(String name, NodeRole role, String ipAddress, int port, int id) {
        this.name = name;
        this.role = role;
        this.ipAddress = ipAddress;
        this.port = port;
        this.id = id;
    } 

    public Player(GamePlayer player) {
        this.name = player.getName();
        this.id = player.getId();
        this.role = player.getRole();
        this.ipAddress = player.getIpAddress();
        this.port = player.getPort();
        this.score = player.getScore();

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NodeRole getRole() {
        return role;
    }

    public void setRole(NodeRole role) {
        this.role = role;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    
      public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static Player getPlayer(List<Player> players, int id) {
        for (Player player: players) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }

    private int id;
    private String name;
    private NodeRole role;
    private int score;
    private String ipAddress;
    private int port;
    public Snake snake;


}
