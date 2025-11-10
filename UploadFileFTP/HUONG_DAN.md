# HƯỚNG DẪN SỬ DỤNG ỨNG DỤNG FTP CLIENT-SERVER

## 🎯 GIỚI THIỆU

Ứng dụng này mô phỏng hệ thống Client-Server sử dụng giao thức FTP để upload file. 
Bao gồm:
- **FTP Server**: Nhận và lưu file vào thư mục `data/`
- **FTP Client**: Giao diện JavaFX để chọn và upload file

## 📋 YÊU CẦU

- ✅ Java JDK 17 trở lên (đã có Java 21)
- ✅ Maven (có sẵn Maven Wrapper)
- ✅ Windows OS

## 🚀 CÁCH CHẠY ỨNG DỤNG

### Cách 1: Đơn giản nhất (Khuyến nghị)

1. Mở **Command Prompt** hoặc **PowerShell**
2. Di chuyển vào thư mục project:
   ```cmd
   cd D:\UploadFileFTP
   ```
3. Chạy lệnh:
   ```cmd
   run.bat
   ```
4. Chờ compile xong, giao diện Client sẽ tự động hiển thị

### Cách 2: Chạy riêng Server và Client

**Terminal 1 - Chạy Server:**
```cmd
cd D:\UploadFileFTP
run-server.bat
```
Giữ terminal này mở, server sẽ chạy liên tục.

**Terminal 2 - Chạy Client:**
```cmd
cd D:\UploadFileFTP
run-client.bat
```
Giao diện Client sẽ hiển thị.

### Cách 3: Dùng Maven trực tiếp

```cmd
cd D:\UploadFileFTP
.\mvnw.cmd clean javafx:run
```

## 📖 HƯỚNG DẪN SỬ DỤNG

### Bước 1: Khởi động ứng dụng
- Chạy `run.bat`
- Màn hình console sẽ hiển thị log của Server
- Giao diện Client sẽ tự động mở

### Bước 2: Kết nối đến Server

Trên giao diện Client, các thông tin đã được điền sẵn:
- **Host**: `localhost`
- **Port**: `2121`
- **Username**: `admin`
- **Password**: `123`

👉 Nhấn nút **"Kết nối"**

**Kết quả:**
- Nếu thành công: Hiển thị ✓ "Đã kết nối và đăng nhập thành công" (màu xanh)
- Nếu thất bại: Hiển thị ✗ "Đăng nhập thất bại" (màu đỏ)

### Bước 3: Chọn file để upload

Sau khi kết nối thành công:
1. Nhấn nút **"Chọn File"**
2. Cửa sổ chọn file sẽ hiển thị
3. Chọn file bất kỳ từ máy tính (txt, pdf, image, docx, v.v.)
4. Tên file và kích thước sẽ hiển thị dưới nút

### Bước 4: Upload file

1. Nhấn nút **"Upload"**
2. Theo dõi tiến trình qua:
   - **Progress Bar**: Thanh tiến trình trực quan
   - **Log**: Chi tiết từng bước trong vùng "Log hoạt động"

**Kết quả:**
- Upload thành công: Thông báo "Upload thành công!" + dialog xác nhận
- Upload thất bại: Thông báo lỗi chi tiết

### Bước 5: Kiểm tra file

File đã upload sẽ được lưu tại:
```
D:\UploadFileFTP\data\<tên_file>
```

Mở thư mục `data` để xem file đã upload.

## 🎨 GIAO DIỆN

### Phần 1: Thông tin kết nối
```
┌─────────────────────────────────────┐
│ Host:     [localhost             ]  │
│ Port:     [2121                  ]  │
│ Username: [admin                 ]  │
│ Password: [***                   ]  │
│ [Kết nối]  ✓ Đã kết nối thành công │
└─────────────────────────────────────┘
```

### Phần 2: Upload File
```
┌─────────────────────────────────────┐
│ [Chọn File]  File: test.txt (1.2KB) │
│ ████████████░░░░░░░░░ 60%           │
│ [Upload]                            │
└─────────────────────────────────────┘
```

### Phần 3: Log hoạt động
```
┌─────────────────────────────────────┐
│ Sẵn sàng kết nối đến FTP Server...  │
│ Đang kết nối đến localhost:2121     │
│ Server: 220 FTP Server sẵn sàng     │
│ Client: USER admin                  │
│ Server: 331 Yêu cầu mật khẩu        │
│ ========== Đăng nhập thành công === │
│ Đã chọn file: D:\test.txt           │
│ Bắt đầu upload file...              │
│ Đã gửi: 50%                         │
│ ========== Upload thành công ====== │
└─────────────────────────────────────┘
```

## ❗ XỬ LÝ LỖI THƯỜNG GẶP

### Lỗi 1: "Connection refused"
**Nguyên nhân:** Server chưa chạy

**Giải pháp:**
1. Mở terminal mới
2. Chạy: `run-server.bat`
3. Chờ thấy dòng "FTP Server đã khởi động trên cổng 2121"
4. Mới chạy client và kết nối

### Lỗi 2: "Address already in use"
**Nguyên nhân:** Port 2121 đã được chương trình khác sử dụng

**Giải pháp:**
```cmd
# Tìm process đang dùng port 2121
netstat -ano | findstr :2121

# Kết quả: TCP 0.0.0.0:2121  0.0.0.0:0  LISTENING  12345
# 12345 là PID

# Kill process đó
taskkill /PID 12345 /F
```

### Lỗi 3: "JAVA_HOME not found"
**Nguyên nhân:** Biến môi trường JAVA_HOME chưa được thiết lập

**Giải pháp (PowerShell Administrator):**
```powershell
# Thiết lập JAVA_HOME
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-21", "Machine")

# Khởi động lại terminal
```

### Lỗi 4: "530 Login incorrect"
**Nguyên nhân:** Sai username hoặc password

**Giải pháp:**
- Username chính xác: `admin`
- Password chính xác: `123`
- Không có khoảng trắng thừa

### Lỗi 5: File không xuất hiện trong thư mục data/
**Nguyên nhân:** Lỗi quyền ghi hoặc upload chưa hoàn tất

**Giải pháp:**
1. Kiểm tra log xem có dòng "226 Upload hoàn tất" không
2. Kiểm tra quyền ghi thư mục `data/`
3. F5 (refresh) Windows Explorer

## 📊 THÔNG TIN KỸ THUẬT

### Luồng kết nối FTP:
```
Client                          Server
  |                               |
  |---- TCP Connect ------------->|
  |<--- 220 Welcome --------------|
  |---- USER admin -------------->|
  |<--- 331 Password required ----|
  |---- PASS 123 --------------->|
  |<--- 230 Login OK -------------|
  |---- STOR file.txt ----------->|
  |<--- 150 Ready ----------------|
  |---- <file size> ------------->|
  |---- <binary data> ----------->|
  |<--- 226 Complete -------------|
  |---- QUIT -------------------->|
  |<--- 221 Goodbye --------------|
  |                               |
```

### Mã phản hồi FTP:
- **220**: Server sẵn sàng
- **230**: Đăng nhập thành công
- **331**: Cần nhập password
- **150**: Sẵn sàng nhận file
- **226**: Transfer hoàn tất
- **530**: Đăng nhập thất bại
- **550**: Lỗi lưu file

## 🔧 TÙY CHỈNH

### Đổi port server:
1. Mở `FTPServer.java`
2. Tìm dòng: `private static final int DEFAULT_PORT = 2121;`
3. Đổi thành port mong muốn, ví dụ: `2122`
4. Lưu file
5. Compile lại

### Đổi thông tin đăng nhập:
1. Mở `ClientHandler.java`
2. Tìm:
   ```java
   private static final String VALID_USER = "admin";
   private static final String VALID_PASS = "123";
   ```
3. Đổi thành giá trị mong muốn
4. Lưu và compile lại

### Đổi thư mục lưu file:
1. Mở `ClientHandler.java`
2. Tìm: `private static final String DATA_DIR = "data";`
3. Đổi thành đường dẫn mong muốn
4. Lưu và compile lại

## 📝 TIPS

1. **Upload file lớn:** Ứng dụng hỗ trợ file lớn, nhưng sẽ mất thời gian. Theo dõi progress bar.

2. **Upload nhiều file:** Sau khi upload xong, có thể chọn file khác và upload tiếp mà không cần kết nối lại.

3. **Kiểm tra log:** Log rất chi tiết, giúp debug khi có lỗi.

4. **Restart server:** Nếu server bị lỗi, Ctrl+C để dừng và chạy lại.

5. **File trùng tên:** Upload file trùng tên sẽ ghi đè file cũ.

## 🎓 MỤC ĐÍCH HỌC TẬP

Ứng dụng này minh họa:
- ✅ Lập trình mạng với Socket
- ✅ Giao thức FTP cơ bản
- ✅ JavaFX GUI
- ✅ Multi-threading
- ✅ File I/O
- ✅ Client-Server architecture

## 📞 HỖ TRỢ

Nếu gặp vấn đề:
1. Kiểm tra log trong console và trong giao diện
2. Xem lại phần "Xử lý lỗi" ở trên
3. Đọc file `SETUP.md` để biết chi tiết cài đặt
4. Đọc file `PROJECT_INFO.txt` để hiểu cấu trúc code

---
**Chúc bạn sử dụng thành công! 🎉**


== CLo1:
Quy trình thiết lập và ngắt kết nối trong mô hình Client–Server sử dụng giao thức TCP:

1. Server khởi động và lắng nghe:
   Server tạo một ServerSocket và lắng nghe trên một cổng (port) cố định, ví dụ port 8888.
   Việc chọn cổng phụ thuộc vào:
      Không trùng với các cổng hệ thống (0–1023).
      Thuộc vùng 1024–65535, dễ cấu hình và tránh xung đột với dịch vụ khác.
   Server chờ yêu cầu kết nối từ client (trạng thái listening).

2. Client kết nối đến Server:
   Client tạo Socket và kết nối tới địa chỉ IP và port của Server.
   Khi kết nối được chấp nhận, TCP thiết lập kênh truyền song công (full-duplex) giữa hai bên.

3. Trao đổi dữ liệu:
   Client gửi một chuỗi văn bản lên Server (qua OutputStream).
   Server nhận chuỗi, chuyển thành chữ in hoa, rồi gửi lại phản hồi cho Client.
   Client nhận phản hồi (qua InputStream) và hiển thị kết quả.

4. Ngắt kết nối:
   Sau khi hoàn tất trao đổi, Client đóng Socket trước.
   Server đóng luồng và Socket tương ứng sau khi xử lý xong.
   Cuối cùng, Server vẫn tiếp tục lắng nghe các kết nối mới (nếu không tắt hẳn chương trình).

Chọn port: 8888
Giải thích:
Port 8888 nằm trong dải 1024–65535, là cổng tự do (ephemeral) không bị hệ thống hay dịch vụ phổ biến chiếm dụng, thường dùng cho ứng dụng thử nghiệm hoặc giao tiếp nội bộ giữa client–server.











== Clo2:
Dự án của bạn đang sử dụng TCP (Transmission Control Protocol), không phải UDP.

📡 Phân tích giao thức
1. FTP sử dụng TCP
FTP (File Transfer Protocol) luôn luôn hoạt động trên nền tảng TCP, không bao giờ dùng UDP.

2. Bằng chứng trong code
Hãy xem các file server và client:

- Server (FTPServer.java):
ServerSocket serverSocket = new ServerSocket(port);
// ServerSocket chỉ hỗ trợ TCP

- Client Handler (ClientHandler.java):
Socket clientSocket = ...;
// Socket là TCP socket

- Client (ClientController.java):
socket = new Socket(host, port);
// Socket = TCP connection

3. So sánh TCP vs UDP
Đặc điểm	   ||          TCP                        ||	               UDP
Class java  ||   	Socket, ServerSocket	            ||      DatagramSocket
Kết nối	   || Hướng kết nối (connection-oriented)	|| Không kết nối (connectionless)



Hỗ trợ nhiều client đồng thời!

1. Server hỗ trợ Multi-threading
File: FTPServer.java
while (running) {
    Socket clientSocket = serverSocket.accept();
    
    // Tạo thread mới cho MỖI client
    ClientHandler handler = new ClientHandler(clientSocket);
    new Thread(handler).start(); // ← Mỗi client = 1 thread riêng
}

Nghĩa là:
Client 1 kết nối → Thread 1 xử lý
Client 2 kết nối → Thread 2 xử lý (đồng thời với Client 1)
Client 3, 4, 5... → Mỗi client có thread riêng

2. Trong code
ClientHandler.java implements Runnable:
public class ClientHandler implements Runnable {
    @Override
    public void run() {
        // Xử lý client độc lập
    }
}
Mỗi ClientHandler chạy trong thread riêng biệt, không ảnh hưởng lẫn nhau.

3. Kịch bản thực tế
4. Kiểm chứng
Bạn có thể test bằng cách:

Kết quả mong đợi:
Cả 3 client đều kết nối thành công
Cả 3 đều upload được file đồng thời
Server log hiện 3 dòng "Client đã kết nối"