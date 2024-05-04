package com.frolo.ads.onexbet

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.frolo.ui.Screen

class OneXBetAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        setBackgroundColor(Color.BLACK)
        foregroundGravity = Gravity.CENTER
        addView(createImageView(), LayoutParams.MATCH_PARENT,
            Screen.dp(context, PREFERRED_HEIGHT_IN_DP))
    }

    private fun createImageView(): View {
        return ImageView(context).also { imageView ->
            imageView.setBackgroundColor(ContextCompat.getColor(context, R.color.onexbet_background))
            Glide.with(imageView)
                .load(R.drawable.onexbet_logo)
                .fitCenter()
                .into(imageView)
        }
    }

//    override fun getSuggestedMinimumHeight(): Int {
//        return Screen.dp(context, PREFERRED_HEIGHT_IN_DP)
//    }

    companion object {
        private const val PREFERRED_HEIGHT_IN_DP = 80
    }
}