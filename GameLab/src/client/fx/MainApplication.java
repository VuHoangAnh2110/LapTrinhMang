package client.fx;

import javafx.application.Application;
import javafx.stage.Stage;
import client.fx.utils.SceneManager;

public class MainApplication extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager.getInstance().setPrimaryStage(primaryStage);
        SceneManager.getInstance().showLoginScene();
        
        primaryStage.setTitle("Fighting Game - JavaFX");
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}