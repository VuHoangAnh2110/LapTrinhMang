package client.fx;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import client.GameClient;
import client.fx.utils.SceneManager;
import shared.models.Message;
import shared.utils.Constants;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;
    @FXML private Label serverLabel;
    @FXML private ProgressIndicator progressIndicator;
    
    private GameClient client;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        client = new GameClient();
        SceneManager.getInstance().setGameClient(client);
        
        serverLabel.setText("Server: " + Constants.SERVER_HOST + ":" + Constants.SERVER_PORT);
        progressIndicator.setVisible(false);
        
        // Enter key handling
        passwordField.setOnAction(e -> handleLogin());
        
        // Add some placeholder text
        usernameField.setPromptText("Enter username");
        passwordField.setPromptText("Enter password");
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Please enter both username and password", Color.RED);
            return;
        }
        
        setButtonsEnabled(false);
        progressIndicator.setVisible(true);
        setStatus("Connecting to server...", Color.BLUE);
        
        Task<Boolean> loginTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return attemptLogin(username, password);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    if (getValue()) {
                        setStatus("Login successful!", Color.GREEN);
                        try {
                            Thread.sleep(500); // Show success message briefly
                            SceneManager.getInstance().showGameScene();
                        } catch (Exception e) {
                            setStatus("Error opening game: " + e.getMessage(), Color.RED);
                            setButtonsEnabled(true);
                        }
                    } else {
                        setStatus("Login failed. Check your credentials.", Color.RED);
                        setButtonsEnabled(true);
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    setStatus("Connection error: " + getException().getMessage(), Color.RED);
                    setButtonsEnabled(true);
                });
            }
        };
        
        new Thread(loginTask).start();
    }
    
    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Please enter both username and password", Color.RED);
            return;
        }
        
        if (password.length() < 4) {
            setStatus("Password must be at least 4 characters", Color.RED);
            return;
        }
        
        setButtonsEnabled(false);
        progressIndicator.setVisible(true);
        setStatus("Creating account...", Color.BLUE);
        
        Task<Boolean> registerTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return attemptRegister(username, password);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    if (getValue()) {
                        setStatus("Account created successfully! You can now login.", Color.GREEN);
                        usernameField.clear();
                        passwordField.clear();
                    } else {
                        setStatus("Registration failed. Username may already exist.", Color.RED);
                    }
                    setButtonsEnabled(true);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    setStatus("Registration error: " + getException().getMessage(), Color.RED);
                    setButtonsEnabled(true);
                });
            }
        };
        
        new Thread(registerTask).start();
    }
    
    private boolean attemptLogin(String username, String password) {
        try {
            System.out.println("Attempting login for: " + username);
            
            if (!client.connect(Constants.SERVER_HOST, Constants.SERVER_PORT)) {
                System.err.println("Failed to connect to server");
                return false;
            }
            
            client.setUsername(username);
            
            System.out.println("Connected to server, sending login message");
            String[] credentials = {username, password};
            Message loginMessage = new Message(Message.Type.LOGIN, credentials, username);
            client.sendMessage(loginMessage);
            
            System.out.println("Waiting for login response...");
            Message response = client.waitForResponse(10000);
            
            if (response != null) {
                System.out.println("Login response: " + response.getType());
                if (response.getType() == Message.Type.LOGIN_SUCCESS) {
                    System.out.println("Login successful for: " + username);
                    return true;
                } else {
                    System.out.println("Login failed: " + response.getMessage());
                    client.disconnect();
                    return false;
                }
            } else {
                System.err.println("No response from server");
                client.disconnect();
                return false;
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            client.disconnect();
            return false;
        }
    }
    
    private boolean attemptRegister(String username, String password) {
        try {
            System.out.println("Attempting registration for: " + username);
            
            GameClient regClient = new GameClient();
            if (!regClient.connect(Constants.SERVER_HOST, Constants.SERVER_PORT)) {
                System.err.println("Failed to connect to server for registration");
                return false;
            }
            
            String[] credentials = {username, password};
            Message registerMessage = new Message(Message.Type.REGISTER, credentials, username);
            regClient.sendMessage(registerMessage);
            
            Message response = regClient.waitForResponse(10000);
            regClient.disconnect();
            
            if (response != null) {
                System.out.println("Registration response: " + response.getType());
                return response.getType() == Message.Type.REGISTER_SUCCESS;
            } else {
                System.err.println("No response from server for registration");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setTextFill(color);
    }
    
    private void setButtonsEnabled(boolean enabled) {
        loginButton.setDisable(!enabled);
        registerButton.setDisable(!enabled);
        usernameField.setDisable(!enabled);
        passwordField.setDisable(!enabled);
    }
}