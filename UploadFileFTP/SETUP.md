# Hướng dẫn cài đặt và chạy ứng dụng

## Yêu cầu hệ thống

- **Java JDK 17 trở lên** (đã cài Java 21 ✓)
- **Maven** (có sẵn Maven Wrapper trong project ✓)
- **JavaFX** (được tải tự động qua Maven)

## Cài đặt JAVA_HOME (nếu cần)

### Windows PowerShell (Administrator):

```powershell
# Tìm đường dẫn Java
Get-Command java | Select-Object -ExpandProperty Source

# Thiết lập JAVA_HOME (thay đổi đường dẫn phù hợp)
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-21", "Machine")

# Hoặc thiết lập cho user hiện tại
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-21", "User")
```

### Windows Command Prompt (Administrator):

```cmd
setx JAVA_HOME "C:\Program Files\Java\jdk-21" /M
```

**Lưu ý:** Sau khi thiết lập, cần mở lại terminal/command prompt.

## Cách chạy ứng dụng

### Cách 1: Sử dụng file BAT (Đơn giản nhất)

#### Chạy cả Server và Client:
```cmd
run.bat
```

#### Chỉ chạy Server:
```cmd
run-server.bat
```

#### Chỉ chạy Client (cần chạy server trước):
```cmd
run-client.bat
```

### Cách 2: Sử dụng Maven Wrapper

#### Chạy cả Server và Client:
```cmd
.\mvnw.cmd clean javafx:run
```

#### Chỉ chạy Server:
```cmd
.\mvnw.cmd clean compile
.\mvnw.cmd exec:java -Dexec.mainClass="com.example.uploadfileftp.ftpserver.FTPServer"
```

#### Chỉ chạy Client:
```cmd
.\mvnw.cmd clean javafx:run
```

### Cách 3: Sử dụng PowerShell

```powershell
# Di chuyển vào thư mục project
cd D:\UploadFileFTP

# Compile project
.\mvnw.cmd clean compile

# Chạy ứng dụng
.\mvnw.cmd javafx:run
```

## Hướng dẫn sử dụng

1. **Khởi động ứng dụng**
   - Chạy `run.bat` hoặc lệnh Maven
   - Server tự động khởi động trên port 2121
   - Giao diện Client sẽ hiển thị

2. **Kết nối đến Server**
   - Host: `localhost`
   - Port: `2121`
   - Username: `admin`
   - Password: `123`
   - Click nút "Kết nối"

3. **Upload File**
   - Sau khi kết nối thành công, click "Chọn File"
   - Chọn file từ máy tính
   - Click "Upload"
   - Theo dõi tiến trình trong log

4. **Kiểm tra kết quả**
   - File được upload sẽ lưu trong thư mục `data/`
   - Kiểm tra log để xem chi tiết quá trình

## Xử lý lỗi thường gặp

### Lỗi: "mvnw is not recognized"

**Nguyên nhân:** Chưa vào đúng thư mục project

**Giải pháp:**
```cmd
cd D:\UploadFileFTP
```

### Lỗi: "JAVA_HOME not found"

**Nguyên nhân:** Chưa thiết lập biến môi trường JAVA_HOME

**Giải pháp:** Xem phần "Cài đặt JAVA_HOME" ở trên

### Lỗi: "Address already in use"

**Nguyên nhân:** Port 2121 đã được sử dụng

**Giải pháp:**
1. Tìm và đóng process đang dùng port 2121:
```cmd
netstat -ano | findstr :2121
taskkill /PID <PID_NUMBER> /F
```

2. Hoặc đổi port trong code (FTPServer.java và giao diện client)

### Lỗi: "Connection refused"

**Nguyên nhân:** Server chưa khởi động

**Giải pháp:**
- Đảm bảo server đã chạy trước khi kết nối từ client
- Nếu chạy riêng, mở 2 terminal và chạy server trước

### Lỗi compile

**Nguyên nhân:** Dependencies chưa được tải về

**Giải pháp:**
```cmd
.\mvnw.cmd clean install -U
```

## Kiểm tra cài đặt

```cmd
# Kiểm tra Java
java -version

# Kiểm tra JAVA_HOME
echo %JAVA_HOME%

# Test compile
.\mvnw.cmd clean compile
```

## Cấu trúc thư mục sau khi chạy

```
UploadFileFTP/
├── data/              <- File upload sẽ lưu ở đây
├── src/
├── target/            <- File compiled
├── run.bat            <- Script chạy app
├── run-server.bat     <- Script chạy server
├── run-client.bat     <- Script chạy client
└── pom.xml
```

## Ghi chú

- Lần đầu chạy sẽ mất thời gian tải dependencies
- Server chỉ chạy trên localhost (không remote)
- Thông tin đăng nhập được hard-code (demo only)
- Thư mục `data/` tự động tạo khi server khởi động
