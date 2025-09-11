import java.io.*;

public class exam5 {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new FileWriter("output5.txt"));
        
        System.out.println("Nhap cac dong text ('stop' ket thuc va tao file):");
        
        String line;
        int lineCount = 0;
        
        while ((line = reader.readLine()) != null) {
            if (line.equals("stop")) {
                break;
            }
            
            writer.write(line);
            writer.newLine();
            lineCount++;
        }
        
        writer.close();
        reader.close();
        
        System.out.println("\nDa tao file 'output5.txt' thanh cong!");
        
        System.out.println("\nNoi dung file vua tao:");
        BufferedReader fileReader = new BufferedReader(new FileReader("output5.txt"));
        String fileLine;
        while ((fileLine = fileReader.readLine()) != null) {
            System.out.println(fileLine);
        }
        fileReader.close();
    }
}