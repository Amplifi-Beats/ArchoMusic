package tk.archo.music.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startApp()
    }

    fun startApp() {
        var intent = Intent(this, MusicActivity::class.java)
        startActivity(intent)
    }
}