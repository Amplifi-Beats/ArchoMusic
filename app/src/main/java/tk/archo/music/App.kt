package tk.archo.music

import android.app.Application
import android.content.Intent
import android.util.Log
import tk.archo.music.util.AppUtil
import java.util.*

class App : Application() {
    private var uncaughtExceptionHandler:
            Thread.UncaughtExceptionHandler? = null

    override fun onCreate() {
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread: Thread, ex: Throwable ->
            Log.e("AudioDev", AppUtil.getStackTrace(ex))
            uncaughtExceptionHandler!!.uncaughtException(thread, ex)
        }

        super.onCreate()
    }
}