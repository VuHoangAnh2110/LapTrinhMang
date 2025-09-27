import javax.swing.*; 
import java.net.*; 
 
public class Client { 
    public static void main(String[] args) throws Exception { 
        // Thiết lập Look and Feel đẹp hơn
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Sử dụng default nếu không thể thiết lập system L&F
        }
        
        String serverIP = "localhost"; 
        int serverPort = 9876; 
         
        // Chọn người chơi với dialog đẹp hơn
        String[] options = {"Người chơi 1 (X)", "Người chơi 2 (O)"}; 
        int choice = JOptionPane.showOptionDialog(null, 
                "Chọn vai trò của bạn trong game:", 
                "Game Cờ Caro - Chọn Người Chơi", 
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, options, options[0]); 
 
        String playerName = ""; 
        String mySymbol = ""; 
        String opponentSymbol = ""; 
         
        if (choice == 0) { 
            playerName = "Người chơi 1"; 
            mySymbol = "X"; 
            opponentSymbol = "O"; 
        } else if (choice == 1) { 
            playerName = "Người chơi 2"; 
            mySymbol = "O"; 
            opponentSymbol = "X"; 
        } else { 
            System.exit(0); 
        } 
         
        DatagramSocket clientSocket = new DatagramSocket(); 
        InetAddress serverAddress = InetAddress.getByName(serverIP); 
        
        System.out.println("Client port: " + clientSocket.getLocalPort());
        
        // Khởi tạo GUI với socket chung
        CaroGUI gui = new CaroGUI(serverIP, serverPort, playerName, mySymbol, opponentSymbol, clientSocket); 
        gui.setVisible(true); 
        gui.setEnabledButtons(false); 
         
        String initialMsg = "CONNECT," + mySymbol; 
        byte[] sendData = initialMsg.getBytes(); 
        DatagramPacket initialPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort); 
        clientSocket.send(initialPacket); 

        // Tạo các biến final để sử dụng trong lambda
        final CaroGUI finalGui = gui;
        final String finalMySymbol = mySymbol;
        final DatagramSocket finalClientSocket = clientSocket;
         
        // Luồng lắng nghe dữ liệu từ server 
        new Thread(() -> { 
            try { 
                while (true) { 
                    byte[] receiveData = new byte[1024]; 
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); 
                    finalClientSocket.receive(receivePacket); 
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim(); 
                     
                    System.out.println("Nhận từ Server: " + response); 
                     
                    if (response.startsWith("START_GAME")) { 
                        finalGui.setEnabledButtons(true);
                        finalGui.setStatus("Trò chơi bắt đầu! Lượt của X."); 
                        if (finalMySymbol.equals("X")) { 
                            finalGui.setMyTurn(true); 
                            finalGui.setStatus("Đến lượt bạn! Hãy đánh nước đầu tiên."); 
                        } else {
                            finalGui.setMyTurn(false);
                            finalGui.setStatus("Chờ đối thủ đánh nước đầu tiên..."); 
                        }
                    } else if (response.startsWith("UPDATE")) { 
                        String[] parts = response.split(","); 
                        int row = Integer.parseInt(parts[1]); 
                        int col = Integer.parseInt(parts[2]); 
                        String symbol = parts[3]; 
                         
                        finalGui.setSymbol(row, col, symbol); 
                        if (symbol.equals(finalMySymbol)) { 
                            finalGui.setStatus("Bạn đã đánh. Chờ đối thủ..."); 
                            finalGui.setMyTurn(false); 
                        } else { 
                            finalGui.setStatus("Đến lượt bạn!"); 
                            finalGui.setMyTurn(true); 
                        } 
                    } else if (response.startsWith("GAME_OVER")) { 
                        String[] parts = response.split(","); 
                        String result = parts[1]; 
                        if (result.equals("DRAW")) { 
                            finalGui.setStatus("Hòa! Trận đấu kết thúc với tỷ số hòa."); 
                            finalGui.showGameOverDialog("Trận đấu kết thúc hòa!\nCả hai đều chơi rất tốt!", "Kết quả - Hòa");
                        } else if (result.equals(finalMySymbol)) { 
                            finalGui.setStatus("Bạn đã thắng! Chúc mừng!"); 
                            finalGui.showGameOverDialog("Chúc mừng!\nBạn đã thắng trận đấu!", "Kết quả - Chiến thắng");
                        } else { 
                            finalGui.setStatus("Bạn đã thua. Chúc bạn may mắn lần sau!"); 
                            finalGui.showGameOverDialog("Tiếc quá!\nBạn đã thua trận này. Hãy thử lại nhé!", "Kết quả - Thất bại");
                        } 
                        finalGui.setEnabledButtons(false); 
                    } 
                } 
            } catch (Exception e) { 
                e.printStackTrace(); 
            } 
        }).start(); 
    } 
}