package com.frolo.core.ui.carousel

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.frolo.core.ui.R
import com.frolo.player.AudioSource
import com.frolo.ui.StyleUtils


class CarouselView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?= null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr), ICarouselView {

    private val implView by lazy { createCarousel(context, attrs, defStyleAttr) }
    private val placeholderView: TextView by lazy {
        TextView(context, attrs, defStyleAttr).apply {
            setTextAppearance(StyleUtils.resolveStyleRes(context, R.attr.textAppearanceBody2))
        }
    }

    override val size: Int get() = implView.size

    init {
        addView(implView, 0)
        addView(placeholderView,
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
        )
        updateViewsVisibilities(0)
    }

    private fun createCarousel(
        context: Context,
        attrs: AttributeSet?= null,
        defStyleAttr: Int = 0
    ) = FrameCarouselView(context, attrs, defStyleAttr)

    private fun updateViewsVisibilities(itemCount: Int) {
        val transition = Fade().apply {
            addTarget(implView)
            addTarget(placeholderView)
            duration = 200L
        }
        TransitionManager.beginDelayedTransition(this, transition)
        val isEmpty = itemCount == 0
        implView.isVisible = !isEmpty
        placeholderView.isVisible = isEmpty
    }

    fun setPlaceholderText(@StringRes stringId: Int) {
        placeholderView.setText(stringId)
    }

    override fun registerCallback(callback: ICarouselView.CarouselCallback) {
        implView.registerCallback(callback)
    }

    override fun unregisterCallback(callback: ICarouselView.CarouselCallback) {
        implView.unregisterCallback(callback)
    }

    override fun invalidateData() {
        implView.invalidateData()
    }

    override fun submitList(list: List<AudioSource>?, commitCallback: Runnable?) {
        val callbackWrapper = Runnable {
            updateViewsVisibilities(list?.count() ?: 0)
            commitCallback?.run()
        }
        implView.submitList(list, callbackWrapper)
    }

    override fun setCurrentPosition(position: Int) {
        implView.setCurrentPosition(position)
    }
}