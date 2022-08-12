package com.frolo.muse.ui.main.library.favourites

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.frolo.arch.support.observeNonNull
import com.frolo.core.ui.glide.GlideAlbumArtHelper
import com.frolo.core.ui.glide.observe
import com.frolo.music.model.Song
import com.frolo.muse.thumbnails.provideThumbnailLoader
import com.frolo.muse.ui.main.library.base.SimpleMediaCollectionFragment
import com.frolo.muse.ui.main.library.base.SongAdapter


class FavouriteSongListFragment: SimpleMediaCollectionFragment<Song>() {

    companion object {
        // Factory
        fun newIntent() = FavouriteSongListFragment()
    }

    override val viewModel: FavouriteSongListViewModel by viewModel()

    override val adapter: SongAdapter<Song> by lazy { SongAdapter<Song>(provideThumbnailLoader()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlideAlbumArtHelper.get().observe(this) {
            adapter.forceResubmit()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observerViewModel(viewLifecycleOwner)
    }

    private fun observerViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            isPlaying.observeNonNull(owner) { isPlaying ->
                adapter.setPlaying(isPlaying)
            }

            playingPosition.observeNonNull(owner) { playingPosition ->
                val isPlaying = isPlaying.value ?: false
                adapter.setPlayState(playingPosition, isPlaying)
            }
        }
    }
}