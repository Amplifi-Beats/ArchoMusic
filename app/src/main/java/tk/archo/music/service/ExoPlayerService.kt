package tk.archo.music.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.*
import tk.archo.music.R
import tk.archo.music.activity.MusicActivity
import tk.archo.music.data.SongItem
import tk.archo.music.util.AppUtil
import java.io.ByteArrayOutputStream


class ExoPlayerService(): Service() {

    /*** ExoPlayer is an extensible Android class used by YouTube.
     * THIS IS A CUSTOM CLASS FOR EXOPLAYER SINCE IT IS USED FOR PLAYING AUDIO.
     */

    private lateinit var player: SimpleExoPlayer
    private lateinit var notification: Notification
    private lateinit var notificationManager: NotificationManager
    private lateinit var mutableSongItems: MutableList<MediaItem>
    private lateinit var songItems: ArrayList<SongItem>
    private val serviceBinder = ExoServiceBinder()

    private val STR_ERR_INIT_ALREADY: String = "Cannot initialize multiple instances of ExoPlayer!"
    private val STR_ERR_INIT_NONE: String = "No ExoPlayer instance was initialized."
    private val STR_ERR_INIT_FAILED: String = "Unable to initialize an instance of ExoPlayer."
    private val STR_ERR_NOTIF_ALREADY: String = "Cannot create multiple player notifications!"
    private val STR_ERR_NOTIF_NONE: String = "No player notification was created."
    private val STR_ERR_NOTIF_FAILED: String = "Unable to create a player notification."
    private val STR_ERR_GENERIC: String = "An unknown error was occurred."
    private val STR_ERR_ITEM_ADD_FAILED: String = "Failed to add song item to ExoPlayer."
    private val STR_ERR_ITEM_DEL_FAILED: String = "Failed to remove song item from ExoPlayer."
    private val STR_ERR_ITEM_NON_INIT: String = "No ExoPlayer instance was initialized," +
            " ignoring item..."
    private val STR_ERR_ITEM_EXISTS: String = "Song item already exists in the " +
            " current ExoPlayer playlist!"

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        stopForeground(true)
    }

    override fun onBind(intent: Intent): IBinder {
        return serviceBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return false
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (this::player.isInitialized) {
            stop()
            release()
        }

        stopSelf()
    }

    fun initializePlayer() {
        if (this::player.isInitialized) {
            throw ExoServiceException(STR_ERR_INIT_ALREADY)
        }

        try {
            player = SimpleExoPlayer.Builder(this).build()
            player.setHandleAudioBecomingNoisy(true)
            if (Build.VERSION.SDK_INT > 26) {
                create_notif_oreo()
            }
        } catch (error: Exception) {
            throw ExoServiceException(STR_ERR_INIT_FAILED)
        }
    }

    fun prepare() {
        player.prepare()
    }

    fun previous() {
        player.previous()
    }

    fun play() {
        player.play()
    }

    fun next() {
        player.next()
    }

    fun pause() {
        player.pause()
    }

    fun stop() {
        player.stop()
    }

    fun release() {
        player.release()
    }

    fun seekTo(duration: Long) {
        player.seekTo(duration)
    }

    fun seekTo(index: Int, duration: Long) {
        player.seekTo(index, duration)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateNotification() {
        notificationManager.cancel(1)
        create_notif_oreo()
    }

    fun setRepeatMode(mode: Int) {
        player.repeatMode = mode
    }

    fun setShuffleEnabled(state: Boolean) {
        player.shuffleModeEnabled = state
    }

    fun setPlaybackParams(params: PlaybackParameters) {
        player.playbackParameters = params
    }

    fun getDuration(): Long {
        return player.duration
    }

    fun getPosition(): Long {
        return player.currentPosition
    }

    fun getIndex(): Int {
        return player.currentWindowIndex
    }

    fun isInitialized(): Boolean {
        return this::player.isInitialized
    }

    fun isPlaying(): Boolean {
        return player.isPlaying
    }

    fun addListener(listener: Player.Listener) {
        check(STR_ERR_INIT_NONE)
        player.addListener(listener)
    }

    fun removeListener(listener: Player.Listener) {
        check(STR_ERR_INIT_NONE)
        player.removeListener(listener)
    }

    fun addSongItem(data: String) {
        check(STR_ERR_ITEM_NON_INIT)

        try {
            player.addMediaItem(MediaItem.fromUri(data))
        } catch (error: Exception) {
            throw ExoServiceException(STR_ERR_ITEM_ADD_FAILED)
        }
    }

    fun addSongItemAtIndex(index: Int, data: String) {
        check(STR_ERR_ITEM_NON_INIT)

        try {
            player.addMediaItem(index, MediaItem.fromUri(data))
        } catch (error: Exception) {
            AppUtil.toast(this, STR_ERR_ITEM_ADD_FAILED, Toast.LENGTH_LONG)
        }
    }

    fun setSongItem(string: String) {
        check(STR_ERR_ITEM_NON_INIT)

        try {
            player.setMediaItem(MediaItem.fromUri(string))
        } catch (error: Exception) {
            AppUtil.toast(this, STR_ERR_ITEM_ADD_FAILED, Toast.LENGTH_LONG)
        }
    }

    fun setArraySongItems(arrayList: ArrayList<SongItem>) {
        songItems = arrayList
    }

    fun addSongItems(mutableList: MutableList<MediaItem>) {
        check(STR_ERR_ITEM_NON_INIT)
        mutableSongItems = mutableList

        try {
            player.addMediaItems(mutableList)
        } catch (error: Exception) {
            AppUtil.toast(this, STR_ERR_ITEM_ADD_FAILED, Toast.LENGTH_LONG)
        }
    }

    fun deleteSongItem(index: Int) {
        check(STR_ERR_ITEM_NON_INIT)

        try {
            player.removeMediaItem(index)
        } catch (error: Exception) {
            AppUtil.toast(this, STR_ERR_ITEM_DEL_FAILED, Toast.LENGTH_LONG)
        }
    }

    private fun check(err_str: String) {
        if (!this::player.isInitialized) {
            AppUtil.toast(this, err_str, Toast.LENGTH_LONG)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun create_notif_oreo() {
        check(STR_ERR_INIT_NONE)
        lateinit var imageBytes: ByteArray

        val imageRetriever = MediaMetadataRetriever()
        imageRetriever.setDataSource(songItems[getIndex()].getSongData())
        if (imageRetriever.embeddedPicture != null) {
            imageBytes = imageRetriever.embeddedPicture!!
        } else {
            /* If there is no artwork, use the default artwork */
            val bitmapArt = (ContextCompat
                .getDrawable(this, R.drawable.music_default_song_art) as BitmapDrawable).bitmap
            val artByte = ByteArrayOutputStream()
            bitmapArt.compress(Bitmap.CompressFormat.PNG, 100, artByte)
            imageBytes = artByte.toByteArray()
        }

        val currentIntent = Intent(this, MusicActivity::class.java)
        currentIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendInt =
            PendingIntent.getActivity(this, 0, currentIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationBuilder = NotificationCompat.Builder(this, "ExoPlayerService")
        val notificationChannel =
            NotificationChannel("ExoPlayerService", "ExoPlayer", NotificationManager.IMPORTANCE_LOW)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
        notificationBuilder.setNumber(0)
        notification = notificationBuilder.setOngoing(true)
            .setContentIntent(pendInt)
            .setPriority(NotificationManager.IMPORTANCE_LOW)
            .setNumber(0)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentText(songItems[getIndex()].getSongArtist()
                .plus(" ")
                .plus(getString(R.string.unicode_black_filled))
                .plus(" ")
                .plus(songItems[getIndex()].getSongAlbum()))
            .setContentTitle(songItems[getIndex()].getSongTitle())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle())
            .build()

        startForeground(1, notification)
    }

    private fun create_notif() {
        /* Implemented soon */
    }

    inner class ExoServiceBinder: Binder() {
        fun getService(): ExoPlayerService = this@ExoPlayerService
    }

    inner class ExoServiceException(error: String) : RuntimeException(error)
}