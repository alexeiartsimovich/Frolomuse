package com.frolo.muse.interactor.ads

import com.frolo.muse.firebase.FirebaseRemoteConfigCache
import io.reactivex.Single
import javax.inject.Inject


class LibraryAdsUseCase @Inject constructor() {
    fun isOneXBetAdsEnabled(): Single<Boolean> {
        return FirebaseRemoteConfigCache.getBool("one_xbet_ads_enabled")
    }
}