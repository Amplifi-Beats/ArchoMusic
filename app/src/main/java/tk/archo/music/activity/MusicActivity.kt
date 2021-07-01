package tk.archo.music.activity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.*
import tk.archo.music.R
import tk.archo.music.fragment.MusicHomeFragment

class MusicActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                add<MusicHomeFragment>(R.id.music_main_fragment, "homeFrag")

            }
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    fun changeFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            replace(R.id.music_main_fragment, fragment, tag)
        }
    }

    fun setStatusBarColor(colorStr: String) {
        getWindow().statusBarColor = Color.parseColor(colorStr)
    }
}