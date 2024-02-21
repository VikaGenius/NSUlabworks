package snake;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class StartController {
    @FXML
    private Button createButton;

    @FXML
    private Button joinButton;

    @FXML
    public void initialize() {
        createButton.setOnAction(event -> {
            ChooseGameParametrs chooseGameParametrs = new ChooseGameParametrs();
            chooseGameParametrs.start();

        });
        joinButton.setOnAction(event -> {
            ReceiverAnnouncement normal = new ReceiverAnnouncement();
            normal.reciveAnnouncement();
        });


    }

}
