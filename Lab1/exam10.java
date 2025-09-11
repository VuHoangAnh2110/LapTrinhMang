import java.io.*;
import java.util.*;

public class exam10 {
    public static void main(String[] args) {
        String fileName = "numbers.dat";
        double[] numbers = {10.5, 25.5, 30.5, 45.5, 67.5, 89.5};
        
        try {
            writeDoublesToFile(fileName, numbers);
            
            readDoublesRandomly(fileName, numbers.length);
            
        } catch (IOException e) {
            System.out.println("Loi: " + e.getMessage());
        }
    }
    
    private static void writeDoublesToFile(String fileName, double[] numbers) throws IOException {
        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        
        // System.out.println("=== Ghi so vao file ===");
        for (int i = 0; i < numbers.length; i++) {
            file.writeDouble(numbers[i]);
            // System.out.println("Vi tri " + i + ": " + numbers[i]);
        }
        
        file.close();
        System.out.println("Da ghi " + numbers.length + " so vao file " + fileName);
    }
    
    private static void readDoublesRandomly(String fileName, int count) throws IOException {
        RandomAccessFile file = new RandomAccessFile(fileName, "r");
        Random random = new Random();

        System.out.println("\n=== Doc so theo thu tu ngau nhien ===");

        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            positions.add(i);
        }
        Collections.shuffle(positions);
        
        for (int pos : positions) {
            file.seek(pos * 8);
            double value = file.readDouble();
            System.out.println("Doc tu vi tri " + pos + ": " + value);
        }
        
        file.close();
    }
}