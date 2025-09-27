package client.network;

import shared.network.Message;

public interface MessageListener {
    void onMessageReceived(Message message);
    
    default void onConnectionLost() {
        // Default implementation - can be overridden
        System.out.println("Connection to server lost");
    }
    
    default void onConnectionEstablished() {
        // Default implementation - can be overridden
        System.out.println("Connection to server established");
    }
}