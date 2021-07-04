package tk.archo.music.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.browse.MediaBrowser
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
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
import java.io.File

class MusicActivity : AppCompatActivity() {
    lateinit var exoService: ExoPlayerService
    lateinit var exoServiceConn: ServiceConnection
    lateinit var intentExoService: Intent
    var isExoServiceBound: Boolean = false

    val songItems: ArrayList<SongItem> = arrayListOf()
    val exoItems: MutableList<MediaItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)
        (AudioScanner()).execute()
        bindActivityToExoService()

        if (savedInstanceState == null) {
            var fragHomeBundle = Bundle()
            fragHomeBundle.putParcelableArrayList("songItems", songItems)

            var homeFragment = MusicHomeFragment()
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

    fun changeFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            replace(R.id.music_main_fragment, fragment)
        }
    }

    fun changeFragmentWithBundle(fragment: Fragment, bundle: Bundle) {
        fragment.arguments = bundle
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            replace(R.id.music_main_fragment, fragment)
        }
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
            unbindService(exoServiceConn)
        }
    }

    fun setStatusBarColor(colorStr: String) {
        getWindow().statusBarColor = Color.parseColor(colorStr)
    }

    @SuppressLint("Deprecation", "StaticFieldLeak")
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