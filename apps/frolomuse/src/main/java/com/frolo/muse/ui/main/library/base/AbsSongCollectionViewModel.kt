package com.frolo.muse.ui.main.library.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.frolo.arch.support.liveDataOf
import com.frolo.player.AudioSource
import com.frolo.player.Player
import com.frolo.player.SimplePlayerObserver
import com.frolo.muse.router.AppRouter
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetMediaUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Song
import com.frolo.music.model.SongCountWithTotalDuration
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable


abstract class AbsSongCollectionViewModel<T: Song> constructor(
    private val player: Player,
    permissionChecker: PermissionChecker,
    getMediaListUseCase: GetMediaUseCase<T>,
    getMediaMenuUseCase: GetMediaMenuUseCase<T>,
    clickMediaUseCase: ClickMediaUseCase<T>,
    playMediaUseCase: PlayMediaUseCase<T>,
    shareMediaUseCase: ShareMediaUseCase<T>,
    deleteMediaUseCase: DeleteMediaUseCase<T>,
    getIsFavouriteUseCase: GetIsFavouriteUseCase<T>,
    changeFavouriteUseCase: ChangeFavouriteUseCase<T>,
    createShortcutUseCase: CreateShortcutUseCase<T>,
    private val schedulerProvider: SchedulerProvider,
    appRouter: AppRouter,
    eventLogger: EventLogger
): AbsMediaCollectionViewModel<T>(
        permissionChecker,
        getMediaListUseCase,
        getMediaMenuUseCase,
        clickMediaUseCase,
        playMediaUseCase,
        shareMediaUseCase,
        deleteMediaUseCase,
        getIsFavouriteUseCase,
        changeFavouriteUseCase,
        createShortcutUseCase,
        schedulerProvider,
        appRouter,
        eventLogger
) {

    private var playingPositionDisposable: Disposable? = null

    private val playerObserver = object : SimplePlayerObserver() {
        override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
            detectPlayingPosition(mediaList.value, item)
        }
        override fun onPlaybackStarted(player: Player) {
            _isPlaying.value = true
        }
        override fun onPlaybackPaused(player: Player) {
            _isPlaying.value = false
        }
    }

    private val _isPlaying: MutableLiveData<Boolean> = MutableLiveData()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _playingPosition: MutableLiveData<Int> = MediatorLiveData<Int>().apply {
        addSource(mediaList) { list ->
            detectPlayingPosition(list, player.getCurrent())
        }
    }
    val playingPosition: LiveData<Int> = _playingPosition

    val songCountWithTotalDuration: LiveData<SongCountWithTotalDuration> by lazy {
        Transformations.switchMap(mediaList) { songs: List<Song>? ->
            if (songs == null) {
                // NULL for NULL
                return@switchMap liveDataOf<SongCountWithTotalDuration>(null)
            }

            MutableLiveData<SongCountWithTotalDuration>().apply {
                Single.fromCallable {
                    val totalDuration = songs.sumBy { it.duration }
                    SongCountWithTotalDuration(songs.count(), totalDuration)
                }
                    .subscribeOn(schedulerProvider.computation())
                    .observeOn(schedulerProvider.main())
                    .doOnSubscribe { d ->
                        calcSongCountWithTotalDurationDisposable?.dispose()
                        calcSongCountWithTotalDurationDisposable = d
                    }
                    .subscribeFor { value = it }
            }
        }
    }
    private var calcSongCountWithTotalDurationDisposable: Disposable? = null

    init {
        player.registerObserver(playerObserver)
        _isPlaying.value = player.isPlaying()
    }

    private fun detectPlayingPosition(songList: List<Song>?, item: AudioSource?) {
        Single.fromCallable { songList?.indexOfFirst { it.id == item?.id } ?: -1 }
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.main())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSuccess(position: Int) {
                        _playingPosition.value = position
                    }
                    override fun onSubscribe(d: Disposable) {
                        playingPositionDisposable?.dispose()
                        playingPositionDisposable = d
                    }
                    override fun onError(e: Throwable) {
                        logError(e)
                    }
                })
    }

    override fun onCleared() {
        super.onCleared()
        player.unregisterObserver(playerObserver)
        playingPositionDisposable?.dispose()
    }
}