package snake;

import java.net.InetSocketAddress;

import me.ippolitov.fit.snakes.SnakesProto.GameMessage;

public class Message {
        public Message(InetSocketAddress inetAddress, GameMessage gameMessage) {
            this.socketAddress = inetAddress;
            this.gameMessage = gameMessage;
        }

        public GameMessage getGameMessage() {
            return gameMessage;
        }

        public InetSocketAddress getSocketAddress() {
            return socketAddress;
        }

        private InetSocketAddress socketAddress;
        private GameMessage gameMessage;
        
    }
