package com.frolo.muse.ui.main.settings.premium

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.arch.support.EventLiveData
import com.frolo.arch.support.call
import com.frolo.arch.support.combine
import com.frolo.billing.ProductDetails
import com.frolo.muse.billing.Products
import com.frolo.muse.billing.TrialStatus
import com.frolo.muse.interactor.billing.PremiumManager
import com.frolo.muse.logger.*
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel


class BuyPremiumViewModel constructor(
    private val premiumManager: PremiumManager,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger,
    private val allowTrialActivation: Boolean
): BaseViewModel(eventLogger) {

    private val _closeEvent = EventLiveData<Unit>()
    val closeEvent: LiveData<Unit> get() = _closeEvent

    private val _showTrialActivationAndCloseEvent = EventLiveData<Unit>()
    val showTrialActivationAndCloseEvent: LiveData<Unit> get() = _showTrialActivationAndCloseEvent

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val productDetails: LiveData<ProductDetails> by lazy {
        MutableLiveData<ProductDetails>().apply {
            premiumManager.getProductDetails(Products.PREMIUM)
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { _isLoading.value = true }
                .doFinally { _isLoading.value = false }
                .doOnError { eventLogger.logFailedToGetProductDetails(Products.PREMIUM) }
                .subscribeFor { productDetails ->
                    value = productDetails
                }
        }
    }

    private val trialStatus: LiveData<TrialStatus> by lazy {
        MutableLiveData<TrialStatus>().apply {
            premiumManager.getTrialStatus()
                .observeOn(schedulerProvider.main())
                .subscribeFor { trialStatus ->
                    value = trialStatus
                }
        }
    }

    val premiumStatus: LiveData<PremiumStatus> by lazy {
        combine(productDetails, trialStatus) { productDetails, trialStatus ->
            PremiumStatus(productDetails, trialStatus, allowTrialActivation)
        }
    }

    val isButtonEnabled: LiveData<Boolean> by lazy {
        combine(isLoading, premiumStatus) { isLoading, premiumStatus ->
            isLoading != true && premiumStatus != null
        }
    }

    fun onButtonClicked() {
        val premiumStatus: PremiumStatus = premiumStatus.value ?: return
        if (premiumStatus.activateTrial) {
            premiumManager.activateTrialVersion()
                .observeOn(schedulerProvider.main())
                .doOnComplete { eventLogger.logPremiumTrialActivated() }
                .doOnError { eventLogger.logFailedToActivatePremiumTrial() }
                .subscribeFor { _showTrialActivationAndCloseEvent.call() }
        } else {
            // TODO: Launch and log only when we have product details
            val productId = Products.PREMIUM
            eventLogger.logLaunchedBillingFlow(productId)
            premiumManager.launchBillingFlow(productId)
                .observeOn(schedulerProvider.main())
                .subscribeFor { _closeEvent.call() }
        }
    }

    data class PremiumStatus(
        val productDetails: ProductDetails?,
        val trialStatus: TrialStatus?,
        val allowTrialActivation: Boolean
    ) {

        /**
         * If true, then the user will be prompted to activate the premium trial version first.
         * Otherwise, he can only buy premium.
         */
        val activateTrial: Boolean get() {
            return allowTrialActivation && trialStatus is TrialStatus.Available
        }

    }

}