import java.io.*;

public class exam12 {
    public static void main(String[] args) {
        String fileName = "test.txt";
        
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            
            System.out.println("Nhap cac dong van ban (nhap 'stop' de ket thuc):");
            
            String line;
            int lineCount = 0;
            
            while ((line = reader.readLine()) != null) {
                if (line.equals("stop")) {
                    break;
                }
                
                writer.write(line);
                writer.newLine(); 
                lineCount++;

                System.out.println("Da ghi dong " + lineCount + ": " + line);
            }
            
            writer.close();
            reader.close();

            System.out.println("Da ghi " + lineCount + " dong vao file " + fileName);

            displayFileContent(fileName);
            
        } catch (IOException e) {
            System.out.println("Loi: " + e.getMessage());
        }
    }
    
    private static void displayFileContent(String fileName) throws IOException {
        System.out.println("\n=== Noi dung file " + fileName + " ===");

        BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
        String line;
        int lineNumber = 1;
        
        while ((line = fileReader.readLine()) != null) {
            System.out.println(lineNumber + ": " + line);
            lineNumber++;
        }
        
        fileReader.close();
    }
}