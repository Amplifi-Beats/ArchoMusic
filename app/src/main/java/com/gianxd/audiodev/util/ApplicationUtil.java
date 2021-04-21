package com.gianxd.audiodev.util;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
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

    public static void hideKeyboard(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public static void showKeyboard(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void toast(String toastMsg, int toastLength, Context context) {
        Toast.makeText(context, toastMsg, toastLength).show();
    }
}
