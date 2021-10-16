@file:Suppress("TestFunctionName")

package com.frolo.muse

import android.content.Context
import com.frolo.muse.di.impl.local.LibraryConfiguration
import com.frolo.muse.model.media.SongFilter
import com.frolo.muse.repository.SongFilterProvider
import io.reactivex.Flowable
import java.util.concurrent.Executor


internal fun TestLibraryConfiguration(context: Context): LibraryConfiguration {
    val songFilterProvider = SongFilterProvider { Flowable.just(SongFilter.allEnabled()) }
    val queryExecutor = Executor { it.run() }
    return LibraryConfiguration(context, songFilterProvider, queryExecutor)
}