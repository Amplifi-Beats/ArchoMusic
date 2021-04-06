package com.gianxd.audiodev.util;

import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import static com.gianxd.audiodev.AudioDev.applicationContext;

public class StringUtil {

    public static String decodeString(String encodedData) {
        String decodedData = encodedData;
        if (decodedData.startsWith("/")) {
            try {
                return new String(android.util.Base64.decode(encodedData, android.util.Base64.DEFAULT), "UTF-8");
            } catch (UnsupportedEncodingException exception) {
                return null;
            }
        }
        return null;
    }

    public static String encodeString(String decodedData) {
        try {
            return android.util.Base64.encodeToString(decodedData.getBytes("UTF-8"), android.util.Base64.DEFAULT);
        } catch (UnsupportedEncodingException exception) {
            return null;
        }
    }

}
