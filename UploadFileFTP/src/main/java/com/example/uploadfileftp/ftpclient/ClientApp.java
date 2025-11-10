package com.example.uploadfileftp.ftpclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main Application cho FTP Client
 * Khởi tạo giao diện JavaFX
 */
public class ClientApp extends Application {
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
            ClientApp.class.getResource("Main.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 700, 600);
        
        stage.setTitle("FTP Client - Upload File");
        stage.setScene(scene);
        stage.setResizable(true); // Cho phép resize cửa sổ
        stage.setMinWidth(700);   // Kích thước tối thiểu
        stage.setMinHeight(600);
        stage.show();
    }

    @Override
    public void stop() {
        // Đảm bảo đóng kết nối khi đóng ứng dụng
        System.out.println("Ứng dụng đã đóng");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
