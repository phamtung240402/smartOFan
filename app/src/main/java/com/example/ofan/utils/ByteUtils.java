package com.example.ofan.utils;

public class ByteUtils {
    public static String bytes2HexStr(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            b.append(String.format("%02x", bytes[i] & 0xFF));
        }
        return b.toString();
    }

    public static String convertBytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte temp : bytes) {
            result.append(String.format("%02x", temp));
        }
        return result.toString();
    }

    public static byte[] hexStr2Bytes(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    public static int fromByteArray(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF) << 0 );
    }
}
