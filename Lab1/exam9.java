import java.io.*;
public class exam9 {
    public static void main(String[] args) {
        String fileName = "output9.dat";
        writeDataToFile(fileName);
        readDataFromFile(fileName);
    }

    private static void writeDataToFile(String fileName) {
        System.out.println("\n1. Ghi file: ");
        System.out.println("File: " + fileName);

        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(fileName)))) {

            dos.writeBoolean(true);
            System.out.println("boolean: true");

            dos.writeByte(100);
            System.out.println("byte: 100");

            dos.writeShort(30000);
            System.out.println("short: 30000");

            dos.writeInt(1234567890);
            System.out.println("int: 1234567890");

            dos.writeLong(9876543210L);
            System.out.println("long: 9876543210");

            dos.writeFloat(4.255f);
            System.out.println("float: 4.255");

            dos.writeDouble(8.8238483294823);
            System.out.println("double: 8.8238483294823");

            dos.writeChar('A');
            System.out.println("char: 'A'");

            byte[] byteArray = {10, 20, 30, 40, 50};
            dos.writeInt(byteArray.length);
            dos.write(byteArray);
            System.out.println("array: [10, 20, 30, 40, 50]");

            dos.flush();
            System.out.println("\nGhi hoan tat!");

        } catch (IOException e) {
            System.err.println("Loi khi ghi file: " + e.getMessage());
        }
    }

    private static void readDataFromFile(String fileName) {
        System.out.println("\n2. Doc file: ");
        System.out.println("File: " + fileName);

        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(fileName)))) {

            boolean boolValue = dis.readBoolean();
            System.out.println("boolean: " + boolValue);

            byte byteValue = dis.readByte();
            System.out.println("byte: " + byteValue);

            short shortValue = dis.readShort();
            System.out.println("short: " + shortValue);

            int intValue = dis.readInt();
            System.out.println("int: " + intValue);

            long longValue = dis.readLong();
            System.out.println("long: " + longValue);

            float floatValue = dis.readFloat();
            System.out.println("float: " + floatValue);

            double doubleValue = dis.readDouble();
            System.out.println("double: " + doubleValue);

            char charValue = dis.readChar();
            System.out.println("char: '" + charValue + "'");

            int byteArrayLength = dis.readInt();
            byte[] byteArray = new byte[byteArrayLength];
            dis.readFully(byteArray);
            System.out.print("array: [");
            for (int i = 0; i < byteArray.length; i++) {
                System.out.print(byteArray[i]);
                if (i < byteArray.length - 1) System.out.print(", ");
            }
            System.out.println("]");

            System.out.println("\n Doc hoan tat!");

        } catch (EOFException e) {
            System.err.println("Da doc het file: " + e.getMessage());
        } catch (FileNotFoundException e) {
            System.err.println("Khong tim thay file: " + fileName);
        } catch (IOException e) {
            System.err.println("Loi khi doc file: " + e.getMessage());
        }
    }
}