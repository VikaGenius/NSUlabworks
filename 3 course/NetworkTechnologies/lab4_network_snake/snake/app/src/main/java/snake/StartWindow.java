package snake;

import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class StartWindow extends Application {

    @Override
    public void start(Stage stage) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/start_window.fxml"));
        StartController controller = new StartController();
        loader.setController(controller);
        Parent root;
        try {
            root = loader.load();
            stage.setScene(new Scene(root, 700, 700));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
