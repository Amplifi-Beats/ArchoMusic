package tk.archo.music.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent

class SearchBar: androidx.appcompat.widget.AppCompatEditText {
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context,
        attrs, defStyle)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
            event.action == KeyEvent.ACTION_DOWN) {
                clearFocus()
            return false
        }
        return super.onKeyPreIme(keyCode, event)
    }
}