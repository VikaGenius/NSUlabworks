package snake;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;
import me.ippolitov.fit.snakes.SnakesProto.GameConfig;
import me.ippolitov.fit.snakes.SnakesProto.GameState;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CopyOnWriteArrayList;


public class GameController implements Controller {
    public GameController(GameConfig config, String name, Player player) {
        this.name = name;
        this.width = config.getWidth();
        this.height = config.getHeight();
        this.foodStatic = config.getFoodStatic();
        this.delay = config.getStateDelayMs();
        this.masterId = player.getId();
        //players.add(player);

        initializeNewSnake(player);
    }

    public GameController(GameWindow game, List<Player> players, List<Point> foods, GameConfig config, String name, int masterId) {
        this.players = players;
        this.foods = foods;
        this.name = name;
        this.width = config.getWidth();
        this.height = config.getHeight();
        this.foodStatic = config.getFoodStatic();
        this.delay = config.getStateDelayMs();
        gameWindow = game;
        this.masterId = masterId;
    }

    public void start(Master master) {
        gameWindow = new GameWindow(name, width, height, tileSize, this);
        spawnFood(foods, foodStatic + players.size());
        
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(delay), event -> {
            moveSnake();
            checkCollision();
            checkFood();
            gameWindow.draw(players, foods);
            master.sendState();
            System.out.println(System.currentTimeMillis()); 

        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void continueGame(Master master) {
        gameWindow.setNewController(this);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                moveSnake();
                checkCollision();
                checkFood();
                gameWindow.draw(players, foods);
                master.sendState();  
                System.out.println(System.currentTimeMillis()); 
            }
        };

        // Запуск задачи с фиксированным интервалом
        scheduler.scheduleAtFixedRate(task, 0, delay, TimeUnit.MILLISECONDS);

        /*Timeline timeline = new Timeline(new KeyFrame(Duration.millis(delay), event -> {
            //System.out.println("==========================================================");

            moveSnake();
            checkCollision();
            checkFood();
            gameWindow.draw(players, foods);

            //System.out.println("==========================================================");
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();*/
    }


    private void moveSnake() {
        
        for (Player player: players) {
            Snake snake = player.snake;
            Point head = snake.getFirst();
            Point newHead;

            GameDirection direction = snake.getDirection();

            switch (direction) {
                case UP:
                    newHead = new Point(head.getX() % width, (head.getY() - 1 + height) % height);
                    break;
                case DOWN:
                    newHead = new Point(head.getX() % width, (head.getY() % height) + 1);
                    break;
                case LEFT:
                    newHead = new Point((head.getX() - 1 + width) % width, head.getY() % height);
                    break;
                case RIGHT:
                    newHead = new Point((head.getX() % width) + 1, head.getY() % height);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + direction);
            }

            snake.addFirst(newHead);

            if (!hasFood(newHead)) {
                snake.removeLast();
            }
            // else {
        //     player.setScore(player.getScore() + 1);
        // }

            player.snake = snake;

        }
        
    }

    public boolean initializeNewSnake(Player player) {
        Point emptySquare = findEmptySquare();

        Snake snake = new Snake(player.getId(), null);
        
        if (emptySquare != null) {
            // Создаем новую змейку длиной 2 клетки
            Point head = emptySquare;
            Point tail = getRandomNeighbor(head);
    
            snake.add(head);
            snake.add(tail);
    
            // Устанавливаем направление движения змейки
            snake.setDirection(setInitialDirection(head, tail));
            //snakes.add(snake);
            player.snake = snake;
            players.add(player);
            return true;
        } else {
            // Обработка ситуации, когда не удалось найти свободный квадрат
            System.out.println("Cannot initialize new snake. No empty square found.");
            return false;
        }
    }
    
    private Point getRandomNeighbor(Point point) {
        Random random = new Random();
        int neighborX = (point.getX() + random.nextInt(3) - 1 + width) % width;
        int neighborY = (point.getY() + random.nextInt(3) - 1 + height) % height;
        return new Point(neighborX, neighborY);
    }
    
    private GameDirection setInitialDirection(Point head, Point tail) {
        GameDirection direction;
        // Устанавливаем направление движения змейки противоположно выбранному направлению хвоста
        if (head.getX() < tail.getX()) {
            direction = GameDirection.RIGHT;
        } else if (head.getX() > tail.getX()) {
            direction = GameDirection.LEFT;
        } else if (head.getY() < tail.getY()) {
            direction = GameDirection.DOWN;
        } else {
            direction = GameDirection.UP;
        }

        return direction;
    }

    private Point findEmptySquare() {
        Random random = new Random();
    
        for (int attempt = 0; attempt < 100; attempt++) { // 100 попыток на случай, если все позиции заняты
            int x = random.nextInt(width);
            int y = random.nextInt(height);
    
            Point topLeft = new Point(x, y);
    
            if (isSquareEmpty(topLeft)) {
                return topLeft;
            }
        }
        // Возвращаем null, если не удалось найти подходящий квадрат
        return null;
    }
    
    private boolean isSquareEmpty(Point topLeft) {
        // Проверяем, что в каждой клетке квадрата нет еды и змейки
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                int x = (topLeft.getX() + i + width) % width; // Обрабатываем "тороидальность" по горизонтали
                int y = (topLeft.getY() + j + height) % height; // Обрабатываем "тороидальность" по вертикали
    
                Point currentPoint = new Point(x, y);
                
                if (hasFood(currentPoint)) {
                    return false;
                }
                for (Player player: players) {
                    Snake snake = player.snake;
                    if (snake.getSnake().contains(currentPoint)) {
                        return false;
                    }
                }
                
            }
        }
    
        return true; // Пусто
    }

    private boolean hasFood(Point point) {
        for (Point food: foods) {
            if (point.equals(food)) {
                return true;
            }
        }
        return false;
    }

    private void checkCollision() {
        List<Player> removeSnakes = new ArrayList<>();
        for (Player player: players) {
            Snake snake = player.snake;
            Point head = snake.getFirst();
            for (Player player1: players) {
                Snake snake1 = player1.snake;
                for (int i = 1; i < snake1.getLength(); i++) {
                    if (head.equals(snake1.get(i))) {
                        player1.setScore(player1.getScore() + 1);
                        removeSnakes.add(player);
                    }
                }
            }
        }  

        for(Player player: removeSnakes) {
            Snake snake = player.snake;
            //функция, которая превращает змейки в еду
            changeSnakeOnFoods(snake.getSnake());
            players.remove(player);
        }
    }

    void changeSnakeOnFoods(List<Point> snake) {
        Random random = new Random();
        for (Point point: snake) {
            int a = random.nextInt(2);
            if (a == 1) {
                foods.add(point);
            }
        }
    }

    private Point randomPoint() {
        Random random = new Random();
        int x = random.nextInt(width);
        int y = random.nextInt(height);
        Point point = new Point(x, y);

        for (Player player: players) {
            Snake snake = player.snake;
            if (snake.getSnake().contains(point)) {
                point = randomPoint();
            }
        }

        return point;
    }

    private void spawnFood(List<Point> manyFood, int count) {
        for(int i = 0; i < count; i++) {
            manyFood.add(randomPoint());
        }
    }

    private void checkFood() {
        List<Point> newFoods = new ArrayList<>();
        List<Point> removeFoods = new ArrayList<>();
        for (Player player: players) {
            Snake snake = player.snake;
            Point head = snake.getFirst();
            for (Point food: foods) {
                if (head.equals(food)) {
                    //food = randomPoint();
                    removeFoods.add(food);
                    player.setScore(player.getScore() + 1);
                    if (foods.size() - removeFoods.size() < foodStatic + players.size()) {
                        spawnFood(newFoods, 1);
                    } 
                }
            }     
        } 
        foods.addAll(newFoods);
        foods.removeAll(removeFoods);
    }

    @Override
    public void handleUserInput(KeyCode code) {
        Player player = Player.getPlayer(players, masterId);
        
        if (player != null) {
            GameDirection direction = player.snake.getDirection();
            if (code == KeyCode.UP && direction != GameDirection.DOWN) {
                direction = GameDirection.UP;
            } else if (code == KeyCode.DOWN && direction != GameDirection.UP) {
                direction = GameDirection.DOWN;
            } else if (code == KeyCode.LEFT && direction != GameDirection.RIGHT) {
                direction = GameDirection.LEFT;
            } else if (code == KeyCode.RIGHT && direction != GameDirection.LEFT) {
                direction = GameDirection.RIGHT;
            }
        
            player.snake.setDirection(direction);
        }
        
    }

    public void ChangeDirection(int snakeId, GameDirection direction) {
        Player player = Player.getPlayer(players, snakeId);
        if (player != null) {
            player.snake.setDirection(direction);
        }
        /*for (Snake snake: snakes) {
            if (snake.getPlayerId() == snakeId) {
                snake.setDirection(direction);
            }
        }*/
    }

    public void deleteSnake(int id) {
        Player player = Player.getPlayer(players, id);
        /*for (Snake snake: snakes) {
            if (snake.getPlayerId() == id) {
                snakes.remove(snake);
            }
        }*/
        players.remove(player);
    }

    public List<GameState.Snake> getSnakes() {
        List<GameState.Snake> snakesProto = new ArrayList<>(); 
        for (Player player: players) {
            Snake snake = player.snake;
            snakesProto.add(snake.getSnakeProto());
        }
        return snakesProto;
    }

    public List<GameState.Coord> getFoods() {
        List<GameState.Coord> foodsProto = new ArrayList<>();
        for (Point point: foods) {
            foodsProto.add(messageHandler.getCoord(point.getX(), point.getY()));
        }

        return foodsProto;
    }

    public enum GameDirection {
        UP, DOWN, LEFT, RIGHT
    }

    private String name;
    private int tileSize = 20;
    private int width = 40;
    private int height = 40;
    private int foodStatic = 1;
    private int delay = 150;

    //private List<Snake> snakes = new CopyOnWriteArrayList<>();
    private List<Point> foods = new CopyOnWriteArrayList<>();
    public List<Player> players = new CopyOnWriteArrayList<>();
    private GameWindow gameWindow;
    private MessageHandler messageHandler = new MessageHandler();

    int masterId;

    
}
