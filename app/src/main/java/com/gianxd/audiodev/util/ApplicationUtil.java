package com.gianxd.audiodev.util;

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

}
