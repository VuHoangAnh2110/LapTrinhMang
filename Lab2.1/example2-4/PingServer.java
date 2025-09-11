import java.io.*;
import java.net.*;
import java.util.Scanner;

public class PingServer {
    private static final int DEFAULT_PORT = 7; // Echo port
    private static final int TIMEOUT = 5000; // 5 giây timeout
    private static final String PING_MESSAGE = "hello";
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("╔═════════════════════════════════════════");
        System.out.println("║            PING SERVER TOOL             ");
        System.out.println("╚═════════════════════════════════════════");
        System.out.println();
        
        while (true) {
            System.out.print("Nhap dia chi may chu (hoac 'exit' de thoat): ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Thoat chuong trinh. Tam biet!");
                break;
            }
            
            if (input.isEmpty()) {
                System.out.println("Vui long nhap dia chi may chu!");
                continue;
            }
            
            // Tách host và port nếu có
            String host;
            int port = DEFAULT_PORT;
            
            if (input.contains(":")) {
                String[] parts = input.split(":");
                host = parts[0];
                try {
                    port = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    System.out.println("Port khong hop le! Su dung port mac dinh: " + DEFAULT_PORT);
                }
            } else {
                host = input;
            }
            
            System.out.println("\n Dang ping den " + host + ":" + port + "...");
            pingServer(host, port);
            System.out.println();
        }
        
        scanner.close();
    }
    
    public static void pingServer(String host, int port) {
        // Phương pháp 1: Ping bằng Socket (TCP)
        boolean tcpSuccess = pingWithSocket(host, port);
        
        // // Phương pháp 2: Ping bằng InetAddress (ICMP - nếu có quyền)
        // boolean icmpSuccess = pingWithInetAddress(host);
        
        // // Phương pháp 3: Ping bằng HTTP (nếu là web server)
        // boolean httpSuccess = false;
        // if (port == 80 || port == 8080) {
        //     httpSuccess = pingWithHTTP(host, port);
        // }
        
        // Kết quả tổng hợp
        // displayResults(host, port, tcpSuccess, icmpSuccess, httpSuccess);
        displayResults(host, port, tcpSuccess);
    }
    
    private static boolean pingWithSocket(String host, int port) {
        System.out.println(" Thu ket noi TCP den " + host + ":" + port + "...");
        
        try (Socket socket = new Socket()) {
            long startTime = System.currentTimeMillis();
            
            // Kết nối với timeout
            socket.connect(new InetSocketAddress(host, port), TIMEOUT);
            
            // Gửi thông điệp "hello"
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            out.println(PING_MESSAGE);
            
            // Đọc phản hồi (nếu có)
            String response = null;
            try {
                socket.setSoTimeout(2000); // Timeout cho việc đọc
                response = in.readLine();
            } catch (SocketTimeoutException e) {
                // Không có phản hồi, nhưng kết nối thành công
            }
            
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            
            System.out.println(" TCP ket noi thanh cong!");
            System.out.println(" Da gui: \"" + PING_MESSAGE + "\"");
            System.out.println(" Nhan duoc: \"" + response + "\"");
            if (response != null && !response.trim().isEmpty()) {
                System.out.println(" Nhan duoc: \"" + response + "\"");
            } else {
                System.out.println(" Khong nhan duoc phan hoi (binh thuong voi hau het dich vu)");
            }
            
            System.out.println(" Thoi gian phan hoi: " + responseTime + "ms");
            return true;
            
        } catch (UnknownHostException e) {
            System.out.println(" Khong the tim thay host: " + host);
            return false;
        } catch (ConnectException e) {
            System.out.println(" Tu choi ket noi - Port " + port + " co the dang dong");
            return false;
        } catch (SocketTimeoutException e) {
            System.out.println(" Timeout - Host khong phan hoi trong " + TIMEOUT + "ms");
            return false;
        } catch (IOException e) {
            System.out.println(" Loi ket noi: " + e.getMessage());
            return false;
        }
    }
    
    // private static boolean pingWithInetAddress(String host) {
    //     System.out.println(" Thử ping ICMP đến " + host + "...");
        
    //     try {
    //         InetAddress address = InetAddress.getByName(host);
    //         long startTime = System.currentTimeMillis();
            
    //         boolean reachable = address.isReachable(TIMEOUT);
            
    //         long endTime = System.currentTimeMillis();
    //         long responseTime = endTime - startTime;
            
    //         if (reachable) {
    //             System.out.println(" ICMP ping thành công!");
    //             System.out.println(" Thời gian phản hồi: " + responseTime + "ms");
    //             System.out.println(" Địa chỉ IP: " + address.getHostAddress());
    //             return true;
    //         } else {
    //             System.out.println(" ICMP ping thất bại - Host không phản hồi");
    //             return false;
    //         }
            
    //     } catch (UnknownHostException e) {
    //         System.out.println(" Không thể phân giải tên miền: " + host);
    //         return false;
    //     } catch (IOException e) {
    //         System.out.println(" Lỗi ICMP ping: " + e.getMessage());
    //         return false;
    //     }
    // }
    
    // private static boolean pingWithHTTP(String host, int port) {
    //     System.out.println(" Thử kết nối HTTP đến " + host + ":" + port + "...");
        
    //     try {
    //         String protocol = (port == 443) ? "https" : "http";
    //         URL url = new URL(protocol + "://" + host + ":" + port + "/");
            
    //         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    //         connection.setRequestMethod("GET");
    //         connection.setConnectTimeout(TIMEOUT);
    //         connection.setReadTimeout(TIMEOUT);
    //         connection.setRequestProperty("User-Agent", "Java Ping Tool");
            
    //         long startTime = System.currentTimeMillis();
    //         int responseCode = connection.getResponseCode();
    //         long endTime = System.currentTimeMillis();
    //         long responseTime = endTime - startTime;
            
    //         System.out.println(" HTTP kết nối thành công!");
    //         System.out.println(" Đã gửi HTTP GET request");
    //         System.out.println(" Mã phản hồi: " + responseCode + " " + connection.getResponseMessage());
    //         System.out.println("⏱ Thời gian phản hồi: " + responseTime + "ms");
            
    //         connection.disconnect();
    //         return true;
            
    //     } catch (Exception e) {
    //         System.out.println(" HTTP ping thất bại: " + e.getMessage());
    //         return false;
    //     }
    // }
    
    // private static void displayResults(String host, int port, boolean tcpSuccess, boolean icmpSuccess, boolean httpSuccess) {
    private static void displayResults(String host, int port, boolean tcpSuccess) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println(" KET QUA PING - " + host + ":" + port);
        System.out.println("=".repeat(50));
        
        // Kết quả tổng quát
        boolean anySuccess = tcpSuccess;
        
        if (anySuccess) {
            System.out.println(" Trang thai: DANG HOAT DONG");
            System.out.println(" Server dang phan hoi thanh cong!");
        } else {
            System.out.println(" Trang thai: TAT KET NOI");
            System.out.println(" Server khong phan hoi hoac khong kha dung!");
        }
        
        System.out.println("\nChi tiet:");
        System.out.println("• TCP Socket: " + (tcpSuccess ? " Thanh cong" : " That bai"));
        // System.out.println("• ICMP Ping:  " + (icmpSuccess ? " Thành công" : " Thất bại"));
        
        // if (port == 80 || port == 8080 || port == 443) {
        //     System.out.println("• HTTP Ping:  " + (httpSuccess ? " Thành công" : " Thất bại"));
        // }
        
        System.out.println("=".repeat(50));
        
        // // Gợi ý sử dụng
        // if (!anySuccess) {
        //     System.out.println("\n Gợi ý:");
        //     System.out.println("• Kiểm tra lại địa chỉ host");
        //     System.out.println("• Thử port khác (ví dụ: " + host + ":80, " + host + ":443)");
        //     System.out.println("• Kiểm tra kết nối mạng của bạn");
        //     System.out.println("• Server có thể đang bảo trì hoặc chặn ping");
        // }
    }
}