package com.frolo.muse.ui.main.library.genres.genre

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.ActivityComponentInjector
import com.frolo.muse.di.ActivityComponentProvider
import com.frolo.player.Player
import com.frolo.muse.router.AppRouter
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetGenreSongsUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Genre
import com.frolo.music.model.Song
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class GenreVMFactory constructor(
    injector: ActivityComponentInjector,
    provider: ActivityComponentProvider,
    private val genreArg: Genre
): ViewModelProvider.Factory {

    @Inject
    internal lateinit var player: Player
    @Inject
    internal lateinit var permissionChecker: PermissionChecker
    /*assisted inject*/
    internal lateinit var getGenreSongsUseCase: GetGenreSongsUseCase
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
    internal lateinit var createGenreShortcutUseCase: CreateShortcutUseCase<Genre>
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
        getGenreSongsUseCase = provider
            .provideGetGenreSongsUseCaseFactory()
            .create(genreArg)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GenreViewModel(
            player,
            permissionChecker,
            getGenreSongsUseCase,
            getMediaMenuUseCase,
            clickMediaUseCase,
            playMediaUseCase,
            shareMediaUseCase,
            deleteMediaUseCase,
            getIsFavouriteUseCase,
            changeFavouriteUseCase,
            createSongShortcutUseCase,
            createGenreShortcutUseCase,
            schedulerProvider,
            appRouter,
            eventLogger,
            genreArg
        ) as T
    }

}