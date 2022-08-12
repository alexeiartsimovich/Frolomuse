package com.frolo.muse.views.text

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.frolo.muse.R


class OptionTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.optionTextViewStyle,
    defStyleRes: Int = R.style.Base_AppTheme_OptionTextView
): LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    // Child views
    private val iconImageView: ImageView
    private val titleTextView: TextView

    private var iconScaleAnim: Animator? = null

    private var iconScale: Float = 1f
        set(value) {
            field = value
            iconImageView.scaleX = value
            iconImageView.scaleY = value
        }

    var optionTitle: String? = null
        set(value) {
            field = value
            titleTextView.text = value
        }

    var optionIcon: Drawable? = null
        set(value) {
            field = value
            iconImageView.setImageDrawable(value)
        }

    var optionIconSize: Int = 0
        set(value) {
            field = value
            iconImageView.updateLayoutParams<MarginLayoutParams> {
                width = value
                height = value
            }
        }

    var optionIconPadding: Int = 0
        set(value) {
            field = value
            iconImageView.updateLayoutParams<MarginLayoutParams> {
                marginEnd = value
            }
        }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        View.inflate(context, R.layout.merge_option_text_view, this)
        iconImageView = findViewById(R.id.imv_icon)
        titleTextView = findViewById(R.id.tv_title)

        initAttrs(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun initAttrs(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {
        val arr = context.theme.obtainStyledAttributes(
                attrs, R.styleable.OptionTextView, defStyleAttr, defStyleRes)

        optionTitle = arr.getString(R.styleable.OptionTextView_optionTitle)
        optionIcon = arr.getDrawable(R.styleable.OptionTextView_optionIcon)
        optionIconSize = arr.getDimension(R.styleable.OptionTextView_optionIconSize, 0f).toInt()
        optionIconPadding = arr.getDimension(R.styleable.OptionTextView_optionIconPadding, 0f).toInt()

        arr.recycle()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action

        if (action == MotionEvent.ACTION_DOWN) {
            animatePressDown()
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            animatePressUp()
        }

        return super.onTouchEvent(event)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        iconScale = 1f
    }

    override fun onDetachedFromWindow() {
        iconScaleAnim?.cancel()
        super.onDetachedFromWindow()
    }

    private fun animatePressDown() {
        iconScaleAnim?.cancel()

        iconScaleAnim = ValueAnimator.ofFloat(iconScale, 0.8f).apply {
            interpolator = SCALE_ANIM_INTERPOLATOR
            duration = 250
            addUpdateListener {
                iconScale = it.animatedValue as Float
            }
            start()
        }
    }

    private fun animatePressUp() {
        iconScaleAnim?.cancel()

        iconScaleAnim = ValueAnimator.ofFloat(iconScale, 1f).apply {
            interpolator = SCALE_ANIM_INTERPOLATOR
            duration = 50
            addUpdateListener {
                iconScale = it.animatedValue as Float
            }
            start()
        }
    }

    companion object {
        private val SCALE_ANIM_INTERPOLATOR = FastOutSlowInInterpolator()
    }

}