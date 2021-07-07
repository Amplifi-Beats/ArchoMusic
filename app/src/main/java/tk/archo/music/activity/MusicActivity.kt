package tk.archo.music.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import com.google.android.exoplayer2.MediaItem
import tk.archo.music.R
import tk.archo.music.data.SongItem
import tk.archo.music.fragment.MusicHomeFragment
import tk.archo.music.fragment.MusicPlayerFragment
import tk.archo.music.service.ExoPlayerService
import java.io.File
import kotlin.system.exitProcess

class MusicActivity : AppCompatActivity() {
    lateinit var exoService: ExoPlayerService
    lateinit var exoServiceConn: ServiceConnection
    lateinit var intentExoService: Intent
    var isExoServiceBound: Boolean = false

    lateinit var homeFragment: MusicHomeFragment
    lateinit var playerFragment: MusicPlayerFragment
    lateinit var threadHandler: Handler

    val songItems: ArrayList<SongItem> = arrayListOf()
    val exoItems: MutableList<MediaItem> = mutableListOf()

    val FRAGMENT_HOME_INT = 0
    val FRAGMENT_PLAYER_INT = 1

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)
        (AudioScanner()).execute()
        bindActivityToExoService()
        threadHandler = Handler()

        if (savedInstanceState == null) {
            val fragHomeBundle = Bundle()
            fragHomeBundle.putParcelableArrayList("songItems", songItems)

            playerFragment = MusicPlayerFragment()
            homeFragment = MusicHomeFragment()
            homeFragment.arguments = fragHomeBundle

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                add(R.id.music_main_fragment, homeFragment)
            }
        }
    }

    override fun onBackPressed() {
        if (isExoServiceBound) {
            unbindActivityFromExoService()
            stopService(intentExoService)
        }

        finishAffinity()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val restartIntent = Intent(applicationContext, MainActivity::class.java)
        startActivity(restartIntent)
        exitProcess(0)
    }

    fun changeFragment(fragmentInt: Int) {
        lateinit var fragmentIntAsFragment: Fragment
        if (fragmentInt == FRAGMENT_HOME_INT) {
            fragmentIntAsFragment = homeFragment
        } else if (fragmentInt == FRAGMENT_PLAYER_INT) {
            fragmentIntAsFragment = playerFragment
        }

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            replace(R.id.music_main_fragment, fragmentIntAsFragment)
        }
    }

    fun changeFragmentWithBundle(fragmentInt: Int, bundle: Bundle) {
        lateinit var fragmentIntAsFragment: Fragment
        if (fragmentInt == FRAGMENT_HOME_INT) {
            fragmentIntAsFragment = homeFragment
        } else if (fragmentInt == FRAGMENT_PLAYER_INT) {
            fragmentIntAsFragment = playerFragment
        }

        fragmentIntAsFragment.arguments = bundle
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            replace(R.id.music_main_fragment, fragmentIntAsFragment)
        }
    }

    fun startUiThread(action: Runnable) {
        threadHandler.postDelayed(action, 0)
    }

    fun cancelUiThread(action: Runnable) {
        threadHandler.removeCallbacksAndMessages(action)
    }

    fun bindActivityToExoService() {
        if (!this::exoServiceConn.isInitialized) {
            intentExoService = Intent(applicationContext, ExoPlayerService::class.java)
        }
        if (!this::exoServiceConn.isInitialized) {
            exoServiceConn = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    val binder = service as ExoPlayerService.ExoServiceBinder
                    exoService = binder.getService()
                    if (!exoService.isInitialized()) {
                        exoService.initializePlayer()
                        exoService.addSongItems(exoItems)
                    }

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
            unbindService(exoServiceConn)
        }
    }

    fun setStatusBarColor(colorStr: String) {
        getWindow().statusBarColor = Color.parseColor(colorStr)
    }

    fun setNavigationBarColor(colorStr: String) {
        getWindow().navigationBarColor = Color.parseColor(colorStr)
    }

    @SuppressLint("Deprecation", "StaticFieldLeak")
    @Suppress("DEPRECATION")
    inner class AudioScanner: AsyncTask<Void, Void, Void>() {
        @SuppressLint("InlinedApi")
        override fun doInBackground(vararg path: Void?): Void? {
            val mediaProjection = arrayOf(
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM
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
                    val metadata = MediaMetadataRetriever()
                    var data: String?
                    var name: String
                    var artist: String
                    var album: String
                    do {
                        data =
                            cursor.getString(cursor.getColumnIndexOrThrow(
                                MediaStore.Audio.Media.DATA))
                        try {
                            metadata.setDataSource(data!!)
                        } catch (error: Exception) {
                            cursor.moveToNext()
                        }

                        name =
                            metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                                .toString()
                        if (name == "null") {
                            name =
                                cursor.getString(cursor.getColumnIndexOrThrow(
                                    MediaStore.Audio.Media.TITLE))
                        }

                        artist =
                            metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                                .toString()
                        if (artist == "null") {
                            artist =
                                cursor.getString(cursor.getColumnIndexOrThrow(
                                    MediaStore.Audio.Media.ARTIST))

                            if (artist == "<unknown>") {
                                artist = "Unknown Artist"
                            }
                        }

                        album =
                            metadata
                                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                                .toString()
                        if (album == "null") {
                            album =
                                cursor.getString(cursor.getColumnIndexOrThrow(
                                    MediaStore.Audio.Media.ALBUM))

                            if (File(data!!).parentFile!!.name
                                == album) {
                                album = "Unknown Album"
                            } else if (album == "<unknown>") {
                                album = "Unknown Album"
                            }
                        }
                        run {
                            if (File(data!!).exists()) {
                                songItems.add(SongItem(name, data, artist, album))
                                exoItems.add(MediaItem.fromUri(data))
                            }
                        }
                    } while (cursor.moveToNext())
                }
                cursor.close()
            } catch (error: Exception) {
                error.printStackTrace()
            }

            return null
        }

        override fun onProgressUpdate(vararg values: Void?) {}

        override fun onPostExecute(param: Void?) {
            /* do nothing */
        }
    }
}