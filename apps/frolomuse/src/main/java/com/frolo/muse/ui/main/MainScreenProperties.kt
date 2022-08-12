package com.frolo.muse.ui.main

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.Px
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import com.frolo.core.graphics.Palette
import com.frolo.debug.DebugUtils
import com.frolo.muse.R
import com.frolo.ui.ColorUtils2
import com.frolo.ui.Screen
import com.frolo.ui.StyleUtils


@UiThread
internal class MainScreenProperties(
    private val activity: Activity
) {

    private val context: Context get() = activity
    private val resources: Resources get() = context.resources

    private val whiteColorStateList = ColorStateList.valueOf(Color.WHITE)
    private val blackColorStateList = ColorStateList.valueOf(Color.BLACK)
    private val iconColorStateList =
        StyleUtils.resolveColorStateList(context, R.attr.iconTintMuted)
            ?: ColorStateList.valueOf(Color.GRAY)

    val isLandscape: Boolean get() = Screen.isLandscape(context)

    val isLightTheme: Boolean by lazy {
        StyleUtils.resolveBool(context, R.attr.isLightTheme)
    }

    @get:ColorInt
    val colorPrimary: Int by lazy {
        StyleUtils.resolveColor(context, R.attr.colorPrimary)
    }

    @get:ColorInt
    val colorPrimaryDark: Int by lazy {
        StyleUtils.resolveColor(context, R.attr.colorPrimaryDark)
    }

    @get:ColorInt
    val colorPrimarySurface: Int by lazy {
        StyleUtils.resolveColor(context, R.attr.colorPrimarySurface)
    }

    @get:ColorInt
    val colorSurface: Int by lazy {
        StyleUtils.resolveColor(context, R.attr.colorSurface)
    }

    @get:ColorInt
    val actionModeBackgroundColor: Int by lazy {
        try {
            StyleUtils.resolveColor(context, R.attr.actionModeBackground)
        } catch (error: Throwable) {
            // This is probably a drawable
            DebugUtils.dumpOnMainThread(error)
            StyleUtils.resolveColor(context, android.R.attr.navigationBarColor)
        }
    }

    @get:ColorInt
    val colorPlayerSurface: Int by lazy {
        StyleUtils.resolveColor(context, R.attr.colorPlayerSurface)
    }

//    @get:ColorInt
//    val colorOnPlayerSurface: Int by lazy {
//        StyleUtils.resolveColor(context, R.attr.colorOnPlayerSurface)
//    }

    @get:ColorInt
    val colorPlayerText: Int by lazy {
        StyleUtils.resolveColor(context, R.attr.colorPlayerText)
    }

    @get:ColorInt
    val colorPlayerElement1: Int by lazy {
        StyleUtils.resolveColor(context, R.attr.colorPlayerElement1)
    }

    @get:ColorInt
    val colorPlayerElement2: Int by lazy {
        StyleUtils.resolveColor(context, R.attr.colorPlayerElement2)
    }

    @get:ColorInt
    val colorPlayerElement3: Int by lazy {
        StyleUtils.resolveColor(context, R.attr.colorPlayerElement3)
    }

    @get:ColorInt
    val playerStatusBarBackground: Int by lazy {
        Color.TRANSPARENT //ContextCompat.getColor(context, R.color.player_status_bar_background)
    }

    @get:ColorInt
    val playerToolbarElementBackground: Int by lazy {
        ContextCompat.getColor(context, R.color.player_toolbar_element_background)
    }

    @get:ColorInt
    val colorModeOff: Int by lazy {
        colorPlayerElement1 //StyleUtils.resolveColor(context, R.attr.iconTintMuted)
    }

    @get:ColorInt
    val colorModeOn: Int by lazy {
        StyleUtils.resolveColor(context, R.attr.colorAccent)
    }

    @get:Px
    val playerSheetPeekHeight: Int by lazy {
        resources.getDimension(R.dimen.player_sheet_peek_height).toInt()
    }

    @get:Px
    val playerSheetCornerRadius: Int by lazy {
        resources.getDimension(R.dimen.player_sheet_corner_radius).toInt()
    }

    @get:ColorInt
    val defaultArtBackgroundColor: Int by lazy {
        validateArtBackgroundColorLightness(colorPrimary)
    }

    val ignoreArtBackgroundForStatusBar: Boolean get() = isLandscape

    @get:Dimension
    val bottomNavigationCornerRadius: Float by lazy {
        resources.getDimension(R.dimen.bottom_navigation_bar_corner_radius)
    }

    @get:ColorInt
    val transparentStatusBarColor: Int = Color.TRANSPARENT

    val fragmentContentInsets: Rect by lazy {
        val left = resources.getDimension(R.dimen.fragment_content_left_inset).toInt()
        val top = resources.getDimension(R.dimen.fragment_content_top_inset).toInt()
        val right = resources.getDimension(R.dimen.fragment_content_right_inset).toInt()
        val bottom = resources.getDimension(R.dimen.fragment_content_bottom_inset).toInt()
        Rect(left, top, right, bottom)
    }

    @ColorInt
    fun getModeColor(on: Boolean): Int {
        return if (on) colorModeOn else colorModeOff
    }

    @ColorInt
    fun extractArtBackgroundColor(palette: Palette?): Int {
        if (isLandscape) {
            return Color.TRANSPARENT
        }
        @ColorInt
        val colorFromPalette: Int? = when {
            palette == null -> null
//            isLightTheme -> palette.getSwatch(Palette.Target.DARK_MUTED)?.rgb
//            else -> palette.getSwatch(Palette.Target.LIGHT_MUTED)?.rgb
            else -> palette.getDominantSwatch()?.rgb
        }
        return colorFromPalette?.let(::validateArtBackgroundColorLightness)
            ?: defaultArtBackgroundColor
    }

    @ColorInt
    private fun validateArtBackgroundColorLightness(@ColorInt color: Int): Int {
        val originalLightness = ColorUtils2.getLightness(color)
        val targetLightness = if (isLightTheme) {
            originalLightness.coerceIn(0.05f, 0.6f)
        } else {
            originalLightness.coerceIn(0.4f, 0.85f)
        }
        return ColorUtils2.setLightness(color, targetLightness)
    }

    fun getPlayerToolbarElementColor(@ColorInt backgroundColor: Int): ColorStateList {
        if (isLandscape) {
            return iconColorStateList
        }
        val isLight = ColorUtils2.isLight(backgroundColor)
        return if (isLight) {
            blackColorStateList
        } else {
            whiteColorStateList
        }
    }

    fun getPlaceholderTextColor(@ColorInt artBackgroundColor: Int): Int {
        return getPlayerToolbarElementColor(artBackgroundColor).defaultColor
    }
}