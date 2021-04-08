package com.gianxd.audiodev.util;

import android.util.Log;

import java.io.UnsupportedEncodingException;

public class StringUtil {

    public static String decodeString(String encodedData) {
        String decodedData = encodedData;
        if (!decodedData.startsWith("/")) {
            try {
                decodedData = new String(android.util.Base64.decode(encodedData, android.util.Base64.DEFAULT), "UTF-8");
            } catch (UnsupportedEncodingException exception) {
                Log.e("Error", "UnsupportedEncodingException was threw.");
            }
        }
        return decodedData;
    }

    public static String encodeString(String decodedData) {
        String encodedData = decodedData;
        try {
            encodedData = android.util.Base64.encodeToString(encodedData.getBytes("UTF-8"), android.util.Base64.DEFAULT);
        } catch (UnsupportedEncodingException exception) {
            Log.e("Error", "UnsupportedEncodingException was threw.");
        }
        return encodedData;
    }

}
