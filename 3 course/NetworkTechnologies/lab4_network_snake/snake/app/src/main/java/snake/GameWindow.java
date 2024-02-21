package snake;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;

public class GameWindow {
    private int tileSize = 20;
    private int width = 40;
    private int height = 40;
    private Scene scene;


    GraphicsContext gc;

    public GameWindow(String name, int width, int height, int tileSize, Controller gameController) {
        this.width = width;
        this.height = height;
        this.tileSize = tileSize;

        Stage primaryStage = new Stage();
        Canvas canvas = new Canvas((width + 10) * tileSize, height * tileSize);
        gc = canvas.getGraphicsContext2D();

        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        scene = new Scene(root, (width + 10) * tileSize, height * tileSize);
        
        scene.setOnKeyPressed(event -> {
            gameController.handleUserInput(event.getCode());
        });

        primaryStage.setTitle(name);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
  
    void setNewController(Controller gameController) {
        scene.setOnKeyPressed(event -> {
            gameController.handleUserInput(event.getCode());
        });
    }

    public void draw(List<Player> players, List<Point> foods) { 
        // Clear canvas
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, (width + 10) * tileSize, height * tileSize);

        gc.setStroke(Color.GRAY);
        for (int i = 0; i <= width; i++) {
            double x = i * tileSize;
            gc.strokeLine(x, 0, x, height * tileSize);
        }

        for (int i = 0; i <= height; i++) {
            double y = i * tileSize;
            gc.strokeLine(0, y, width * tileSize, y);
        }

        gc.setFill(Color.RED);
        for(Point food: foods) {
            gc.fillRect(food.getX() * tileSize, food.getY() * tileSize, tileSize, tileSize);
        }

        gc.setFill(Color.GREEN);
        for (Player player: players) {
            Snake snake = player.snake;
            List<Point> snakePoints = snake.getSnake();
            for (Point point : snakePoints) {
                gc.fillRect((point.getX() % width) * tileSize, (point.getY() % height) * tileSize, tileSize, tileSize);
            }
        }

        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 18));
        gc.fillText("Players Score", width * tileSize + 15, 30);

        // Рисование очков и имен игроков справа от игрового поля
        double infoX = width * tileSize + 10;
        double infoY = 70; // Увеличиваем отступ после заголовка

        for (Player player : players) {
            String playerInfo = player.getName() + ": " + player.getScore();
            gc.fillText(playerInfo, infoX, infoY);
            infoY += 20;
        }  
    }
}
