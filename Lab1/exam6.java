import java.io.*;
import java.nio.charset.StandardCharsets;

public class exam6 {
    public static void main(String[] args) throws IOException {
        // Cách 1: Sử dụng PrintWriter với encoding UTF-8
        PrintWriter writer1 = new PrintWriter(
            new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true);
        
        System.out.println("=== Xuat voi UTF-8 ===");
        writer1.println("Xin chao! Hello!");
        writer1.print(2025);
        writer1.print("\n");
        writer1.println(3.14159);
        // // Cách 2: Sử dụng OutputStreamWriter với encoding khác
        // OutputStreamWriter writer2 = new OutputStreamWriter(System.out, "UTF-16");
        
        // System.out.println("\n=== Xuất với UTF-16 ===");
        // writer2.write("Tiếng Việt có dấu: áàảãạăắằẳẵặâấầẩẫậ\n");
        // writer2.write("Emojis: \n");
        // writer2.flush();
        
        // Cách 3: Sử dụng PrintWriter với auto-flush
        // PrintWriter writer3 = new PrintWriter(
        //     new OutputStreamWriter(System.out, StandardCharsets.ISO_8859_1), true);
        
        // System.out.println("\n=== Xuất với ISO-8859-1 ===");
        // writer3.println("Basic Latin characters: ABCDEFGHijklmnop");
        // writer3.println("Extended ASCII: àáâãäåæçèéêë");
        
        // // Cách 4: Thay đổi encoding động
        // System.out.println("\n=== Thay đổi encoding động ===");
        
        // String[] encodings = {"UTF-8", "UTF-16", "US-ASCII"};
        // String text = "Java Network Programming!";
        
        // for (String encoding : encodings) {
        //     try {
        //         OutputStreamWriter writer = new OutputStreamWriter(System.out, encoding);
        //         writer.write("[" + encoding + "] " + text + "\n");
        //         writer.flush();
        //     } catch (UnsupportedEncodingException e) {
        //         System.out.println("Encoding " + encoding + " không được hỗ trợ");
        //     }
        // }
        
        // // Cách 5: Sử dụng BufferedWriter với encoding
        // System.out.println("\n=== Sử dụng BufferedWriter ===");
        // BufferedWriter bufferedWriter = new BufferedWriter(
        //     new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
        
        // bufferedWriter.write("Đây là text từ BufferedWriter\n");
        // bufferedWriter.write("Với encoding UTF-8\n");
        // bufferedWriter.write("Hỗ trợ đầy đủ tiếng Việt: ăâêôơưđ\n");
        // bufferedWriter.flush();
        
        // Đóng các writer
        writer1.close();
        // writer2.close();
        // writer3.close();
        // bufferedWriter.close();
    }
}