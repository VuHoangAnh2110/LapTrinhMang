package com.example.uploadfileftp;

import com.example.uploadfileftp.ftpclient.ClientApp;
import javafx.application.Application;

/**
 * Launcher class để khởi động JavaFX application
 * Được sử dụng khi run từ Maven
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(ClientApp.class, args);
    }
}
