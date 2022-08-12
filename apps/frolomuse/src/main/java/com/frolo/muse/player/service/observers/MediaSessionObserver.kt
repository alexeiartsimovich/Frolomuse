package com.frolo.muse.player.service.observers

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.MainThread
import com.frolo.muse.common.toSong
import com.frolo.player.AudioSource
import com.frolo.player.Player
import com.frolo.player.SimplePlayerObserver
import com.frolo.muse.player.service.setMetadata
import com.frolo.music.model.Song
import com.frolo.muse.rx.subscribeSafely
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


class MediaSessionObserver private constructor(
    private val context: Context,
    private val mediaSession: MediaSessionCompat
): SimplePlayerObserver() {

    @PlaybackStateCompat.Actions
    private val supportedActions: Long =
        PlaybackStateCompat.ACTION_PLAY or
            PlaybackStateCompat.ACTION_PAUSE or
            PlaybackStateCompat.ACTION_PLAY_PAUSE or
            PlaybackStateCompat.ACTION_SEEK_TO or
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

    private var songDisposable: Disposable? = null
    private var progressDisposable: Disposable? = null

    /**
     * Syncs the media session with the current playback state of [player].
     * Call this at some point when registering this player observer in the player,
     * so that the media session has a valid state at the very beginning
     * before any observer callback gets called.
     */
    private fun syncPlaybackState(player: Player) {
        setPlaybackState(
            isPlaying = player.isPlaying(),
            progress = player.getProgress().toLong(),
            speed = player.getSpeed()
        )
    }

    override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
        updateMetadataAsync(item)
        setUndefinedPlaybackState()
    }

    override fun onAudioSourceUpdated(player: Player, item: AudioSource) {
        updateMetadataAsync(item)
    }

    private fun updateMetadataAsync(item: AudioSource?) {
        val song: Song? = item?.toSong()
        songDisposable?.dispose()
        songDisposable = Arts.getPlaybackArt(context, song)
            .doOnSubscribe { mediaSession.setMetadata(song , null) }
            .doOnSuccess { art -> mediaSession.setMetadata(song , art) }
            .ignoreElement()
            .subscribeSafely()
    }

    override fun onPrepared(player: Player, duration: Int, progress: Int) {
        setPlaybackState(
            isPlaying = false,
            progress = progress.toLong(),
            speed = player.getSpeed()
        )
    }

    override fun onPlaybackStarted(player: Player) {
        progressDisposable = Flowable.interval(0L, PROGRESS_UPDATER_INTERVAL_IN_MS, TimeUnit.MILLISECONDS)
            .onBackpressureLatest()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                setPlaybackState(
                    isPlaying = true,
                    progress = player.getProgress().toLong(),
                    speed = player.getSpeed()
                )
            }
            .subscribeSafely()
    }

    override fun onPlaybackPaused(player: Player) {
        progressDisposable?.dispose()
        setPlaybackState(
            isPlaying = false,
            progress = player.getProgress().toLong(),
            speed = player.getSpeed()
        )
    }

    override fun onSoughtTo(player: Player, position: Int) {
        setPlaybackState(
            isPlaying = player.isPlaying(),
            progress = position.toLong(),
            speed = player.getSpeed()
        )
    }

    override fun onPlaybackSpeedChanged(player: Player, speed: Float) {
        setPlaybackState(
            isPlaying = player.isPlaying(),
            progress = player.getProgress().toLong(),
            speed = speed
        )
    }

    @MainThread
    private fun setUndefinedPlaybackState() {
        val playbackState = PlaybackStateCompat.Builder()
            // All the actions are still available; important for headsets!
            .setActions(supportedActions)
            .setState(
                // no state
                PlaybackStateCompat.STATE_NONE,
                // no position
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                // no speed
                0f
            )
            .build()
        mediaSession.setPlaybackState(playbackState)
    }

    @MainThread
    private fun setPlaybackState(isPlaying: Boolean, progress: Long, speed: Float) {
        val state: Int
        val actualSpeed: Float
        if (isPlaying) {
            state = PlaybackStateCompat.STATE_PLAYING
            actualSpeed = speed
        } else {
            state = PlaybackStateCompat.STATE_PAUSED
            actualSpeed = 0f
        }
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(supportedActions)
            .setState(state, progress, actualSpeed)
            .build()
        mediaSession.setPlaybackState(playbackState)
    }

    override fun onShutdown(player: Player) {
        songDisposable?.dispose()
        progressDisposable?.dispose()
    }

    companion object {
        private const val PROGRESS_UPDATER_INTERVAL_IN_MS = 1000L

        fun attach(context: Context, mediaSession: MediaSessionCompat, player: Player): MediaSessionObserver {
            return MediaSessionObserver(context, mediaSession).also { observer ->
                player.registerObserver(observer)
                observer.syncPlaybackState(player)
            }
        }
    }

}