package client;

import shared.models.Message;
import shared.utils.Constants;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private MainMenu parent;
    
    public LoginPanel(MainMenu parent) {
        this.parent = parent;
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Title
        JLabel titleLabel = new JLabel("Fighting Game Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; 
        gbc.insets = new Insets(20, 20, 20, 20);
        add(titleLabel, gbc);
        
        // Username
        gbc.gridwidth = 1; gbc.insets = new Insets(5, 20, 5, 5);
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Username:"), gbc);
        
        usernameField = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 20);
        add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 20, 5, 5);
        add(new JLabel("Password:"), gbc);
        
        passwordField = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 20);
        add(passwordField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        
        loginButton.addActionListener(new LoginActionListener());
        registerButton.addActionListener(new RegisterActionListener());
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 10, 20);
        add(buttonPanel, gbc);
        
        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 20, 20, 20);
        add(statusLabel, gbc);
        
        // Server info
        JLabel serverLabel = new JLabel("Server: " + Constants.SERVER_HOST + ":" + Constants.SERVER_PORT);
        serverLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        serverLabel.setForeground(Color.GRAY);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 20, 10, 20);
        add(serverLabel, gbc);
        
        // Enter key listeners
        usernameField.addActionListener(e -> passwordField.requestFocus());
        passwordField.addActionListener(e -> loginButton.doClick());
    }
    
    private class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                setStatus("Please enter both username and password", Color.RED);
                return;
            }
            
            setButtonsEnabled(false);
            setStatus("Connecting...", Color.BLUE);
            
            // Connect and login in background thread
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return parent.attemptLogin(username, password);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            setStatus("Login successful!", Color.GREEN);
                            parent.openGameWindow();
                        } else {
                            setStatus("Login failed. Check credentials.", Color.RED);
                            setButtonsEnabled(true);
                        }
                    } catch (Exception ex) {
                        setStatus("Connection error: " + ex.getMessage(), Color.RED);
                        setButtonsEnabled(true);
                    }
                }
            };
            worker.execute();
        }
    }
    
    private class RegisterActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                setStatus("Please enter both username and password", Color.RED);
                return;
            }
            
            if (password.length() < 4) {
                setStatus("Password must be at least 4 characters", Color.RED);
                return;
            }
            
            setButtonsEnabled(false);
            setStatus("Creating account...", Color.BLUE);
            
            // Register in background thread
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return parent.attemptRegister(username, password);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            setStatus("Account created! You can now login.", Color.GREEN);
                        } else {
                            setStatus("Registration failed. Username may exist.", Color.RED);
                        }
                        setButtonsEnabled(true);
                    } catch (Exception ex) {
                        setStatus("Connection error: " + ex.getMessage(), Color.RED);
                        setButtonsEnabled(true);
                    }
                }
            };
            worker.execute();
        }
    }
    
    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }
    
    private void setButtonsEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
        registerButton.setEnabled(enabled);
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
    }
    
    public void resetForm() {
        usernameField.setText("");
        passwordField.setText("");
        statusLabel.setText(" ");
        setButtonsEnabled(true);
    }
}