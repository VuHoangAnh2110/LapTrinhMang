# FTP Client-Server Application

Ứng dụng JavaFX cho phép upload file từ client lên server sử dụng giao thức FTP.

## ✨ Cập nhật mới (November 7, 2025)

- ✅ **Loại bỏ file thừa**: Đã xóa các file cũ không sử dụng
- ✅ **Giao diện mới**: Nút "Chọn File" và "Upload" trên cùng một hàng
- ✅ **Log lớn hơn**: Ô log cao hơn (350px) để hiển thị nhiều thông tin
- ✅ **Resize được**: Cửa sổ có thể phóng to/thu nhỏ, giao diện tự động điều chỉnh
- ✅ **Kích thước mới**: 800x700 (tăng từ 700x600) cho trải nghiệm tốt hơn

## Cấu trúc dự án

```
src/
├── ftpclient/
│   ├── ClientApp.java         - Main application JavaFX cho client
│   ├── ClientController.java   - Controller xử lý logic giao diện
│   └── Main.fxml              - Giao diện FXML
├── ftpserver/
│   ├── FTPServer.java         - FTP Server lắng nghe kết nối
│   └── ClientHandler.java     - Xử lý mỗi client connection
└── Main.java                  - Main class để chạy cả server và client
```

## Tính năng

### Server
- Lắng nghe kết nối trên localhost:2121
- Xử lý lệnh FTP cơ bản: USER, PASS, STOR, QUIT
- Xác thực đăng nhập (user: admin, pass: 123)
- Lưu file upload vào thư mục `data/`
- Hỗ trợ đa client (mỗi client 1 thread riêng)

### Client
- Giao diện JavaFX thân thiện
- Kết nối đến FTP server
- Đăng nhập với username/password
- Chọn file từ máy tính
- Upload file với progress bar
- Hiển thị log chi tiết quá trình

## Cách chạy

### Cách 1: Chạy cả Server và Client (Recommended)

```bash
mvn clean javafx:run
```

Hoặc chạy trực tiếp class Main:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.example.uploadfileftp.Main"
```

### Cách 2: Chạy riêng Server

Terminal 1 - Chạy Server:
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.example.uploadfileftp.ftpserver.FTPServer"
```

### Cách 3: Chạy riêng Client

Terminal 2 - Chạy Client:
```bash
mvn clean javafx:run
```

## Hướng dẫn sử dụng

1. **Khởi động ứng dụng**
   - Chạy Main.java hoặc dùng lệnh maven
   - Server sẽ tự động khởi động trên port 2121
   - Giao diện client sẽ hiển thị

2. **Kết nối**
   - Host: `localhost` (mặc định)
   - Port: `2121` (mặc định)
   - Username: `admin`
   - Password: `123`
   - Nhấn nút "Kết nối"

3. **Upload file**
   - Sau khi đăng nhập thành công, nhấn "Chọn File"
   - Chọn file từ máy tính
   - Nhấn "Upload"
   - Theo dõi tiến trình qua progress bar và log

4. **Kiểm tra file**
   - File được upload sẽ lưu trong thư mục `data/` tại thư mục gốc project

## Mã phản hồi FTP

- `220` - Service ready
- `230` - User logged in
- `530` - Login incorrect
- `150` - File status okay, about to open data connection
- `226` - Closing data connection, transfer complete
- `331` - User name okay, need password
- `502` - Command not implemented
- `550` - Requested action not taken

## Yêu cầu hệ thống

- Java 17 hoặc cao hơn
- Maven 3.6+
- JavaFX 17+
- Apache Commons Net 3.9.0

## Thư viện sử dụng

- **JavaFX**: Giao diện người dùng
- **Apache Commons Net**: Hỗ trợ FTP protocol cho client
- **Java Socket API**: Xây dựng FTP server

## Ghi chú

- Server chạy trên localhost, không hỗ trợ remote connection
- Thông tin đăng nhập được hard-code (demo only)
- Chỉ hỗ trợ các lệnh FTP cơ bản
- Thư mục `data/` sẽ tự động được tạo nếu chưa tồn tại

## Troubleshooting

**Lỗi: "Address already in use"**
- Port 2121 đã được sử dụng
- Giải pháp: Dừng process đang dùng port hoặc đổi port khác

**Lỗi: "Connection refused"**
- Server chưa được khởi động
- Giải pháp: Chạy FTPServer trước khi kết nối từ client

**File không upload được**
- Kiểm tra quyền ghi thư mục `data/`
- Kiểm tra dung lượng đĩa
- Xem log để biết chi tiết lỗi
