package snake;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChooseGameParametrs {
    public void start() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/game_parametrs.fxml"));
        ParametrsController controller = new ParametrsController();
        loader.setController(controller);
        Parent root;
        Stage stage = new Stage();
        try {
            root = loader.load();
            stage.setScene(new Scene(root, 700, 700));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
}
