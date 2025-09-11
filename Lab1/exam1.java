// Theo ví dụ
// import java.io.*;  
// class exam1   
// {  
//     public static void main(String args[]) throws IOException  
//     {  
//         byte data[] = new byte[100];  
//         System.out.print("Ban hay nhap mot so ky tu: ");  
//         System.in.read(data); 
//         System.out.print(" Cac ky tu cua ban da nhap: ");  
//         System.out.print('\n');
//         for(int i=0; i < data.length; i++)  
//             System.out.print((char) data[i]);  
//     }  
// } 

import java.io.*;
class exam1
{
    public static void main(String args[]) throws IOException  
    {  
        InputStream input = System.in;
        OutputStream output = System.out;
        
        byte data[] = new byte[100];
        
        String message = "Ban hay nhap mot so ky tu: ";
        output.write(message.getBytes());
        output.flush();
        
        int bytesRead = input.read(data);
        
        String result = "Cac ky tu cua ban da nhap: \n";
        output.write(result.getBytes());
        
        for(int i = 0; i < bytesRead - 1; i++) { 
            output.write(data[i]);
        }
        output.write('\n'); 
        output.write('\n');
        output.flush();
    }  
}
