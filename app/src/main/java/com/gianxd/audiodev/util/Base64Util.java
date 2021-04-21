package com.gianxd.audiodev.util;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class Base64Util {
    public static String decode(String encodedData) {
        String decodedData = encodedData;

        if (!decodedData.startsWith("/")) {
            decodedData = new String(Base64.decode(encodedData, Base64.DEFAULT), StandardCharsets.UTF_8);
        }

        return decodedData;
    }

    public static String encode(String decodedData) {
        String encodedData = decodedData;
        encodedData = Base64.encodeToString(encodedData.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        return encodedData;
    }
}
