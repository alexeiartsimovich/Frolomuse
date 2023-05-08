package com.frolo.muse.common

import com.frolo.player.AudioSource
import com.frolo.player.AudioSourceQueue
import com.frolo.music.model.Media
import com.frolo.music.model.Song
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


val TAG_ASSOCIATED_MEDIA = Any()
@Deprecated("Use TAG_ASSOCIATED_MEDIA")
val TAG_QUEUE_NAME = Any()
@Deprecated("Use TAG_ASSOCIATED_MEDIA")
val TAG_QUEUE_ID = Any()

fun AudioSourceQueue?.isNullOrEmpty(): Boolean {
    return this == null || isEmpty
}

fun AudioSourceQueue.indexOf(predicate: (item: AudioSource) -> Boolean): Int {
    for (i in 0 until length) {
        if (predicate(getItemAt(i))) {
            return i
        }
    }
    return -1
}

fun AudioSourceQueue.first(): AudioSource {
    return getItemAt(0)
}

fun AudioSourceQueue.findFirstOrNull(predicate: (item: AudioSource) -> Boolean): AudioSource? {
    for (i in 0 until length) {
        val item = getItemAt(i)
        if (predicate(item)) {
            return item
        }
    }
    return null
}

@Throws(NoSuchElementException::class)
fun AudioSourceQueue.find(predicate: (item: AudioSource) -> Boolean): AudioSource {
    return findFirstOrNull(predicate) ?: throw NoSuchElementException()
}

inline fun <T> AudioSourceQueue.map(transform: (AudioSource) -> T): List<T> {
    return snapshot.map(transform)
}

fun blockingCreateAudioSourceQueue(songs: List<Song>, associatedMediaItem: Media?): AudioSourceQueue {
    val audioSources = songs.toAudioSources()
    val queue = AudioSourceQueue.create(audioSources)
    if (associatedMediaItem != null) {
        queue.putTag(TAG_ASSOCIATED_MEDIA, associatedMediaItem)
    }
    return queue
}

fun blockingCreateAudioSourceQueue(song: Song): AudioSourceQueue {
    return blockingCreateAudioSourceQueue(listOf(song), song)
}

fun createAudioSourceQueue(songs: List<Song>, associatedMediaItem: Media?): Single<AudioSourceQueue> {
    val source = Single.fromCallable {
        blockingCreateAudioSourceQueue(songs, associatedMediaItem)
    }
    return source.subscribeOn(Schedulers.computation())
}

val AudioSourceQueue.associatedMedia: Media?
    get() {
        return this.getTag(TAG_ASSOCIATED_MEDIA) as? Media
    }