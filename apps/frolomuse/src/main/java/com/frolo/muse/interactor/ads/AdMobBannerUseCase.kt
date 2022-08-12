package com.frolo.muse.interactor.ads

import android.content.Context
import androidx.annotation.MainThread
import com.frolo.muse.BuildConfig
import com.frolo.muse.admob.AdMobs
import com.frolo.muse.admob.BannerState
import com.frolo.muse.android.firstPackageInstallTime
import com.frolo.muse.billing.Products
import com.frolo.muse.billing.TrialStatus
import com.frolo.muse.interactor.billing.PremiumManager
import com.frolo.muse.repository.AppLaunchInfoProvider
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.functions.Function3
import javax.inject.Inject


class AdMobBannerUseCase @Inject constructor(
    private val context: Context,
    private val premiumManager: PremiumManager,
    private val launchInfoProvider: AppLaunchInfoProvider,
    private val schedulerProvider: SchedulerProvider
) {

    private val testAdMob: Boolean = false

    // The default library banner id, hardcoded in the app code.
    private fun getDefaultLibraryBannerId(): String {
        error("Not implemented")
    }

    fun getLibraryBannerState(): Flowable<BannerState> {
        val remoteConfigsSource: Flowable<BannerState>
        if (testAdMob) {
            val state = BannerState(
                canBeShown = true,
                bannerId = getDefaultLibraryBannerId()
            )
            remoteConfigsSource = Flowable.just(state)
        } else {
            // Immediately fetch the configs from the server
            remoteConfigsSource = AdMobs.getAdMobRemoteConfigs(minimumFetchIntervalInSeconds = 0)
                .observeOn(schedulerProvider.main())
                .map { config ->
                    val bannerId = if (!config.libraryBannerId.isNullOrBlank()) {
                        config.libraryBannerId
                    } else {
                        getDefaultLibraryBannerId()
                    }
                    BannerState(
                        canBeShown = canBeShown(config),
                        bannerId = bannerId
                    )
                }
                .toFlowable()
        }

        val isPremiumPurchasedSource: Flowable<Boolean> =
            premiumManager.isProductPurchased(productId = Products.PREMIUM, forceCheckFromApi = true)

        val isTrialActivatedSource: Flowable<Boolean> =
            premiumManager.getTrialStatus().map { status -> status == TrialStatus.Activated }

        val sourceCombiner = Function3<BannerState, Boolean, Boolean, BannerState> { state, isPremiumPurchased, isTrialActivated ->
            when {
                isPremiumPurchased || isTrialActivated -> {
                    // The user has purchased the premium or the premium trial is activated,
                    // so he is completely free from advertising.
                    state.copy(canBeShown = false)
                }
                else -> {
                    // It goes as it is.
                    state
                }
            }
        }

        return Flowable.combineLatest(remoteConfigsSource, isPremiumPurchasedSource, isTrialActivatedSource, sourceCombiner)
                .observeOn(schedulerProvider.main())
    }

    @MainThread
    private fun canBeShown(config: AdMobs.AdMobRemoteConfigs): Boolean {
        val firstPackageInstallTime = context.firstPackageInstallTime
        if (config.thresholdInstallTime != null && firstPackageInstallTime < config.thresholdInstallTime) {
            return false
        }

        val launchCount = launchInfoProvider.launchCount
        if (config.thresholdOpenCount != null && launchCount < config.thresholdOpenCount) {
            return false
        }

        return config.isEnabled
    }

    companion object {
        private val DEBUG = BuildConfig.DEBUG

        @Deprecated("This is controlled from the remote configs")
        private const val INSTALL_TIME_THRESHOLD_FOR_AD = 1609459200L // 2020.01.01 00:00:00

        // The min open count to show ads to the user
        @Deprecated("This is controlled from the remote configs")
        private const val OPEN_COUNT_THRESHOLD_FOR_AD = 10

        private fun now(): Long = System.currentTimeMillis()
    }

}