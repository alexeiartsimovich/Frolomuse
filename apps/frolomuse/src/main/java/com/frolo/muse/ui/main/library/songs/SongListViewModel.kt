package com.frolo.muse.ui.main.library.songs

import com.frolo.player.Player
import com.frolo.muse.router.AppRouter
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetAllMediaUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Song
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsSongCollectionViewModel
import javax.inject.Inject


class SongListViewModel @Inject constructor(
    player: Player,
    permissionChecker: PermissionChecker,
    getAllSongsUseCase: GetAllMediaUseCase<Song>,
    getMediaMenuUseCase: GetMediaMenuUseCase<Song>,
    clickMediaUseCase: ClickMediaUseCase<Song>,
    playMediaUseCase: PlayMediaUseCase<Song>,
    shareMediaUseCase: ShareMediaUseCase<Song>,
    deleteMediaUseCase: DeleteMediaUseCase<Song>,
    getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>,
    changeFavouriteUseCase: ChangeFavouriteUseCase<Song>,
    createShortcutUseCase: CreateShortcutUseCase<Song>,
    schedulerProvider: SchedulerProvider,
    appRouter: AppRouter,
    eventLogger: EventLogger
): AbsSongCollectionViewModel<Song>(
    player,
    permissionChecker,
    getAllSongsUseCase,
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
)