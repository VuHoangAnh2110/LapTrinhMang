package client.fx.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import shared.utils.Constants;
import client.GameClient;

public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;
    private GameClient gameClient;
    
    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }
    
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    public void setGameClient(GameClient client) {
        this.gameClient = client;
    }
    
    public GameClient getGameClient() {
        return gameClient;
    }
    
    public void showLoginScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 450, 400);
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }
    
    public void showGameScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game.fxml"));
        Scene scene = new Scene(loader.load(), Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/css/game.css").toExternalForm());
        
        // Get controller and set game client
        client.fx.GameController controller = loader.getController();
        if (gameClient != null) {
            controller.setGameClient(gameClient);
        }
        
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        
        // Request focus for key events
        scene.getRoot().requestFocus();
    }
}