module com.example.uploadfileftp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.apache.commons.net;  // Apache Commons Net cho FTP client
    requires java.desktop; // Cho FileChooser

    // Mở các package cho JavaFX FXML
    opens com.example.uploadfileftp to javafx.fxml;
    opens com.example.uploadfileftp.ftpclient to javafx.fxml;
    
    // Export các package
    exports com.example.uploadfileftp;
    exports com.example.uploadfileftp.ftpclient;
    exports com.example.uploadfileftp.ftpserver;
}