package com.frolo.muse.ui.main.library.artists.artist.albums

import com.frolo.muse.router.AppRouter
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetAlbumsOfArtistUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Album
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionViewModel


class AlbumsOfArtistViewModel constructor(
        permissionChecker: PermissionChecker,
        getAlbumsOfArtistUseCase: GetAlbumsOfArtistUseCase,
        getMediaMenuUseCase: GetMediaMenuUseCase<Album>,
        clickMediaUseCase: ClickMediaUseCase<Album>,
        playMediaUseCase: PlayMediaUseCase<Album>,
        shareMediaUseCase: ShareMediaUseCase<Album>,
        deleteMediaUseCase: DeleteMediaUseCase<Album>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<Album>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Album>,
        createShortcutUseCase: CreateShortcutUseCase<Album>,
        schedulerProvider: SchedulerProvider,
        appRouter: AppRouter,
        eventLogger: EventLogger
): AbsMediaCollectionViewModel<Album>(
        permissionChecker,
        getAlbumsOfArtistUseCase,
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