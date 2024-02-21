package snake;

import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

public class ParametrsController {
    @FXML
    private Button saveButton;

    @FXML
    private TextField widthTextField;

    @FXML
    private TextField heightTextField;

    @FXML
    private TextField foodTextField;

    @FXML
    private TextField delayTextField;

    @FXML
    private TextField nameTextField;

    @FXML
    public void initialize() {
        saveButton.setOnAction(event -> {

            int width = Integer.valueOf(widthTextField.getText());
            int height = Integer.valueOf(heightTextField.getText());
            int food = Integer.valueOf(foodTextField.getText());
            int delay = Integer.valueOf(delayTextField.getText());
            String gameName = nameTextField.getText();

            TextInputDialog nickameForm = new TextInputDialog();
            nickameForm.setTitle("Nickname form ");
            nickameForm.setHeaderText("Enter you nickname");
            nickameForm.setContentText("Nickname:");

            Optional<String> result = nickameForm.showAndWait();
            if (result.isPresent()) {
                String name = result.get();
                Master master = new Master(name);
                master.startGame(width, height, food, delay, gameName);
                //вызвать конструктор игры
            }
        });
    }
}
