import java.io.*;

public class exam3 {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.println("Nhap cac ky tu: ");
        
        StringBuilder result = new StringBuilder();
        int ch;
        
        while ((ch = reader.read()) != -1) {
            char character = (char) ch;
            
            if (character == '.') {
                break;
            }
            
            result.append(character);
        }
        
        System.out.println("\nCac ky tu ban da nhap: \n" + result.toString() + "\n");
        reader.close();
    }
}