package com.frolo.muse.views.checkable

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.Checkable
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import com.frolo.muse.R
import com.frolo.muse.views.InsetDrawable
import com.frolo.ui.Screen
import com.frolo.ui.StyleUtils


class CheckableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): AppCompatImageView(context, attrs, defStyleAttr), Checkable {

    private var targetDrawable: Drawable? = null
    private var checkFlipDrawable: CheckFlipDrawable? = null

    private var checked = false

    init {
        val checkMarkDrawable: Drawable

        checkMarkDrawable = context.theme.obtainStyledAttributes(
                attrs, R.styleable.CheckableImageView, 0, 0).let { arr ->
            val value = if (arr.hasValue(R.styleable.CheckableImageView_checkMarkShape)) {
                arr.getInt(R.styleable.CheckableImageView_checkMarkShape, 0)
            } else 0
            arr.recycle()

            @ColorInt val colorSecondary = StyleUtils.resolveColor(context, R.attr.colorSecondary)
            @ColorInt val colorOnSecondary = StyleUtils.resolveColor(context, R.attr.colorOnSecondary)
            if (value == 0) {
                CheckMarkDrawable(colorSecondary, colorOnSecondary)
            } else {
                SqrCheckMarkDrawable(colorSecondary, colorOnSecondary)
            }
        }

        val insetCheckMarkDrawable = InsetDrawable(checkMarkDrawable, Screen.dp(context, 4f))
        checkFlipDrawable = CheckFlipDrawable(target = targetDrawable, checkMark = insetCheckMarkDrawable)
        super.setImageDrawable(checkFlipDrawable)
    }

    override fun isChecked() = checked

    override fun toggle() {
        checked = !checked
        invalidateFlipper(true)
    }

    override fun setChecked(checked: Boolean) {
        if (this.checked != checked) {
            this.checked = checked
            invalidateFlipper(true)
        }
    }

    fun setChecked(checked: Boolean, animate: Boolean) {
        if (this.checked != checked) {
            this.checked = checked
            invalidateFlipper(animate)
        }
    }

    private fun invalidateFlipper(animate: Boolean) {
        (drawable as? CheckFlipDrawable)?.apply {
            setChecked(checked, animate)
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        targetDrawable = drawable
        checkFlipDrawable?.setTarget(drawable)
    }

}