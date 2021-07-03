package tk.archo.music.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import tk.archo.music.R
import tk.archo.music.activity.MusicActivity

class MusicPlayerFragment : Fragment() {
    lateinit var sus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var fragmentView = inflater.inflate(R.layout.fragment_music_player, container, false)
        initializeViews(fragmentView)

        return fragmentView
    }

    fun initializeViews(fragmentView: View) {
        /* Find their views by IDs from layout */
        sus = fragmentView.findViewById(R.id.sus)

        sus.setOnClickListener {
            (activity as MusicActivity).changeFragmentToHome()
        }
    }

}