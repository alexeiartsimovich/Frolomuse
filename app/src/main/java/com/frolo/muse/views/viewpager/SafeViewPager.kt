package com.frolo.muse.views.viewpager

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import com.frolo.logger.api.Logger


/**
 * This is an attempt to get rid of all defects of [ViewPager] widget.
 * The following bugs are fixed:
 * -https://fabric.io/frolovs-projects/android/apps/com.frolo.musp/issues/5cb8c183f8b88c29638ffecc?time=last-seven-days
 */
class SafeViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): ViewPager(context, attrs) {

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return try {
            super.onInterceptTouchEvent(ev)
        } catch (e: Throwable) {
            Logger.e(LOG_TAG, e)
            false
        }
    }

    private companion object {
        const val LOG_TAG = "SafeViewPager"
    }

}