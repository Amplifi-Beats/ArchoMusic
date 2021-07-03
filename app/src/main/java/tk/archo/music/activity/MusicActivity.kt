package tk.archo.music.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.*
import tk.archo.music.R
import tk.archo.music.fragment.MusicHomeFragment
import tk.archo.music.service.ExoPlayerService

class MusicActivity : AppCompatActivity() {
    lateinit var intentExoService: Intent
    lateinit var exoService: ExoPlayerService
    lateinit var exoServiceConn: ServiceConnection
    var isExoServiceBound: Boolean = false

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

        bindActivityToExoService()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isExoServiceBound) {
            exoService.release()
            unbindActivityFromExoService()
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

    fun bindActivityToExoService() {
        if (!this::intentExoService.isInitialized) {
            intentExoService = Intent()
            intentExoService.setClass(applicationContext, ExoPlayerService::class.java)
            startService(intentExoService)
        }
        if (!this::exoServiceConn.isInitialized) {
            exoServiceConn = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    val binder = service as ExoPlayerService.ExoServiceBinder
                    exoService = binder.getService()
                    exoService.initializePlayer(applicationContext)
                    isExoServiceBound = true
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    isExoServiceBound = false
                }

            }
        }

       bindService(intentExoService, exoServiceConn, Context.BIND_AUTO_CREATE)
    }

    fun unbindActivityFromExoService() {
        if (isExoServiceBound) {
            stopService(intentExoService)
            unbindService(exoServiceConn)
        }
    }

    fun setStatusBarColor(colorStr: String) {
        getWindow().statusBarColor = Color.parseColor(colorStr)
    }
}