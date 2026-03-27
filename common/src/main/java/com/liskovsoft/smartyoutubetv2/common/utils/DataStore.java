package com.liskovsoft.smartyoutubetv2.common.utils;

import com.liskovsoft.sharedutils.helpers.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Dictionary;
import java.util.HexFormat;
import java.io.*;

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

public class DataStore {

    private Dictionary<Integer, Object> mTable;

    private String mName;

    private SharedPreferences mPrefs;

    public DataStore(String name) {

        mTable = new Hashtable<>();

        mName = name;
        
        mPrefs = getSharedPreferences(name, Context.MODE_PRIVATE);
    
    }
    
    private read() {
        return mPrefs.getString(key, defVal);

        return Serializer.decode(mTable);
    }

    public Object get(
        Integer key, 
        Object defval
    ) {

        String hex = mPrefs.getString(key, null);

        if (hex == null) {
            return defval;
        } else {
            return Serializer.decode(hex);
        }

    }

    public Object get(Integer key) {
        return get(key, null);
    } 

    public put(
        Integer x, 
        Object value
    ) {

        String hex = Serializer.encode(value);

        mPrefs.edit()
            .putString(x, hex)
            .apply();

    }

}