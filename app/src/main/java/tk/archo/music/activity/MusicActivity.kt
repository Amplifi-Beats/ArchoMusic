package tk.archo.music.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.graphics.Color
import android.media.browse.MediaBrowser
import android.os.AsyncTask
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.google.android.exoplayer2.MediaItem
import tk.archo.music.R
import tk.archo.music.data.SongItem
import tk.archo.music.fragment.MusicHomeFragment
import tk.archo.music.fragment.MusicPlayerFragment
import tk.archo.music.service.ExoPlayerService

class MusicActivity : AppCompatActivity() {
    lateinit var intentExoService: Intent
    lateinit var exoService: ExoPlayerService
    lateinit var exoServiceConn: ServiceConnection
    var isExoServiceBound: Boolean = false

    lateinit var songItems: ArrayList<SongItem>
    lateinit var exoItems: MutableList<MediaItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)
        (AudioScanner()).execute()

        if (savedInstanceState == null) {
            var fragHomeBundle = Bundle()
            fragHomeBundle.putParcelableArrayList("songItems", songItems)

            var homeFragment = MusicHomeFragment()
            homeFragment.arguments = fragHomeBundle

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                add(R.id.music_main_fragment, homeFragment, "homeFrag")
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

    fun changeFragmentToHome() {
        var fragHomeBundle = Bundle()
        fragHomeBundle.putParcelableArrayList("songItems", songItems)

        var homeFragment = MusicHomeFragment()
        homeFragment.arguments = fragHomeBundle

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            replace(R.id.music_main_fragment, homeFragment, "homeFrag")
        }
    }

    fun changeFragmentToPlayer() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            replace(R.id.music_main_fragment, MusicPlayerFragment(), "playerFrag")
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
                    exoService.addSongItems(applicationContext, exoItems)
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

    @SuppressLint("StaticFieldLeak")
    @Deprecated("AsyncTask is deprecated.")
    inner class AudioScanner(): AsyncTask<Void, Void, Void>() {
        override fun onPreExecute() {
            songItems = arrayListOf()
            exoItems = mutableListOf()
        }

        @SuppressLint("InlinedApi", "Recycle")
        override fun doInBackground(vararg path: Void?): Void? {
            val mediaProjection = arrayOf(
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID
            )
            val orderBy = " " + MediaStore.MediaColumns.DISPLAY_NAME
            val cursor: Cursor? = applicationContext.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mediaProjection,
                null,
                null,
                orderBy
            )
            try {
                if (cursor!!.moveToFirst()) {
                    var name: String
                    var data: String?
                    var artist: String
                    var album: String
                    do {
                        name =
                            cursor.getString(cursor.getColumnIndexOrThrow(
                                MediaStore.Audio.Media.TITLE))
                        data =
                            cursor.getString(cursor.getColumnIndexOrThrow(
                                MediaStore.Audio.Media.DATA))
                        artist =
                            cursor.getString(cursor.getColumnIndexOrThrow(
                                MediaStore.Audio.Media.ARTIST))
                        album =
                            cursor.getString(cursor.getColumnIndexOrThrow(
                                MediaStore.Audio.Media.ALBUM))
                        run {
                            songItems.add(SongItem(name, data, artist, album))
                            exoItems.add(MediaItem.fromUri(data))
                        }
                    } while (cursor.moveToNext())
                }
                cursor.close()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return null
        }

        override fun onProgressUpdate(vararg values: Void?) {}

        override fun onPostExecute(param: Void?) {
            /* do nothing */
        }
    }
}