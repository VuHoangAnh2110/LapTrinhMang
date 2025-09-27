package client.fx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import client.network.ClientConnection;
import client.network.MessageListener;
import client.fx.utils.SceneManager;
import shared.network.Message;
import shared.utils.Constants;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable, MessageListener {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button connectButton;
    @FXML private Label statusLabel;
    @FXML private TabPane tabPane;
    @FXML private Tab loginTab;
    @FXML private Tab registerTab;
    
    // Register tab fields
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private PasswordField regConfirmPasswordField;
    @FXML private TextField regEmailField;
    
    private ClientConnection client;
    private boolean isConnected = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusLabel.setText("Click Connect to start");
        
        // Disable login/register buttons initially
        loginButton.setDisable(true);
        registerButton.setDisable(true);
        
        // Setup enter key handlers
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
        
        regConfirmPasswordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleRegister();
            }
        });
        
        // Initialize client
        client = new ClientConnection();
        client.setMessageListener(this);
        
        // Set client in SceneManager
        SceneManager.getInstance().setGameClient(client);
    }
    
    @FXML
    private void handleConnect() {
        if (!isConnected) {
            statusLabel.setText("Connecting to server...");
            connectButton.setDisable(true);
            
            // Connect in background thread
            new Thread(() -> {
                boolean connected = client.connect(Constants.SERVER_HOST, Constants.SERVER_PORT);
                
                Platform.runLater(() -> {
                    if (connected) {
                        isConnected = true;
                        statusLabel.setText("Connected to server");
                        connectButton.setText("Disconnect");
                        connectButton.setDisable(false);
                        loginButton.setDisable(false);
                        registerButton.setDisable(false);
                        onConnectionEstablished();
                    } else {
                        statusLabel.setText("Failed to connect to server");
                        connectButton.setDisable(false);
                    }
                });
            }).start();
        } else {
            // Disconnect
            client.disconnect();
            isConnected = false;
            statusLabel.setText("Disconnected from server");
            connectButton.setText("Connect");
            loginButton.setDisable(true);
            registerButton.setDisable(true);
        }
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Please enter both username and password", Alert.AlertType.WARNING);
            return;
        }
        
        if (!isConnected) {
            showAlert("Please connect to server first", Alert.AlertType.WARNING);
            return;
        }
        
        statusLabel.setText("Logging in...");
        loginButton.setDisable(true);
        
        client.login(username, password);
    }
    
    @FXML
    private void handleRegister() {
        String username = regUsernameField.getText().trim();
        String password = regPasswordField.getText();
        String confirmPassword = regConfirmPasswordField.getText();
        String email = regEmailField.getText().trim();
        
        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Please enter username and password", Alert.AlertType.WARNING);
            return;
        }
        
        if (username.length() < 3) {
            showAlert("Username must be at least 3 characters long", Alert.AlertType.WARNING);
            return;
        }
        
        if (password.length() < 6) {
            showAlert("Password must be at least 6 characters long", Alert.AlertType.WARNING);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showAlert("Passwords do not match", Alert.AlertType.WARNING);
            return;
        }
        
        if (!isConnected) {
            showAlert("Please connect to server first", Alert.AlertType.WARNING);
            return;
        }
        
        statusLabel.setText("Registering...");
        registerButton.setDisable(true);
        
        client.register(username, password, email);
    }
    
    @Override
    public void onMessageReceived(Message message) {
        Platform.runLater(() -> {
            switch (message.getType()) {
                case LOGIN_SUCCESS:
                    statusLabel.setText("Login successful!");
                    // Scene change will be handled by ClientConnection
                    break;
                    
                case LOGIN_FAILED:
                    statusLabel.setText("Login failed: " + message.getMessage());
                    loginButton.setDisable(false);
                    showAlert("Login failed: " + message.getMessage(), Alert.AlertType.ERROR);
                    break;
                    
                case REGISTER_SUCCESS:
                    statusLabel.setText("Registration successful! You can now login.");
                    registerButton.setDisable(false);
                    showAlert("Registration successful! You can now login.", Alert.AlertType.INFORMATION);
                    // Switch to login tab
                    tabPane.getSelectionModel().select(loginTab);
                    // Pre-fill username
                    usernameField.setText(regUsernameField.getText());
                    break;
                    
                case REGISTER_FAILED:
                    statusLabel.setText("Registration failed: " + message.getMessage());
                    registerButton.setDisable(false);
                    showAlert("Registration failed: " + message.getMessage(), Alert.AlertType.ERROR);
                    break;
                    
                default:
                    // Ignore other message types in login screen
                    break;
            }
        });
    }
    
    @Override
    public void onConnectionLost() {
        Platform.runLater(() -> {
            isConnected = false;
            statusLabel.setText("Connection lost");
            connectButton.setText("Connect");
            connectButton.setDisable(false);
            loginButton.setDisable(true);
            registerButton.setDisable(true);
            showAlert("Connection to server lost", Alert.AlertType.ERROR);
        });
    }
    
    @Override
    public void onConnectionEstablished() {
        Platform.runLater(() -> {
            statusLabel.setText("Connected - Ready to login");
        });
    }
    
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Game Login");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Clear fields when switching tabs
    @FXML
    private void clearLoginFields() {
        usernameField.clear();
        passwordField.clear();
    }
    
    @FXML
    private void clearRegisterFields() {
        regUsernameField.clear();
        regPasswordField.clear();
        regConfirmPasswordField.clear();
        regEmailField.clear();
    }
}