package tk.archo.music.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.PlaybackParams
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import tk.archo.music.util.AppUtil

class ExoPlayerService(): Service() {

    /*** ExoPlayer is an extensible Android class used by YouTube.
     * THIS IS A CUSTOM CLASS FOR EXOPLAYER SINCE IT IS USED FOR PLAYING AUDIO.
     */

    lateinit var notifManager: PlayerNotificationManager
    private lateinit var player: SimpleExoPlayer
    private val serviceBinder = ExoServiceBinder()

    private val STR_ERR_INIT_ALREADY: String = "Cannot initialize multiple instances of ExoPlayer!"
    private val STR_ERR_INIT_NONE: String = "No ExoPlayer instance was initialized."
    private val STR_ERR_INIT_FAILED: String = "Unable to initialize an instance of ExoPlayer."
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

    override fun onBind(intent: Intent): IBinder {
        return serviceBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return false
    }

    fun initializePlayer(context: Context) {
        if (this::player.isInitialized) {
            throw ExoServiceException(STR_ERR_INIT_ALREADY)
        }

        try {
            player = SimpleExoPlayer.Builder(context).build()
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

    fun addListener(listener: Player.Listener) {
        player.addListener(listener)
    }

    fun removeListener(listener: Player.Listener) {
        player.removeListener(listener)
    }

    fun addSongItem(context: Context, string: String) {
        check_exoplayer_init(context, STR_ERR_ITEM_NON_INIT)

        try {
            if (string != null) {
                player.addMediaItem(MediaItem.fromUri(string))
            }
        } catch (error: Exception) {
            throw ExoServiceException(STR_ERR_ITEM_ADD_FAILED)
        }
    }

    fun addSongItemAtIndex(context: Context, index: Int, string: String) {
        check_exoplayer_init(context, STR_ERR_ITEM_NON_INIT)

        try {
            if (string != null) {
                player.addMediaItem(index, MediaItem.fromUri(string))
            }
        } catch (error: Exception) {
            AppUtil.toast(context, STR_ERR_ITEM_ADD_FAILED, Toast.LENGTH_LONG)
        }
    }

    fun setSongItem(context: Context, string: String) {
        check_exoplayer_init(context, STR_ERR_ITEM_NON_INIT)

        try {
            if (string != null) {
                player.setMediaItem(MediaItem.fromUri(string))
            }
        } catch (error: Exception) {
            AppUtil.toast(context, STR_ERR_ITEM_ADD_FAILED, Toast.LENGTH_LONG)
        }
    }

    fun deleteSongItem(context: Context, index: Int) {
        check_exoplayer_init(context, STR_ERR_ITEM_NON_INIT)

        try {
            player.removeMediaItem(index)
        } catch (error: Exception) {
            AppUtil.toast(context, STR_ERR_ITEM_DEL_FAILED, Toast.LENGTH_LONG)
        }
    }

    private fun check_exoplayer_init(context: Context, err_str: String) {
        if (player == null) {
            AppUtil.toast(context, err_str, Toast.LENGTH_LONG)
        }
    }

    inner class ExoServiceBinder: Binder() {
        fun getService(): ExoPlayerService = this@ExoPlayerService
    }

    inner class ExoServiceException(error: String) : RuntimeException(error)

}