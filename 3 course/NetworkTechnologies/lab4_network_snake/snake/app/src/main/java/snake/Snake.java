package snake;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import me.ippolitov.fit.snakes.SnakesProto.GameState;
import me.ippolitov.fit.snakes.SnakesProto.GameState.Coord;
import me.ippolitov.fit.snakes.SnakesProto.Direction;
import snake.GameController.GameDirection;


public class Snake {
    public Snake(int playerId, GameDirection direction) {
        this.playerId = playerId;
        this.direction = direction;
        snake = new CopyOnWriteArrayList<>();
    }

    public Snake(GameState.Snake snakeProto) {
        this.playerId = snakeProto.getPlayerId();
        if (snakeProto.getState() == GameState.Snake.SnakeState.ALIVE) {
            this.alive = 0;
        } else {
            this.alive = 1;
        }
        this.direction = getGameDirection(snakeProto.getHeadDirection());
        this.snake = getSnakePoints(snakeProto.getPointsList());
    }

    public Point getFirst() {
        return snake.get(0);
    }

    public int getLength() {
        return snake.size();
    }

    public Point get(int index) {
        return snake.get(index);
    }

    public void add(Point point) {
        snake.add(point);
    }

    public static Snake getSnake(List<Snake> snakes, int id) {
        for (Snake snake: snakes) {
            if (snake.getPlayerId() == id) {
                return snake;
            }
        }
        return null;
    }

    public void addFirst(Point point) {
        snake.add(0, point);
    }

    public void removeLast() {
        snake.remove(snake.size() - 1);
    }

    public int getPlayerId() {
        return playerId;
    }

    public GameDirection getDirection() {
        return direction;
    }

    public void setDirection(GameDirection direction) {
        this.direction = direction;
    }

    public List<Point> getSnake() {
        return snake;
    }

    public void setSnake(List<Point> snake) {
        this.snake = snake;
    }

    public GameState.Snake getSnakeProto() {
        List<GameState.Coord> snakeCoord = new ArrayList<>();
        for(Point point: snake) {
            GameState.Coord coord = messageHandler.getCoord(point.getX(), point.getY());
            snakeCoord.add(coord);
        }
        return messageHandler.getSnake(playerId, snakeCoord, getProtoSnakeState(), getDirectionProto());
    }

    public int getAlive() {
        return alive;
    }

    public void setAlive(int alive) {
        this.alive = alive;
    }

    public GameState.Snake.SnakeState getProtoSnakeState() {
        if (alive == 0) {
            return GameState.Snake.SnakeState.ALIVE;
        } else {
            return GameState.Snake.SnakeState.ZOMBIE;
        }
    }

    public Direction getDirectionProto() {
        switch(direction) {
            case UP: return Direction.UP;
            case LEFT: return Direction.LEFT;
            case RIGHT: return Direction.RIGHT;
            case DOWN: return Direction.DOWN;
        }
        return null;
    }

    public static GameDirection getGameDirection(Direction dir) {
        switch(dir) {
            case UP: return GameDirection.UP;
            case LEFT: return GameDirection.LEFT;
            case RIGHT: return GameDirection.RIGHT;
            case DOWN: return GameDirection.DOWN;
        }
        return null;
    }

    public List<Point> getSnakePoints(List<Coord> snakeCoord) {
        List<Point> snakePoints = new ArrayList<>();
        for (Coord coord: snakeCoord) {
            snakePoints.add(new Point(coord.getX(), coord.getY()));
        }
        return snakePoints;
    }

    private int playerId;
    private GameDirection direction;
    private List<Point> snake;
    private MessageHandler messageHandler = new MessageHandler();
    private int alive = 0;
}
