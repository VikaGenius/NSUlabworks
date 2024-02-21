package snake;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import me.ippolitov.fit.snakes.SnakesProto.GameAnnouncement;
import me.ippolitov.fit.snakes.SnakesProto.NodeRole;
import snake.ReceiverAnnouncement.Address;

import java.util.HashMap;
import java.util.Map;

public class CurrentGames {

    private Stage stage;
    private VBox buttonContainer;

    public CurrentGames() {
        stage = new Stage();
        buttonContainer = new VBox();
        buttonContainer.setFillWidth(true);
        buttonContainer.setMinSize(500, 500);
        Scene scene = new Scene(buttonContainer, 500, 500);
        stage.setScene(scene);
        stage.setResizable(false);
    }

    public void draw(HashMap<GameAnnouncement, Address> games) {
        try {
            // Очищаем старые кнопки
            buttonContainer.getChildren().clear();

            for (Map.Entry<GameAnnouncement, Address> pair : games.entrySet()) {
                String gameName = pair.getKey().getGameName();                
                Button button = new Button(gameName);
                button.autosize();
                button.setMinSize(500, 100);
                button.setOnAction(event -> buttonClickHandler(pair));

                buttonContainer.getChildren().add(button);
            }

            stage.show();  
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void buttonClickHandler(Map.Entry<GameAnnouncement, Address> pair) {
        JoinGameController optoinsController = new JoinGameController();

        Stage chooseOptionsStage = new Stage();
        chooseOptionsStage.setTitle("Game Options");

        TextField playerNameField = new TextField();
        playerNameField.setPromptText("Enter your name");

        Button joinButton = new Button("Join");
        joinButton.setMinSize(300, 100);

        Button watchButton = new Button("Watch");
        watchButton.setMinSize(300, 100);

        joinButton.setOnAction(e -> optoinsController.joinGame(pair, playerNameField.getText(), NodeRole.NORMAL));
        watchButton.setOnAction(e -> optoinsController.joinGame(pair, playerNameField.getText(), NodeRole.VIEWER));

        VBox vbox = new VBox(10); 
        vbox.getChildren().addAll(joinButton, watchButton, playerNameField);

        Scene scene = new Scene(vbox, 300, 300);

        chooseOptionsStage.setScene(scene);
        chooseOptionsStage.show();

    }
}
