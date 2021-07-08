package tk.archo.music.util

import android.content.Context
import android.widget.Toast
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.lang.Runtime.getRuntime

class AppUtil {
    companion object {
        fun toast(context: Context, str: String, length: Int) {
            Toast.makeText(context, str, length).show()
        }

        fun cleanMem() {
            System.runFinalization()
            getRuntime().gc()
            System.gc()
        }

        fun getMemSize(): Int {
            return getRuntime().maxMemory().toInt() / 1048576
        }

        fun getAvailMemSize(): Int {
            var usedMemSize = getRuntime().totalMemory().toInt() - getRuntime()
                .freeMemory().toInt() / 1048576
            var maxMemSize = getRuntime().maxMemory().toInt() / 1048576
            return maxMemSize - usedMemSize
        }

        fun getStackTrace(throwable: Throwable?): String {
            val result: Writer = StringWriter()
            val printWriter = PrintWriter(result)
            var cause = throwable
            while (cause != null) {
                cause.printStackTrace(printWriter)
                cause = cause.cause
            }
            val stacktraceAsString = result.toString()
            printWriter.close()
            return stacktraceAsString
        }
    }
}