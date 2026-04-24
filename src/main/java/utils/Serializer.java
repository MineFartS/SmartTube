package SmartTubeApp.utils;

import java.math.BigInteger;
import java.io.Serializable;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

public class Serializer {

    public static String encode(Serializable obj) throws IOException {

        // Step 1: Serialize object to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        byte[] bytes = baos.toByteArray();

        // Step 2: Convert byte array to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b)); // Formats each byte as 2-digit hex
        }
        
        return hexString.toString();

    }

    public static Object decode(String hex) throws IOException, ClassNotFoundException {
 
        byte[] bytes = new BigInteger(hex, 16).toByteArray();

        // Handle potential leading zero byte added by BigInteger for signum
        if (bytes[0] == 0 && bytes.length > 1) {
            byte[] temp = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, temp, 0, temp.length);
            bytes = temp;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        }

    }

}