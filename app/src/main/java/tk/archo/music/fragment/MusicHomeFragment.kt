package tk.archo.music.fragment

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Typeface
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
import de.hdodenhof.circleimageview.CircleImageView
import tk.archo.music.R
import tk.archo.music.activity.MusicActivity
import tk.archo.music.data.SongItem
import tk.archo.music.service.ExoPlayerService
import tk.archo.music.util.AppUtil

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
    lateinit var music_home_albums_grid: RecyclerView
    lateinit var music_home_artists_title: TextView
    lateinit var music_home_artists_more: TextView
    lateinit var music_home_artists_grid: RecyclerView
    lateinit var music_home_songs_title: TextView
    lateinit var music_home_songs_more: TextView
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
    lateinit var music_home_explayer_song_progress: ProgressBar
    lateinit var music_home_explayer_song_title: TextView
    lateinit var music_home_explayer_song_subtitle: TextView
    lateinit var music_home_explayer_button_skip_previous: ImageView
    lateinit var music_home_explayer_button_playback: ImageView
    lateinit var music_home_explayer_button_skip_next: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var fragmentView = inflater.inflate(R.layout.fragment_music_home, container, false)
        initializeViews(fragmentView)

        return fragmentView
    }

    fun initializeViews(fragmentView: View) {
        /* Bind this fragment to ExoPlayerService */
        bindFragmentToExoService()

        /* Find their views by IDs from layout */
        music_home_layout = fragmentView.findViewById(R.id.music_home_layout)
        music_home_profile_image = fragmentView.findViewById(R.id.music_home_profile_image)
        music_home_title = fragmentView.findViewById(R.id.music_home_title)

        music_home_art_image = fragmentView.findViewById(R.id.music_home_art_image)
        music_home_art_layout = fragmentView.findViewById(R.id.music_home_art_layout)
        music_home_art_text = fragmentView.findViewById(R.id.music_home_art_text)

        music_home_albums_title = fragmentView.findViewById(R.id.music_home_albums_title)
        music_home_albums_more = fragmentView.findViewById(R.id.music_home_albums_more)
        music_home_albums_grid = fragmentView.findViewById(R.id.music_home_albums_grid)
        music_home_artists_title = fragmentView.findViewById(R.id.music_home_artists_title)
        music_home_artists_more = fragmentView.findViewById(R.id.music_home_artists_more)
        music_home_artists_grid = fragmentView.findViewById(R.id.music_home_artists_grid)
        music_home_songs_title = fragmentView.findViewById(R.id.music_home_songs_title)
        music_home_songs_more = fragmentView.findViewById(R.id.music_home_songs_more)
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
        music_home_explayer_song_progress = fragmentView.findViewById(R.id
            .music_home_explayer_song_progress)
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

        /* Add items to an empty ArrayList, set some HashMap settings ,and set the adapters. */
        var items: ArrayList<SongItem> = arrayListOf()
        items.add(SongItem("sus", "nglZ","impostor"))
        items.add(SongItem("Machine Gun (16bit remix)", "Noisia", "Unknown"))
        items.add(SongItem("AMOGN", "sus", "aiermogus"))
        items.add(SongItem("wat", "nub","more nub"))
        items.add(SongItem("yu mogus", "waht", "that sus ngl"))
        items.add(SongItem("OK LAMO HAKCER", "saa", "4132"))

        music_home_albums_grid.adapter = AlbumAdapter(items)
        music_home_artists_grid.adapter = ArtistAdapter(items)
        music_home_songs_grid.adapter = SongAdapter(items)

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
            unbindFragmentFromExoService()
            (activity as MusicActivity).changeFragment(MusicPlayerFragment(), "playerFrag")
        }
        music_home_explayer_layout.setOnClickListener {
            /* The minimized player and this expanded player has the same
               functionality so it will performing a click on the minimized player.
             */
            music_home_explayer_layout_minimized.performClick()
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
    }

    fun bindFragmentToExoService() {
        if (!this::intentExoService.isInitialized) {
            intentExoService = Intent()
            intentExoService.setClass(requireContext(), ExoPlayerService::class.java)
            activity?.startService(intentExoService)
        }
        if (!this::exoServiceConn.isInitialized) {
            exoServiceConn = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    val binder = service as ExoPlayerService.ExoServiceBinder
                    exoService = binder.getService()
                    isExoServiceBound = true

                    AppUtil.toast(context!!, "amogus sus connected", Toast.LENGTH_LONG)
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    isExoServiceBound = false

                    AppUtil.toast(context!!, "amogus sus disconnected", Toast.LENGTH_LONG)
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
        RecyclerView.Adapter<AlbumAdapter.AlbumAdapterViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumAdapterViewHolder {
            return AlbumAdapterViewHolder(LayoutInflater.from(context).inflate(R.layout.row_layout_grid, parent, false))
        }

        override fun onBindViewHolder(holder: AlbumAdapterViewHolder, position: Int) {
            holder.grid_item_title.setTypeface(
                Typeface.createFromAsset(
                    activity?.assets,
                    "fonts/OpenSans-Bold.ttf"))
            holder.grid_item_subtitle.setTypeface(
                Typeface.createFromAsset(
                    activity?.assets,
                    "fonts/OpenSans-Regular.ttf"))

            holder.grid_item_title.text = musicList[position].getSongAlbum()
            holder.grid_item_subtitle.text = musicList[position].getSongArtist()
            holder.grid_item_layout.setOnClickListener {
                holder.grid_item_art.performClick()
                music_home_explayer_subtitle_minimized.text =
                    musicList[position].getSongTitle().plus(" ")
                        .plus(getString(R.string.unicode_black_filled))
                        .plus(" ")
                        .plus(musicList[position].getSongArtist())
                        .plus(" ")
                        .plus(getString(R.string.unicode_black_filled))
                        .plus(" ")
                        .plus(musicList[position].getSongAlbum())
                music_home_explayer_song_title.text =
                    musicList[position].getSongTitle()
                music_home_explayer_song_subtitle.text =
                    musicList[position].getSongArtist().plus(" ")
                        .plus(getString(R.string.unicode_black_filled))
                        .plus(" ")
                        .plus(musicList[position].getSongAlbum())
            }
        }

        override fun getItemCount(): Int {
            return musicList.size
        }

        inner class AlbumAdapterViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
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

    inner class ArtistAdapter(val musicList: ArrayList<SongItem>):
        RecyclerView.Adapter<ArtistAdapter.ArtistAdapterViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistAdapterViewHolder {
            return ArtistAdapterViewHolder(LayoutInflater.from(context).inflate(R.layout.row_layout_grid, parent, false))
        }

        override fun onBindViewHolder(holder: ArtistAdapterViewHolder, position: Int) {
            holder.grid_item_title.setTypeface(
                Typeface.createFromAsset(
                    activity?.assets,
                    "fonts/OpenSans-Bold.ttf"))
            holder.grid_item_subtitle.setTypeface(
                Typeface.createFromAsset(
                    activity?.assets,
                    "fonts/OpenSans-Regular.ttf"))

            holder.grid_item_title.text = musicList[position].getSongArtist()
            holder.grid_item_subtitle.visibility = View.GONE
            holder.grid_item_layout.setOnClickListener {
                holder.grid_item_art.performClick()
                music_home_explayer_subtitle_minimized.text =
                    musicList[position].getSongTitle().plus(" ")
                        .plus(getString(R.string.unicode_black_filled))
                        .plus(" ")
                        .plus(musicList[position].getSongArtist())
                        .plus(" ")
                        .plus(getString(R.string.unicode_black_filled))
                        .plus(" ")
                        .plus(musicList[position].getSongAlbum())
                music_home_explayer_song_title.text =
                    musicList[position].getSongTitle()
                music_home_explayer_song_subtitle.text =
                    musicList[position].getSongArtist().plus(" ")
                        .plus(getString(R.string.unicode_black_filled))
                        .plus(" ")
                        .plus(musicList[position].getSongAlbum())
            }
        }

        override fun getItemCount(): Int {
            return musicList.size
        }

        inner class ArtistAdapterViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
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

    inner class SongAdapter(val musicList: ArrayList<SongItem>):
        RecyclerView.Adapter<SongAdapter.SongAdapterViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongAdapterViewHolder {
            return SongAdapterViewHolder(LayoutInflater.from(context).inflate(R.layout.row_layout_grid, parent, false))
        }

        override fun onBindViewHolder(holder: SongAdapterViewHolder, position: Int) {
            holder.grid_item_title.setTypeface(
                Typeface.createFromAsset(
                    activity?.assets,
                    "fonts/OpenSans-Bold.ttf"))
            holder.grid_item_subtitle.setTypeface(
                Typeface.createFromAsset(
                    activity?.assets,
                    "fonts/OpenSans-Regular.ttf"))

            holder.grid_item_title.text = musicList[position].getSongTitle()
            holder.grid_item_subtitle.text = musicList[position].getSongArtist()
            holder.grid_item_layout.setOnClickListener {
                holder.grid_item_art.performClick()
                music_home_explayer_subtitle_minimized.text =
                    musicList[position].getSongTitle().plus(" ")
                        .plus(getString(R.string.unicode_black_filled))
                        .plus(" ")
                        .plus(musicList[position].getSongArtist())
                        .plus(" ")
                        .plus(getString(R.string.unicode_black_filled))
                        .plus(" ")
                        .plus(musicList[position].getSongAlbum())
                music_home_explayer_song_title.text =
                    musicList[position].getSongTitle()
                music_home_explayer_song_subtitle.text =
                    musicList[position].getSongArtist().plus(" ")
                        .plus(getString(R.string.unicode_black_filled))
                        .plus(" ")
                        .plus(musicList[position].getSongAlbum())
            }
        }

        override fun getItemCount(): Int {
            return musicList.size
        }

        inner class SongAdapterViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
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
}