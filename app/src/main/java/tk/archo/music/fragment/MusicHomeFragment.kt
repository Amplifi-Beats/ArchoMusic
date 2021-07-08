package tk.archo.music.fragment

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.IBinder
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.Player
import de.hdodenhof.circleimageview.CircleImageView
import tk.archo.music.R
import tk.archo.music.activity.MusicActivity
import tk.archo.music.data.SongItem
import tk.archo.music.service.ExoPlayerService
import tk.archo.music.util.AppUtil
import java.util.*
import kotlin.collections.ArrayList

class MusicHomeFragment : Fragment() {
    lateinit var intentExoService: Intent
    lateinit var exoService: ExoPlayerService
    lateinit var exoServiceConn: ServiceConnection
    var isExoServiceBound: Boolean = false

    lateinit var music_home_layout: LinearLayout
    lateinit var music_home_profile_image: CircleImageView
    lateinit var music_home_title: TextView

    lateinit var music_home_art_image: ImageView
    lateinit var music_home_art_layout: LinearLayout
    lateinit var music_home_art_text: TextView

    lateinit var music_home_albums_title: TextView
    lateinit var music_home_albums_more: TextView
    lateinit var music_home_albums_progress: ProgressBar
    lateinit var music_home_albums_grid: RecyclerView
    lateinit var music_home_artists_title: TextView
    lateinit var music_home_artists_more: TextView
    lateinit var music_home_artists_progress: ProgressBar
    lateinit var music_home_artists_grid: RecyclerView
    lateinit var music_home_songs_title: TextView
    lateinit var music_home_songs_more: TextView
    lateinit var music_home_songs_progress: ProgressBar
    lateinit var music_home_songs_grid: RecyclerView

    lateinit var music_home_menu_settings: TextView
    lateinit var music_home_menu_about: TextView
    lateinit var music_home_menu_help: TextView

    lateinit var music_home_explayer_layout: LinearLayout
    lateinit var music_home_explayer_layout_minimized: LinearLayout
    lateinit var music_home_explayer_title_minimized: TextView
    lateinit var music_home_explayer_subtitle_minimized: TextView
    lateinit var music_home_explayer_button_expandless: ImageView
    lateinit var music_home_explayer_title: TextView
    lateinit var music_home_explayer_button_expandmore: ImageView
    lateinit var music_home_explayer_song_art: ImageView
    lateinit var music_home_explayer_song_title: TextView
    lateinit var music_home_explayer_song_subtitle: TextView
    lateinit var music_home_explayer_button_skip_previous: ImageView
    lateinit var music_home_explayer_button_playback: ImageView
    lateinit var music_home_explayer_button_skip_next: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        bindFragmentToExoService()
    }

    override fun onStop() {
        super.onStop()
        unbindFragmentFromExoService()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var fragmentView = inflater.inflate(R.layout.fragment_music_home, container, false)
        initializeViews(fragmentView)

        return fragmentView
    }

    @Suppress("DEPRECATION")
    fun initializeViews(fragmentView: View) {
        /* Find their views by IDs from layout */
        music_home_layout = fragmentView.findViewById(R.id.music_home_layout)
        music_home_profile_image = fragmentView.findViewById(R.id.music_home_profile_image)
        music_home_title = fragmentView.findViewById(R.id.music_home_title)

        music_home_art_image = fragmentView.findViewById(R.id.music_home_art_image)
        music_home_art_layout = fragmentView.findViewById(R.id.music_home_art_layout)
        music_home_art_text = fragmentView.findViewById(R.id.music_home_art_text)

        music_home_albums_title = fragmentView.findViewById(R.id.music_home_albums_title)
        music_home_albums_more = fragmentView.findViewById(R.id.music_home_albums_more)
        music_home_albums_progress = fragmentView.findViewById(R.id.music_home_albums_progress)
        music_home_albums_grid = fragmentView.findViewById(R.id.music_home_albums_grid)
        music_home_artists_title = fragmentView.findViewById(R.id.music_home_artists_title)
        music_home_artists_more = fragmentView.findViewById(R.id.music_home_artists_more)
        music_home_artists_progress = fragmentView.findViewById(R.id.music_home_artists_progress)
        music_home_artists_grid = fragmentView.findViewById(R.id.music_home_artists_grid)
        music_home_songs_title = fragmentView.findViewById(R.id.music_home_songs_title)
        music_home_songs_more = fragmentView.findViewById(R.id.music_home_songs_more)
        music_home_songs_progress = fragmentView.findViewById(R.id.music_home_songs_progress)
        music_home_songs_grid = fragmentView.findViewById(R.id.music_home_songs_grid)

        music_home_menu_settings = fragmentView.findViewById(R.id.music_home_menu_settings)
        music_home_menu_about = fragmentView.findViewById(R.id.music_home_menu_about)
        music_home_menu_help = fragmentView.findViewById(R.id.music_home_menu_help)

        music_home_explayer_layout = fragmentView.findViewById(R.id.music_home_explayer_layout)
        music_home_explayer_layout_minimized = fragmentView.findViewById(R.id
            .music_home_explayer_layout_minimized)
        music_home_explayer_title_minimized = fragmentView.findViewById(R.id
            .music_home_explayer_title_minimized)
        music_home_explayer_subtitle_minimized = fragmentView.findViewById(R.id
            .music_home_explayer_subtitle_minimized)
        music_home_explayer_button_expandless = fragmentView.findViewById(R.id
            .music_home_explayer_button_expandless)
        music_home_explayer_title = fragmentView.findViewById(R.id.music_home_explayer_title)
        music_home_explayer_button_expandmore = fragmentView.findViewById(R.id
            .music_home_explayer_button_expandmore)
        music_home_explayer_song_art = fragmentView.findViewById(R.id.music_home_explayer_song_art)
        music_home_explayer_song_title = fragmentView.findViewById(R.id
            .music_home_explayer_song_title)
        music_home_explayer_song_subtitle = fragmentView.findViewById(R.id
            .music_home_explayer_song_subtitle)
        music_home_explayer_button_skip_previous = fragmentView.findViewById(R.id
            .music_home_explayer_button_skip_previous)
        music_home_explayer_button_playback = fragmentView.findViewById(R.id
            .music_home_explayer_button_playback)
        music_home_explayer_button_skip_next = fragmentView.findViewById(R.id
            .music_home_explayer_button_skip_next)

        /* Enable marquee animations */
        music_home_explayer_subtitle_minimized.isSelected = true
        music_home_explayer_song_title.isSelected = true
        music_home_explayer_song_subtitle.isSelected = true

        /* Set elevation to views */
        music_home_explayer_layout_minimized.elevation = 10f
        music_home_explayer_layout.elevation = 10f

        music_home_albums_progress.visibility = View.GONE
        music_home_artists_progress.visibility = View.GONE
        music_home_songs_progress.visibility = View.GONE

        /* Set fonts for TextViews */
        music_home_title.setTypeface(
            Typeface.createFromAsset(
                activity?.assets,
            "fonts/OpenSans-Bold.ttf"))
        music_home_art_text.setTypeface(
            Typeface.createFromAsset(
                activity?.assets,
            "fonts/OpenSans-Bold.ttf"))
        music_home_albums_title.setTypeface(
            Typeface.createFromAsset(
                activity?.assets,
            "fonts/OpenSans-Bold.ttf"))
        music_home_albums_more.setTypeface(
            Typeface.createFromAsset(
                activity?.assets,
            "fonts/OpenSans-Regular.ttf"))
        music_home_artists_title.setTypeface(
            Typeface.createFromAsset(
                activity?.assets,
            "fonts/OpenSans-Bold.ttf"))
        music_home_artists_more.setTypeface(
            Typeface.createFromAsset(
                activity?.assets,
            "fonts/OpenSans-Regular.ttf"))
        music_home_songs_title.setTypeface(
            Typeface.createFromAsset(
                activity?.assets,
            "fonts/OpenSans-Bold.ttf"))
        music_home_songs_more.setTypeface(
            Typeface.createFromAsset(
                activity?.assets,
            "fonts/OpenSans-Regular.ttf"))
        music_home_explayer_title_minimized.setTypeface(
            Typeface.createFromAsset(
                activity?.assets,
                "fonts/OpenSans-Bold.ttf"))
        music_home_explayer_subtitle_minimized.setTypeface(
            Typeface.createFromAsset(
                activity?.assets,
                "fonts/OpenSans-Regular.ttf"))
        music_home_explayer_title.setTypeface(
            Typeface.createFromAsset(
                activity?.assets,
                "fonts/OpenSans-Bold.ttf"))
        music_home_explayer_song_title.setTypeface(
            Typeface.createFromAsset(
                activity?.assets,
                "fonts/OpenSans-Bold.ttf"))
        music_home_explayer_song_subtitle.setTypeface(
            Typeface.createFromAsset(
                activity?.assets,
                "fonts/OpenSans-Regular.ttf"))

        /* Make music_home_explayer_layout gone */
        music_home_explayer_layout.visibility = View.GONE

        /* Set Layout Managers for RecyclerViews */
        music_home_albums_grid.layoutManager = GridLayoutManager(context, 1,
            LinearLayoutManager.HORIZONTAL, false)
        music_home_artists_grid.layoutManager = GridLayoutManager(context, 1,
            LinearLayoutManager.HORIZONTAL, false)
        music_home_songs_grid.layoutManager = GridLayoutManager(context, 1,
            LinearLayoutManager.HORIZONTAL, false)

        music_home_albums_grid.adapter = AlbumAdapter(requireArguments()
            .getParcelableArrayList("songItems")!!)
        music_home_artists_grid.adapter = ArtistAdapter(requireArguments()
            .getParcelableArrayList("songItems")!!)
        music_home_songs_grid.adapter = SongAdapter(requireArguments()
            .getParcelableArrayList("songItems")!!)

        /* Set onClick Listeners */
        music_home_art_layout.setOnClickListener {
            // functionality Soon
        }
        music_home_albums_more.setOnClickListener {
            // functionality Soon
        }
        music_home_artists_more.setOnClickListener {
            // functionality Soon
        }
        music_home_songs_more.setOnClickListener {
            // functionality Soon
        }
        music_home_menu_settings.setOnClickListener {
            // functionality Soon
        }
        music_home_menu_about.setOnClickListener {
            // functionality Soon
        }
        music_home_menu_help.setOnClickListener {
            // functionality Soon
        }
        music_home_explayer_layout_minimized.setOnClickListener {
            (activity as MusicActivity).changeFragment((activity as MusicActivity)
                .FRAGMENT_PLAYER_INT)
        }
        music_home_explayer_button_expandmore.setOnClickListener {
            TransitionManager.beginDelayedTransition(music_home_layout,
                AutoTransition())
            music_home_explayer_layout_minimized.visibility = View.GONE
            music_home_explayer_layout.visibility = View.VISIBLE
        }
        music_home_explayer_button_expandless.setOnClickListener {
            TransitionManager.beginDelayedTransition(music_home_layout,
                AutoTransition())
            music_home_explayer_layout.visibility = View.GONE
            music_home_explayer_layout_minimized.visibility = View.VISIBLE
        }
        music_home_explayer_button_skip_previous.setOnClickListener {
            exoService.previous()
        }
        music_home_explayer_button_playback.setOnClickListener {
            if (!exoService.isInitialized()) {
                return@setOnClickListener
            }
            if (!exoService.isPlaying()) {
                exoService.play()
            } else {
                exoService.pause()
            }
        }
        music_home_explayer_button_skip_next.setOnClickListener {
            exoService.next()
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun updateOnPlaybackUI(index: Int) {
        val arrayItems = requireArguments()
            .getParcelableArrayList<SongItem>("songItems")!!

        try {
            val imageRetriever = MediaMetadataRetriever()
            imageRetriever.setDataSource(arrayItems[index]
                .getSongData())
            val imageBytes = imageRetriever.embeddedPicture!!
            Glide.with(context!!).load(BitmapFactory
                .decodeByteArray(imageBytes, 0, imageBytes.size))
                .into(music_home_explayer_song_art)
        } catch (error: Exception) {
            Glide.with(context!!).load(R.drawable.music_default_song_art)
                .into(music_home_explayer_song_art)
        }

        music_home_explayer_subtitle_minimized.text =
            arrayItems[index].getSongTitle().plus(" ")
                .plus(getString(R.string.unicode_black_filled))
                .plus(" ")
                .plus(arrayItems[index].getSongArtist())
                .plus(" ")
                .plus(getString(R.string.unicode_black_filled))
                .plus(" ")
                .plus(arrayItems[index].getSongAlbum())
        music_home_explayer_song_title.text =
            arrayItems[index].getSongTitle()
        music_home_explayer_song_subtitle.text =
            arrayItems[index].getSongArtist().plus(" ")
                .plus(getString(R.string.unicode_black_filled))
                .plus(" ")
                .plus(arrayItems[index].getSongAlbum())
    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun bindFragmentToExoService() {
        if (!this::intentExoService.isInitialized) {
            intentExoService = Intent(context!!, ExoPlayerService::class.java)
        }
        if (!this::exoServiceConn.isInitialized) {
            exoServiceConn = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    val binder = service as ExoPlayerService.ExoServiceBinder
                    exoService = binder.getService()
                    isExoServiceBound = true

                    if (exoService.isInitialized()) {
                        exoService.addListener(object: Player.Listener {
                            override fun onPlaybackStateChanged(state: Int) {
                                if (state == Player.STATE_IDLE) {
                                    TransitionManager.beginDelayedTransition(music_home_layout,
                                        AutoTransition())
                                    music_home_explayer_layout_minimized
                                        .visibility = View.GONE
                                }
                            }

                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                if (isPlaying) {
                                    Glide.with(context!!).load(R.drawable.ic_pause_circle)
                                        .into(music_home_explayer_button_playback)
                                } else {
                                    Glide.with(context!!).load(R.drawable.ic_play_circle)
                                        .into(music_home_explayer_button_playback)
                                }
                            }

                            override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo,
                                                                 newPosition: Player.PositionInfo,
                                                                 reason: Int) {
                                updateOnPlaybackUI(exoService.getIndex())
                            }
                        })
                    }
                    if (exoService.isInitialized()
                        && exoService.isPlaying()) {
                        if (music_home_explayer_layout.visibility ==
                            View.GONE) {
                            TransitionManager.beginDelayedTransition(music_home_layout,
                                AutoTransition())
                            music_home_explayer_layout_minimized.visibility = View.VISIBLE
                        }

                        updateOnPlaybackUI(exoService.getIndex())
                    } else {
                        TransitionManager.beginDelayedTransition(music_home_layout,
                            AutoTransition())
                        music_home_explayer_layout_minimized
                            .visibility = View.GONE
                    }
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    isExoServiceBound = false
                }
            }
        }

        activity?.bindService(intentExoService, exoServiceConn, Context.BIND_AUTO_CREATE)
    }

    fun unbindFragmentFromExoService() {
        if (isExoServiceBound) {
            activity?.unbindService(exoServiceConn)
        }
    }

    inner class AlbumAdapter(val musicList: ArrayList<SongItem>):
        RecyclerView.Adapter<HomeGridViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeGridViewHolder {
            return HomeGridViewHolder(LayoutInflater.from(context).inflate(R.layout.row_layout_grid, parent, false))
        }

        override fun onBindViewHolder(holder: HomeGridViewHolder, position: Int) {
            holder.grid_item_title.setTypeface(
                Typeface.createFromAsset(
                    activity?.assets,
                    "fonts/OpenSans-Bold.ttf"))
            holder.grid_item_subtitle.setTypeface(
                Typeface.createFromAsset(
                    activity?.assets,
                    "fonts/OpenSans-Regular.ttf"))

            try {
                var imageRetriever = MediaMetadataRetriever()
                imageRetriever.setDataSource(musicList[position].getSongData())
                var imageBytes = imageRetriever.embeddedPicture!!
                Glide.with(context!!).load(BitmapFactory
                    .decodeByteArray(imageBytes, 0, imageBytes.size)).into(holder.grid_item_art)
            } catch (error: Exception) {
                Glide.with(context!!).load(R.drawable.music_default_song_art)
                    .into(holder.grid_item_art)
            }

            holder.grid_item_title.text = musicList[position].getSongAlbum()
            holder.grid_item_subtitle.text = musicList[position].getSongArtist()

            if (holder.grid_item_title.text == "Unknown Album") {
                holder.grid_item_title.text = "Unknown"
            }

            holder.grid_item_layout.setOnClickListener {
                holder.grid_item_art.performClick()
            }
        }

        override fun getItemCount(): Int {
            return musicList.size
        }
    }

    inner class ArtistAdapter(val musicList: ArrayList<SongItem>):
        RecyclerView.Adapter<HomeGridViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeGridViewHolder {
            return HomeGridViewHolder(LayoutInflater.from(context).inflate(R.layout.row_layout_grid, parent, false))
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: HomeGridViewHolder, position: Int) {
            holder.grid_item_title.setTypeface(
                Typeface.createFromAsset(
                    activity?.assets,
                    "fonts/OpenSans-Bold.ttf"))
            holder.grid_item_subtitle.setTypeface(
                Typeface.createFromAsset(
                    activity?.assets,
                    "fonts/OpenSans-Regular.ttf"))

            try {
                var imageRetriever = MediaMetadataRetriever()
                imageRetriever.setDataSource(musicList[position].getSongData())
                var imageBytes = imageRetriever.embeddedPicture!!
                Glide.with(context!!).load(BitmapFactory
                    .decodeByteArray(imageBytes, 0, imageBytes.size)).into(holder.grid_item_art)
            } catch (error: Exception) {
                Glide.with(context!!).load(R.drawable.music_default_song_art)
                    .into(holder.grid_item_art)
            }

            holder.grid_item_title.text = musicList[position].getSongArtist()
            holder.grid_item_subtitle.visibility = View.GONE

            if (holder.grid_item_title.text == "Unknown Artist") {
                holder.grid_item_title.text = "Unknown"
            }

            holder.grid_item_layout.setOnClickListener {
                holder.grid_item_art.performClick()
            }
        }

        override fun getItemCount(): Int {
            return musicList.size
        }
    }

    inner class SongAdapter(val musicList: ArrayList<SongItem>):
        RecyclerView.Adapter<HomeGridViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeGridViewHolder {
            return HomeGridViewHolder(LayoutInflater.from(context).inflate(R.layout.row_layout_grid, parent, false))
        }

        override fun onBindViewHolder(holder: HomeGridViewHolder, position: Int) {
            holder.grid_item_title.setTypeface(
                Typeface.createFromAsset(
                    activity?.assets,
                    "fonts/OpenSans-Bold.ttf"))
            holder.grid_item_subtitle.setTypeface(
                Typeface.createFromAsset(
                    activity?.assets,
                    "fonts/OpenSans-Regular.ttf"))

            try {
                var imageRetriever = MediaMetadataRetriever()
                imageRetriever.setDataSource(musicList[position].getSongData())
                var imageBytes = imageRetriever.embeddedPicture!!
                Glide.with(context!!).load(BitmapFactory
                    .decodeByteArray(imageBytes, 0, imageBytes.size)).into(holder.grid_item_art)
            } catch (error: Exception) {
                Glide.with(context!!).load(R.drawable.music_default_song_art)
                    .into(holder.grid_item_art)
            }

            holder.grid_item_title.isSelected = true
            holder.grid_item_subtitle.isSelected = true
            holder.grid_item_title.text = musicList[position].getSongTitle()
            holder.grid_item_subtitle.text = musicList[position].getSongArtist()
            holder.grid_item_layout.setOnClickListener {
                holder.grid_item_art.performClick()

                if (exoService.isInitialized() &&
                    exoService.getIndex() == position) {
                        if (music_home_explayer_layout.visibility == View.GONE) {
                            music_home_explayer_button_expandmore
                                .performClick()
                        }
                        return@setOnClickListener
                }

                updateOnPlaybackUI(position)
                exoService.seekTo(position, 0)
                if (music_home_explayer_layout.visibility == View.GONE) {
                    TransitionManager.beginDelayedTransition(music_home_layout,
                        AutoTransition())
                    music_home_explayer_layout_minimized.visibility = View.VISIBLE
                }
                if (!exoService.isPlaying()) {
                    exoService.prepare()
                    exoService.play()
                }
            }
        }

        override fun getItemCount(): Int {
            return musicList.size
        }
    }

    inner class HomeGridViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var grid_item_layout: LinearLayout
        var grid_item_art: ImageView
        var grid_item_title: TextView
        var grid_item_subtitle: TextView

        init {
            grid_item_layout = itemView.findViewById(R.id.grid_item_layout)
            grid_item_art = itemView.findViewById(R.id.grid_item_art)
            grid_item_title = itemView.findViewById(R.id.grid_item_title)
            grid_item_subtitle = itemView.findViewById(R.id.grid_item_subtitle)
        }
    }
}