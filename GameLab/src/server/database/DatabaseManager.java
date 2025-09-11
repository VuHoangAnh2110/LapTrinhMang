package server.database;

import java.sql.*;
import java.util.Properties;
import java.io.FileInputStream;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private String url = "jdbc:mysql://localhost:3306/fighting_game";
    private String username = "root";
    private String password = "";
    
    private DatabaseManager() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException ex) {
            throw new SQLException("Tao ket noi database loi: " + ex.getMessage());
        }
    }
    
    public static DatabaseManager getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseManager();
        } else if (instance.getConnection().isClosed()) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public Connection getConnection() {
        return connection;
    }
}