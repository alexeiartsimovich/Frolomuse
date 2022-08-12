package com.frolo.muse.interactor.media

import com.frolo.music.model.Playlist
import com.frolo.music.model.Song
import com.frolo.music.repository.PlaylistChunkRepository
import com.frolo.music.repository.SongRepository
import com.frolo.muse.rx.SchedulerProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single


class AddSongToPlaylistUseCase @AssistedInject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val songRepository: SongRepository,
    private val playlistChunkRepository: PlaylistChunkRepository,
    @Assisted private val playlist: Playlist
) {

    fun getTargetPlaylist(): Flowable<Playlist> = Flowable.just(playlist)

    fun search(query: String): Flowable<List<Song>> {
        return songRepository.getFilteredItems(query)
            .subscribeOn(schedulerProvider.worker())
    }

    fun addSongs(songs: Collection<Song>): Completable {
        return Single.just(songs)
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.worker())
            .flatMapCompletable { selectedItems ->
                playlistChunkRepository.addToPlaylist(playlist, selectedItems)
            }
    }

    @AssistedFactory
    interface Factory {
        fun create(playlist: Playlist): AddSongToPlaylistUseCase
    }

}