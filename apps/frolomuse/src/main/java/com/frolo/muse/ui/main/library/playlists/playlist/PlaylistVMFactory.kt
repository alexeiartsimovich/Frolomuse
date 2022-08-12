package com.frolo.muse.ui.main.library.playlists.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.ActivityComponentInjector
import com.frolo.muse.di.ActivityComponentProvider
import com.frolo.player.Player
import com.frolo.muse.router.AppRouter
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetPlaylistUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Playlist
import com.frolo.music.model.Song
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class PlaylistVMFactory constructor(
    injector: ActivityComponentInjector,
    provider: ActivityComponentProvider,
    playlistArg: Playlist
): ViewModelProvider.Factory {

    @Inject
    internal lateinit var player: Player
    @Inject
    internal lateinit var permissionChecker: PermissionChecker
    /*assisted inject*/
    internal lateinit var getPlaylistUseCase: GetPlaylistUseCase
    @Inject
    internal lateinit var getMediaMenuUseCase: GetMediaMenuUseCase<Song>
    @Inject
    internal lateinit var clickMediaUseCase: ClickMediaUseCase<Song>
    @Inject
    internal lateinit var playMediaUseCase: PlayMediaUseCase<Song>
    @Inject
    internal lateinit var shareMediaUseCase: ShareMediaUseCase<Song>
    @Inject
    internal lateinit var deleteMediaUseCase: DeleteMediaUseCase<Song>
    @Inject
    internal lateinit var getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>
    @Inject
    internal lateinit var changeFavouriteUseCase: ChangeFavouriteUseCase<Song>
    @Inject
    internal lateinit var createSongShortcutUseCase: CreateShortcutUseCase<Song>
    @Inject
    internal lateinit var createPlaylistShortcutUseCase: CreateShortcutUseCase<Playlist>
    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var preferences: Preferences
    @Inject
    internal lateinit var appRouter: AppRouter
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        injector.inject(this)
        getPlaylistUseCase = provider
            .provideGetPlaylistSongsUseCaseFactory()
            .create(playlistArg)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PlaylistViewModel(
            player,
            permissionChecker,
            getPlaylistUseCase,
            getMediaMenuUseCase,
            clickMediaUseCase,
            playMediaUseCase,
            shareMediaUseCase,
            deleteMediaUseCase,
            getIsFavouriteUseCase,
            changeFavouriteUseCase,
            createSongShortcutUseCase,
            createPlaylistShortcutUseCase,
            schedulerProvider,
            appRouter,
            eventLogger
        ) as T
    }

}