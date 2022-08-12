package com.frolo.muse.ui.main.editor.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.di.ActivityComponentInjector
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Album
import com.frolo.music.repository.AlbumRepository
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class AlbumEditorVMFactory constructor(
    injector: ActivityComponentInjector,
    private val album: Album
): ViewModelProvider.Factory {

    @Inject
    internal lateinit var frolomuseApp: FrolomuseApp
    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var repository: AlbumRepository
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        injector.inject(this)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AlbumEditorViewModel(
            frolomuseApp,
            schedulerProvider,
            repository,
            eventLogger,
            album
        ) as T
    }

}