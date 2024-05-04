package com.frolo.muse.ui.main.library

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.interactor.ads.LibraryAdsUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseAndroidViewModel
import javax.inject.Inject

class LibraryViewModel @Inject constructor(
    application: Application,
    eventLogger: EventLogger,
    private val bannerUseCase: LibraryAdsUseCase,
    private val schedulerProvider: SchedulerProvider
): BaseAndroidViewModel(application, eventLogger) {

    private val _showOneXBetAds by lazy {
        MutableLiveData<Boolean>(false).apply {
            bannerUseCase.isOneXBetAdsEnabled()
                .observeOn(schedulerProvider.main())
                .subscribeFor { value = it }
        }
    }
    val showOneXBetAds: LiveData<Boolean> get() = _showOneXBetAds

    companion object {
        private const val LOG_TAG = "LibraryViewModel"
    }
}