import java.io.*;

public class exam7 {
    public static void main(String[] args) {
        String filePath = "D:\\test.txt";
        
        try {
            // Sử dụng BufferedReader
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            
            System.out.println("=== Noi dung file test.txt ===");
            String line;
            int lineNumber = 1;
            
            while ((line = reader.readLine()) != null) {
                System.out.println(lineNumber + ": " + line);
                lineNumber++;
            }
            
            reader.close();
            
        } catch (FileNotFoundException e) {
            System.out.println("Loi: Khong tim thay file " + filePath);
        } catch (IOException e) {
            System.out.println("LLoi doc file: " + e.getMessage());
        }
    }
}