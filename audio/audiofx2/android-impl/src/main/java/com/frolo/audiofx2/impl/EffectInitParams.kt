package com.frolo.audiofx2.impl

import android.media.MediaPlayer

internal data class EffectInitParams(
    val priority: Int,
    val audioSessionId: Int,
    val mediaPlayer: MediaPlayer?
)