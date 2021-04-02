package com.gianxd.audiodev.util;

import android.content.Context;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ApplicationUtil {

    public static String getStackTrace(Throwable throwable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        Throwable cause = throwable;

        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }

        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }
    public static void toast(Context context, String toastMsg, int toastLength) {
        Toast.makeText(context, toastMsg, toastLength).show();
    }

}
