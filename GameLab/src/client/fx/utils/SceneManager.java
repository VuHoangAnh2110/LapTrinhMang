package client.fx.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import shared.utils.Constants;

public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;
    private Object gameClient; // Can be ClientConnection or any client implementation
    
    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }
    
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    public void setGameClient(Object client) {
        this.gameClient = client;
    }
    
    public Object getGameClient() {
        return gameClient;
    }
    
    public void showLoginScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 500, 450);
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Fighting Game - Login");
        primaryStage.centerOnScreen();
    }
    
    public void showMainMenuScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(loader.load(), 600, 400);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Fighting Game - Main Menu");
        primaryStage.centerOnScreen();
    }
    
    public void showLobbyScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/lobby.fxml"));
        Scene scene = new Scene(loader.load(), 800, 600);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        
        client.fx.controllers.LobbyController controller = loader.getController();
        if (gameClient instanceof client.network.ClientConnection) {
            controller.setGameClient((client.network.ClientConnection) gameClient);
        }
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Fighting Game - Room Lobby");
        primaryStage.centerOnScreen();
    }
    
    public void showHistoryScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/history.fxml"));
        Scene scene = new Scene(loader.load(), 700, 500);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        
        client.fx.controllers.HistoryController controller = loader.getController();
        if (gameClient instanceof client.network.ClientConnection) {
            controller.setGameClient((client.network.ClientConnection) gameClient);
        }
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Fighting Game - Game History");
        primaryStage.centerOnScreen();
    }
    
    public void showGameScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game.fxml"));
        Scene scene = new Scene(loader.load(), Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/css/game.css").toExternalForm());
        
        client.fx.controllers.GameController controller = loader.getController();
        if (gameClient instanceof client.network.ClientConnection) {
            controller.setGameClient((client.network.ClientConnection) gameClient);
        }
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Fighting Game - Battle Arena");
        primaryStage.centerOnScreen();
        primaryStage.setResizable(false);
        
        // Focus on the scene for key events
        scene.getRoot().requestFocus();
    }
}