package tk.archo.music.util

import android.content.Context
import android.view.inputmethod.InputMethodManager

class InputUtil {
    companion object {
        fun showInputKeyboard(context: Context) {
            val inputManager = (context.getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager)
            inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }

        fun hideInputKeyboard(context: Context) {
            val inputManager = (context.getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager)
            inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }
    }
}