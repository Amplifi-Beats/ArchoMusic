package tk.archo.music.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtil {
    companion object {
        fun isPermissionGranted(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
        }

        fun isPermissionDenied(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_DENIED
        }

        fun requestPermission(activity: Activity, permission: String, requestCode: Int) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
        }
    }
}