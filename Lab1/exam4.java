import java.io.*;

public class exam4 
{
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.println("Nhap vao chuoi" + "\nDung o stop: ");
        
        String line;
        StringBuilder result = new StringBuilder();
        
        while ((line = reader.readLine()) != null) {
            if (line.equals("stop")) {
                break;
            }
            
            result.append(line).append("\n");
        }
        
        System.out.println("\nCac chuoi ban da nhap:");
        System.out.println(result.toString() + "\n");
        
        reader.close();
    }
}

//Ví dụ trong bài
// import java.io.*;  
// class ReadChars  
// {  
//     public static void main(String args[]) throws IOException  
//     {  
//         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
//         System.out.println("Nhap chuoi ky tu, gioi han stop ");  
        
//         String line;
        
//         do  
//         {  
//             line = br.readLine(); 
//             if (line != null && !line.equals("stop")) {
//                 System.out.println("Ban da nhap: " + line);
//             }
            
//         } while(line != null && !line.equals("stop")); 
        
//         System.out.println("Ket thuc chuong trinh!");
//         br.close();
//     }  
// }