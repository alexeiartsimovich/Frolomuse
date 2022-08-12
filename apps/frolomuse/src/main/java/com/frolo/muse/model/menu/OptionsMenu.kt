package com.frolo.muse.model.menu

import com.frolo.music.model.Media

data class OptionsMenu<E : Media> constructor(
    val item: E,
    val favouriteOptionAvailable: Boolean,
    val isFavourite: Boolean,
    val shareOptionAvailable: Boolean,
    val deleteOptionAvailable: Boolean,
    val playOptionAvailable: Boolean,
    val playNextOptionAvailable: Boolean,
    val addToQueueOptionAvailable: Boolean,
    val editOptionAvailable: Boolean,
    val addToPlaylistOptionAvailable: Boolean,
    val viewLyricsOptionAvailable: Boolean,
    val viewAlbumOptionAvailable: Boolean,
    val viewArtistOptionAvailable: Boolean,
    val setAsDefaultOptionAvailable: Boolean,
    val addToHiddenOptionAvailable: Boolean,
    val scanFilesOptionAvailable: Boolean,
    val shortcutOptionAvailable: Boolean,
    val removeFromQueueOptionAvailable: Boolean
)
