import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class PortScanner {
    private static final String DEFAULT_HOST = "localhost";
    private static final int START_PORT = 0;
    private static final int END_PORT = 1024;
    private static final int TIMEOUT = 1000; // 1 giây timeout
    private static final int THREAD_POOL_SIZE = 50; // Số lượng thread đồng thời
    
    // Danh sách các port phổ biến và dịch vụ tương ứng
    private static final Map<Integer, String> WELL_KNOWN_PORTS = new HashMap<>();
    
    static {
        WELL_KNOWN_PORTS.put(21, "FTP");
        WELL_KNOWN_PORTS.put(22, "SSH");
        WELL_KNOWN_PORTS.put(23, "Telnet");
        WELL_KNOWN_PORTS.put(25, "SMTP");
        WELL_KNOWN_PORTS.put(53, "DNS");
        WELL_KNOWN_PORTS.put(80, "HTTP");
        WELL_KNOWN_PORTS.put(110, "POP3");
        WELL_KNOWN_PORTS.put(143, "IMAP");
        WELL_KNOWN_PORTS.put(443, "HTTPS");
        WELL_KNOWN_PORTS.put(993, "IMAPS");
        WELL_KNOWN_PORTS.put(995, "POP3S");
        WELL_KNOWN_PORTS.put(3306, "MySQL");
        WELL_KNOWN_PORTS.put(3389, "RDP");
        WELL_KNOWN_PORTS.put(5432, "PostgreSQL");
        WELL_KNOWN_PORTS.put(8080, "HTTP-Alt");
    }
    
    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        
        // Xử lý tham số dòng lệnh
        if (args.length > 0) {
            host = args[0];
        }
        
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║           PORT SCANNER TOOL              ");
        System.out.println("╠══════════════════════════════════════════");
        System.out.println("║ Dang quet cac port tu " + START_PORT + " den " + END_PORT + "");
        System.out.println("║ Target: " + String.format("%-30s", host) + " ");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
        
        PortScanner scanner = new PortScanner();
        scanner.scanPorts(host, START_PORT, END_PORT);
    }
    
    public void scanPorts(String host, int startPort, int endPort) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Future<PortResult>> futures = new ArrayList<>();
        List<PortResult> openPorts = Collections.synchronizedList(new ArrayList<>());
        
        long startTime = System.currentTimeMillis();
        
        // Tạo tasks cho mỗi port
        for (int port = startPort; port <= endPort; port++) {
            final int currentPort = port;
            Future<PortResult> future = executor.submit(() -> scanSinglePort(host, currentPort));
            futures.add(future);
        }
        
        // Thu thập kết quả và hiển thị progress
        int completed = 0;
        int totalPorts = endPort - startPort + 1;
        
        for (Future<PortResult> future : futures) {
            try {
                PortResult result = future.get();
                if (result.isOpen()) {
                    openPorts.add(result);
                    System.out.printf("✓ Port %d/tcp OPEN  - %s%n", 
                                    result.getPort(), result.getService());
                }
                
                completed++;
                
                // Hiển thị progress mỗi 100 port
                if (completed % 100 == 0 || completed == totalPorts) {
                    double progress = (double) completed / totalPorts * 100;
                    System.out.printf("\rProgress: %.1f%% (%d/%d ports scanned)", 
                                    progress, completed, totalPorts);
                    if (completed < totalPorts) {
                        System.out.print("\r");
                    } else {
                        System.out.println();
                    }
                }
                
            } catch (Exception e) {
                System.err.println("Loi khi quet port: " + e.getMessage());
            }
        }
        
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        double duration = (endTime - startTime) / 1000.0;
        
        // Hiển thị kết quả tổng kết
        displaySummary(host, openPorts, totalPorts, duration);
    }
    
    private PortResult scanSinglePort(String host, int port) {
        try (Socket socket = new Socket()) {
            // Thiết lập timeout cho kết nối
            socket.connect(new InetSocketAddress(host, port), TIMEOUT);
            
            // Nếu kết nối thành công, port đang mở
            String service = getServiceName(port);
            return new PortResult(port, true, service);
            
        } catch (IOException e) {
            // Kết nối thất bại, port đóng hoặc không có dịch vụ
            return new PortResult(port, false, "");
        }
    }
    
    private String getServiceName(int port) {
        String knownService = WELL_KNOWN_PORTS.get(port);
        if (knownService != null) {
            return knownService;
        }
        
        // Thử lấy tên dịch vụ từ system
        try {
            return getSystemServiceName(port);
        } catch (Exception e) {
            return "Unknown Service";
        }
    }
    
    private String getSystemServiceName(int port) {
        // Một số port phổ biến khác không có trong danh sách
        switch (port) {
            case 135: return "RPC Endpoint Mapper";
            case 139: return "NetBIOS Session Service";
            case 445: return "Microsoft-DS";
            case 631: return "IPP (Internet Printing Protocol)";
            case 3210: return "Custom TCP Calculator"; // Port của bài trước
            default: return "Unknown Service";
        }
    }
    
    private void displaySummary(String host, List<PortResult> openPorts, int totalScanned, double duration) {
        System.out.println("\n╔══════════════════════════════════════════");
        System.out.println("║                KET QUA QUET               ");
        System.out.println("╠══════════════════════════════════════════");
        System.out.printf("║ Host: %-33s  %n", host);
        System.out.printf("║ Tong so port quet: %-21d  %n", totalScanned);
        System.out.printf("║ Port dang mo: %-26d  %n", openPorts.size());
        System.out.printf("║ Thoi gian quet: %.2f giay %-15s  %n", duration, "");
        System.out.println("╚══════════════════════════════════════════");
        
        if (!openPorts.isEmpty()) {
            System.out.println("\n DANH SACH CAC PORT DANG MO:");
            System.out.println("┌──────────┬─────────────────────────────────┐");
            System.out.println("│   PORT   │            DICH VU              │");
            System.out.println("├──────────┼─────────────────────────────────┤");
            
            // Sắp xếp theo port
            openPorts.sort(Comparator.comparingInt(PortResult::getPort));
            
            for (PortResult result : openPorts) {
                System.out.printf("│ %8d │ %-31s │%n", 
                                result.getPort(), result.getService());
            }
            System.out.println("└──────────┴─────────────────────────────────┘");
        } else {
            System.out.println("\n Khong tim thay port nao dang mo trong dai quet.");
        }
        
        // Gợi ý
        // System.out.println("\n Goi y:");
        // System.out.println("   - Chay voi tham so: java PortScanner <hostname/IP>");
        // System.out.println("   - Vi du: java PortScanner google.com");
        // System.out.println("   - Hoac: java PortScanner 192.168.1.1");
    }
}

// Lớp lưu trữ kết quả quét port
class PortResult {
    private final int port;
    private final boolean isOpen;
    private final String service;
    
    public PortResult(int port, boolean isOpen, String service) {
        this.port = port;
        this.isOpen = isOpen;
        this.service = service;
    }
    
    public int getPort() {
        return port;
    }
    
    public boolean isOpen() {
        return isOpen;
    }
    
    public String getService() {
        return service;
    }
}