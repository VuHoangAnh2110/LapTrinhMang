1. Xây dựng Client GUI - Sử dụng JavaFX
2. Implement network protocol - Xử lý tin nhắn giữa client-server
3. Tạo game graphics - Vẽ nhân vật, background, animations
4. Xử lý input - Keyboard/mouse controls
5. Sound system - Thêm hiệu ứng âm thanh
6. Game balance - Điều chỉnh gameplay mechanics
7. Error handling - Xử lý lỗi mạng và exceptions

    ===========================================================
>> cd d:\LapTrinhMang\GameLab
>> javac -cp "lib\mysql-connector-j-8.0.33.jar;src" src\server\*.java src\server\database\*.java src\client\*.java src\shared\models\*.java src\shared\utils\*.java

>> java -cp "lib\mysql-connector-j-8.0.33.jar;src" server.GameServer

>> java -cp "lib\mysql-connector-j-8.0.33.jar;src" client.MainMenu

    ===========================================================
🎨 1. Nguồn ảnh miễn phí cho game (sprite, background, icon)
    🔹 Itch.io (Asset Store)
        https://itch.io/game-assets/free
    Có rất nhiều sprite 2D, pixel art, UI icon, tileset miễn phí hoặc giá rẻ.
    👉 Rất phổ biến cho sinh viên, indie developer.

    🔹 OpenGameArt
        https://opengameart.org
    Kho tàng đồ họa CC0 / CC-BY / GPL (miễn phí, nhưng nhớ xem license).
    Có đủ: nhân vật, background, âm thanh, nhạc nền.

    🔹 Kenney.nl
        https://kenney.nl/assets
    Rất nổi tiếng, cung cấp asset 2D & 3D miễn phí (CC0 – public domain).
    Có đủ sprite sheet, UI button, tilemap → cực dễ dùng trong game.

    🔹 PixelGameArt
        https://pixelgameart.org/
    Pixel art đẹp, nhiều gói RPG, platformer, character animation.

    🔹 CraftPix.net
        https://craftpix.net/freebies/
    Có phần miễn phí: nhân vật, background, UI.
    Phù hợp cho game mobile và desktop.

🎨 2. Công cụ tự tạo hình ảnh
    Piskel (https://www.piskelapp.com/) → vẽ pixel art online.
    Aseprite (trả phí, chuyên vẽ sprite/animation pixel).
    GIMP / Krita (miễn phí, thay thế Photoshop).

🎨 3. Hình ảnh liên quan đến JDBC / Database
    Nếu bạn muốn kết hợp JDBC lưu trữ nhân vật:
    Bạn chỉ cần lưu đường dẫn sprite trong DB.
    Sprite vẫn lấy từ asset pack (các nguồn trên), không cần lưu file ảnh vào DB.

    ===========================================================