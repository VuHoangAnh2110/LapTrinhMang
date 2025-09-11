import java.io.*;
import java.nio.file.*;

public class exam8 {
    public static void main(String[] args) {
        String sourceFile = "D:\\test.txt";
        String newfile = "output8.txt";
        
        try {
            // Cách 1: Sử dụng BufferedReader và BufferedWriter
            copyFileWithBuffered(sourceFile, newfile);
            
            // Cách 2: Sử dụng Files.copy (Java NIO)
            // copyFileWithNIO(sourceFile, destinationFile);
            
            System.out.println("Copy file thành công!");
            System.out.println("Tu: " + sourceFile);
            System.out.println("Den: " + newfile);
        } catch (FileNotFoundException e) {
            System.out.println("Loi: Khong tim thay file - " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Loi: " + e.getMessage());
        }
    }
    
    // Phương thức copy sử dụng BufferedReader và BufferedWriter
    private static void copyFileWithBuffered(String source, String newfile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(source));
        BufferedWriter writer = new BufferedWriter(new FileWriter(newfile));
        
        String line;
        int lineCount = 0;
                
        while ((line = reader.readLine()) != null) {
            writer.write(line);
            writer.newLine(); 
            lineCount++;
        }
        
        reader.close();
        writer.close();
    }
    
    // // Phương thức copy sử dụng Java NIO
    // private static void copyFileWithNIO(String source, String destination) throws IOException {
    //     Path sourcePath = Paths.get(source);
    //     Path destinationPath = Paths.get(destination);
        
    //     // Copy file với option REPLACE_EXISTING (ghi đè nếu file đích đã tồn tại)
    //     Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        
    //     System.out.println("Copy hoàn tất với NIO");
    // }
}