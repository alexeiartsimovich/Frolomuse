package com.frolo.muse.ui.main.library.artists.artist.songs

import com.frolo.player.Player
import com.frolo.muse.router.AppRouter
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetArtistSongsUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Artist
import com.frolo.music.model.Song
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsSongCollectionViewModel
import com.frolo.muse.ui.main.library.base.AssociatedWithMediaItem


class SongsOfArtistViewModel constructor(
    player: Player,
    permissionChecker: PermissionChecker,
    getArtistSongsUseCase: GetArtistSongsUseCase,
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
    eventLogger: EventLogger,
    private val artistArg: Artist
): AbsSongCollectionViewModel<Song>(
    player,
    permissionChecker,
    getArtistSongsUseCase,
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
), AssociatedWithMediaItem by AssociatedWithMediaItem(artistArg)