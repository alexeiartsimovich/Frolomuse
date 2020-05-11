package com.frolo.muse.ui.main.library.base

import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.frolo.muse.R
import com.frolo.muse.glide.makeRequest
import com.frolo.muse.inflateChild
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.views.MiniVisualizer
import com.frolo.muse.views.media.MediaConstraintLayout
import com.l4digital.fastscroll.FastScroller
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.include_song_art_container.view.*
import kotlinx.android.synthetic.main.item_song.view.*


open class SongAdapter<T: Song> constructor(
    private val requestManager: RequestManager? = null
): BaseAdapter<T, SongAdapter.SongViewHolder>(), FastScroller.SectionIndexer {

    var playingPosition = -1
        private set
    var isPlaying = false
        private set

    override fun getItemId(position: Int) = getItemAt(position).id

    fun submit(list: List<T>, position: Int, isPlaying: Boolean) {
        this.playingPosition = position
        this.isPlaying = isPlaying
        submit(list)
    }

    fun setPlayingPositionAndState(position: Int, isPlaying: Boolean) {
        if (this.playingPosition == position
                && this.isPlaying == isPlaying) {
            return
        }

        if (playingPosition >= 0) {
            this.isPlaying = false
            notifyItemChanged(playingPosition)
        }
        this.playingPosition = position
        this.isPlaying = isPlaying
        notifyItemChanged(position)
    }

    fun setPlayingState(isPlaying: Boolean) {
        if (this.isPlaying == isPlaying) {
            return
        }

        this.isPlaying = isPlaying
        if (playingPosition >= 0)
            notifyItemChanged(playingPosition)
    }

    override fun onPreRemove(position: Int) {
        if (playingPosition == position) {
            playingPosition = -1
        } else if (playingPosition > position) {
            playingPosition--
        }
    }

    override fun onPreMove(fromPosition: Int, toPosition: Int) {
        when (playingPosition) {
            fromPosition -> playingPosition = toPosition
            in (fromPosition + 1)..toPosition -> playingPosition--
            in toPosition until fromPosition -> playingPosition++
        }
    }

    override fun onCreateBaseViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = SongViewHolder(parent.inflateChild(R.layout.item_song))

    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int,
        item: T,
        selected: Boolean,
        selectionChanged: Boolean
    ) {

        val isPlayPosition = position == playingPosition

        with((holder.itemView as MediaConstraintLayout)) {
            val res = resources
            tv_song_name.text = item.getNameString(res)
            tv_artist_name.text = item.getArtistString(res)
            tv_duration.text = item.getDurationString()

            val safeRequestManager = requestManager ?: Glide.with(this)
            safeRequestManager.makeRequest(item.albumId)
                .placeholder(R.drawable.ic_framed_music_note)
                .error(R.drawable.ic_framed_music_note)
                .circleCrop()
                .into(imv_album_art)

            imv_check.setChecked(selected, selectionChanged)

            setChecked(selected)
            setPlaying(isPlayPosition)
        }

        holder.resolvePlayingPosition(
            isPlaying = isPlaying,
            isPlayPosition = isPlayPosition
        )
    }

    override fun getSectionText(position: Int) = sectionIndexAt(position) { title }

    open class SongViewHolder(itemView: View): BaseViewHolder(itemView) {
        override val viewOptionsMenu: View? = itemView.view_options_menu

        private val songArtOverlay: View? =
                itemView.findViewById(R.id.view_song_art_overlay)
        private val miniVisualizer: MiniVisualizer? =
                itemView.findViewById(R.id.mini_visualizer)

        fun resolvePlayingPosition(
            isPlayPosition: Boolean,
            isPlaying: Boolean
        ) {
            if (isPlayPosition) {
                songArtOverlay?.visibility = View.VISIBLE
                miniVisualizer?.visibility = View.VISIBLE
                miniVisualizer?.setAnimate(isPlaying)
            } else {
                songArtOverlay?.visibility = View.INVISIBLE
                miniVisualizer?.visibility = View.INVISIBLE
                miniVisualizer?.setAnimate(false)
            }
        }
    }

}