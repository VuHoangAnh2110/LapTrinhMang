import java.net.*; 
import java.util.Arrays; 
 
public class Server { 
    private DatagramSocket serverSocket; 
    private byte[] receiveData = new byte[1024]; 
    private byte[] sendData = new byte[1024]; 
    private int[][] board = new int[8][8]; // 0: rỗng, 1: X, -1: O 
    private InetAddress client1IP, client2IP; 
    private int client1Port, client2Port; 
    private boolean isPlayer1Turn = true; 
    private int playersConnected = 0; 
 
    public Server(int port) throws Exception { 
        serverSocket = new DatagramSocket(port); 
        System.out.println("Server đang chạy trên cổng " + port); 
    } 
 
    public void run() throws Exception { 
        // Vòng lặp chính của Server 
        while (true) { 
            receiveData = new byte[1024]; // Reset buffer
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); 
            serverSocket.receive(receivePacket); 
            
            String message = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
            System.out.println("Nhận từ client: " + message + " from " + receivePacket.getAddress() + ":" + receivePacket.getPort());
 
            // Xử lý kết nối client 
            if (message.startsWith("CONNECT")) {
                if (playersConnected < 2) { 
                    // Đăng ký client đầu tiên 
                    if (playersConnected == 0) { 
                        client1IP = receivePacket.getAddress(); 
                        client1Port = receivePacket.getPort(); 
                        playersConnected++; 
                        String welcomeMsg = "Bạn là người chơi 1 (X). Chờ người chơi 2..."; 
                        sendData = welcomeMsg.getBytes(); 
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, client1IP, client1Port); 
                        serverSocket.send(sendPacket); 
                        System.out.println("Người chơi 1 đã kết nối: " + client1IP + ":" + client1Port);
                    }  
                    // Đăng ký client thứ hai và bắt đầu trò chơi 
                    else if (receivePacket.getPort() != client1Port || !receivePacket.getAddress().equals(client1IP)) { 
                        client2IP = receivePacket.getAddress(); 
                        client2Port = receivePacket.getPort(); 
                        playersConnected++; 
                        String welcomeMsg = "Bạn là người chơi 2 (O). Đã có đủ người. Bắt đầu trò chơi!"; 
                        sendData = welcomeMsg.getBytes(); 
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, client2IP, client2Port); 
                        serverSocket.send(sendPacket); 
                         
                        // Thông báo cho cả hai client rằng game đã bắt đầu 
                        String startMsg = "START_GAME"; 
                        sendData = startMsg.getBytes(); 
                        serverSocket.send(new DatagramPacket(sendData, sendData.length, client1IP, client1Port)); 
                        serverSocket.send(new DatagramPacket(sendData, sendData.length, client2IP, client2Port)); 
 
                        System.out.println("Người chơi 2 đã kết nối: " + client2IP + ":" + client2Port);
                        System.out.println("Đã có 2 người chơi. Bắt đầu trò chơi!"); 
                    } 
                } else {
                    // Đã đủ 2 người chơi, từ chối kết nối mới
                    String rejectMsg = "Server đã đầy. Vui lòng thử lại sau.";
                    sendData = rejectMsg.getBytes();
                    DatagramPacket rejectPacket = new DatagramPacket(sendData, sendData.length, 
                                                receivePacket.getAddress(), receivePacket.getPort());
                    serverSocket.send(rejectPacket);
                }
                continue; 
            }
            
            // Xử lý nước đi của client khi game đã bắt đầu 
            if (playersConnected == 2 && !message.startsWith("CONNECT")) {
                try {
                    String[] parts = message.split(","); 
                    if (parts.length != 2) {
                        System.out.println("Invalid move format: " + message);
                        continue;
                    }
                    
                    int row = Integer.parseInt(parts[0]); 
                    int col = Integer.parseInt(parts[1]); 
                    
                    // Kiểm tra tọa độ hợp lệ
                    if (row < 0 || row >= 8 || col < 0 || col >= 8) {
                        System.out.println("Invalid coordinates: " + row + "," + col);
                        continue;
                    }
                    
                    // Kiểm tra ô đã được đánh chưa
                    if (board[row][col] != 0) {
                        System.out.println("Cell already occupied: " + row + "," + col);
                        continue;
                    }
         
                    // Kiểm tra và xử lý logic game 
                    boolean isValidMove = false; 
                    String symbol = ""; 
                    
                    // Kiểm tra IP để xác định client (bỏ qua port vì có thể thay đổi)
                    boolean isFromClient1 = receivePacket.getAddress().equals(client1IP);
                    boolean isFromClient2 = receivePacket.getAddress().equals(client2IP);
                    
                    System.out.println("Move from IP: " + receivePacket.getAddress());
                    System.out.println("Client1 IP: " + client1IP + ", isFromClient1: " + isFromClient1);
                    System.out.println("Client2 IP: " + client2IP + ", isFromClient2: " + isFromClient2);
                    System.out.println("isPlayer1Turn: " + isPlayer1Turn);
                     
                    if (isPlayer1Turn && isFromClient1) { 
                        board[row][col] = 1; // Đánh dấu X 
                        symbol = "X"; 
                        isValidMove = true; 
                        System.out.println("Player 1 (X) played at (" + row + "," + col + ")");
                    } else if (!isPlayer1Turn && isFromClient2) { 
                        board[row][col] = -1; // Đánh dấu O 
                        symbol = "O"; 
                        isValidMove = true; 
                        System.out.println("Player 2 (O) played at (" + row + "," + col + ")");
                    } else {
                        System.out.println("Invalid move - Wrong turn or wrong player");
                        System.out.println("Expected: " + (isPlayer1Turn ? "Player 1 (X)" : "Player 2 (O)"));
                    }
                     
                    if (isValidMove) { 
                        // Gửi cập nhật đến cả hai client 
                        String updateMsg = "UPDATE," + row + "," + col + "," + symbol; 
                        sendData = updateMsg.getBytes(); 
                        serverSocket.send(new DatagramPacket(sendData, sendData.length, client1IP, client1Port)); 
                        serverSocket.send(new DatagramPacket(sendData, sendData.length, client2IP, client2Port)); 
                        
                        System.out.println("Sent UPDATE to both clients: " + updateMsg);
                         
                        // Kiểm tra điều kiện thắng/hòa 
                        if (checkWin(row, col, symbol)) { 
                            String winMsg = "GAME_OVER," + symbol; 
                            sendData = winMsg.getBytes(); 
                            serverSocket.send(new DatagramPacket(sendData, sendData.length, client1IP, client1Port)); 
                            serverSocket.send(new DatagramPacket(sendData, sendData.length, client2IP, client2Port)); 
                            System.out.println("Người chơi " + symbol + " đã thắng!"); 
                            // Reset game 
                            resetGame(); 
                        } else if (checkDraw()) { 
                            String drawMsg = "GAME_OVER,DRAW"; 
                            sendData = drawMsg.getBytes(); 
                            serverSocket.send(new DatagramPacket(sendData, sendData.length, client1IP, client1Port)); 
                            serverSocket.send(new DatagramPacket(sendData, sendData.length, client2IP, client2Port)); 
                            System.out.println("Trận đấu hòa!"); 
                            // Reset game 
                            resetGame(); 
                        } else { 
                            isPlayer1Turn = !isPlayer1Turn; // Chuyển lượt chơi 
                            System.out.println("Turn changed to: " + (isPlayer1Turn ? "Player 1 (X)" : "Player 2 (O)"));
                        } 
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Error parsing move: " + message);
                }
            }
        } 
    } 
     
    private boolean checkWin(int row, int col, String symbol) { 
        int player = symbol.equals("X") ? 1 : -1; 
        // Kiểm tra 5 quân liên tiếp theo hàng, cột, chéo 
        return checkLine(row, col, player, 1, 0) || // Hàng ngang 
               checkLine(row, col, player, 0, 1) || // Hàng dọc 
               checkLine(row, col, player, 1, 1) || // Chéo chính 
               checkLine(row, col, player, 1, -1);  // Chéo phụ 
    } 
     
    private boolean checkLine(int row, int col, int player, int rowDelta, int colDelta) { 
        int count = 1; 
        // Kiểm tra theo một hướng 
        for (int i = 1; i < 5; i++) { 
            int newRow = row + i * rowDelta; 
            int newCol = col + i * colDelta; 
            if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8 && board[newRow][newCol] == player) { 
                count++; 
            } else { 
                break; 
            } 
        } 
        // Kiểm tra theo hướng ngược lại 
        for (int i = 1; i < 5; i++) { 
            int newRow = row - i * rowDelta; 
            int newCol = col - i * colDelta; 
            if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8 && board[newRow][newCol] == player) { 
                count++; 
            } else { 
                break; 
            } 
        } 
        return count >= 5; 
    } 
     
    private boolean checkDraw() { 
        for (int i = 0; i < 8; i++) { 
            for (int j = 0; j < 8; j++) { 
                if (board[i][j] == 0) { 
                    return false; 
                } 
            } 
        } 
        return true; 
    } 
     
    private void resetGame() { 
        for (int i = 0; i < 8; i++) { 
            Arrays.fill(board[i], 0); 
        } 
        isPlayer1Turn = true; 
        playersConnected = 0; 
        client1IP = null; 
        client2IP = null; 
        System.out.println("Game đã kết thúc và được reset. Chờ người chơi mới..."); 
    } 
 
    public static void main(String[] args) throws Exception { 
        Server server = new Server(9876); // Cổng 9876 
        server.run(); 
    } 
}