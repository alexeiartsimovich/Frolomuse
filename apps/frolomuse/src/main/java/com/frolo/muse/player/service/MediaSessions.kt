package com.frolo.muse.player.service

import android.graphics.Bitmap
import android.media.MediaMetadata
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import com.frolo.music.model.Song


/**
 * Builds [MediaMetadata] for the given [song] and [art].
 * [art] is only set in the metadata if [song] is not null.
 */
fun buildMediaMetadata(song: Song?, art: Bitmap?): MediaMetadata {
    val builder = MediaMetadata.Builder()

    if (song != null) {
        builder.apply {
            putString(MediaMetadata.METADATA_KEY_TITLE, song.title)
            putString(MediaMetadata.METADATA_KEY_ARTIST, song.artist)
            putString(MediaMetadata.METADATA_KEY_ALBUM, song.album)
            putString(MediaMetadata.METADATA_KEY_GENRE, song.genre)
            putLong(MediaMetadata.METADATA_KEY_YEAR, song.year.toLong())
            putLong(MediaMetadata.METADATA_KEY_TRACK_NUMBER, song.trackNumber.toLong())
            // We set the art only if the song is not null
            putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, art)
            putLong(MediaMetadata.METADATA_KEY_DURATION, song.duration.toLong())
        }
    } else {
        // Do NOT put the actual duration value in the metadata,
        // so that the seek bar is invisible.
        builder.putLong(MediaMetadata.METADATA_KEY_DURATION, -1L)
    }

    return builder.build()
}

fun buildEmptyMediaMetadata(): MediaMetadata = buildMediaMetadata(song = null, art = null)

fun MediaSessionCompat.setMetadata(song: Song?, art: Bitmap?) {
    val mediaMetadata = buildMediaMetadata(song = song, art = art)
    setMetadata(MediaMetadataCompat.fromMediaMetadata(mediaMetadata))
}

fun MediaSessionCompat.setEmptyMetadata() {
    val mediaMetadata = buildEmptyMediaMetadata()
    setMetadata(MediaMetadataCompat.fromMediaMetadata(mediaMetadata))
}