package com.frolo.muse.ui.main.settings.theme

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProviders
import com.frolo.muse.di.activityComponent
import com.frolo.ui.StyleUtils
import com.frolo.music.model.Album
import com.frolo.muse.ui.main.library.albums.album.AlbumFragment
import com.frolo.muse.ui.main.library.albums.album.AlbumVMFactory
import com.frolo.muse.ui.main.library.albums.album.AlbumViewModel
import java.io.Serializable


class ThemePreviewFragment : AlbumFragment() {

    @get:StyleRes
    private val themeResId: Int by lazy {
        requireArguments().getInt(ARG_THEME_RES_ID)
    }

    private val densityDpiFactor: Float by lazy {
        requireArguments().getFloat(ARG_DENSITY_DPI_FACTOR, 1f)
    }

    private val album: Album by lazy {
        requireArguments().getSerializable(ARG_ALBUM) as Album
    }

    override val viewModel: AlbumViewModel by lazy {
        val vmFactory = AlbumVMFactory(activityComponent, activityComponent, album)
        // Try using the parent fragment (if any) as the scope for the view model,
        // so that the album and songs are shared and loaded only once.
        ViewModelProviders.of(parentFragment ?: this, vmFactory)
            .get(AlbumViewModel::class.java)
    }

    private val superContext: Context? get() = super.getContext()

    /**
     * Creates context for the theme preview fragment. It has special theme and density dpi.
     */
    private fun createPreviewContext(context: Context): Context {
        val origConfiguration = context.resources.configuration

        // Creating new configuration with a special density dpi
        val newConfiguration = Configuration().apply {
            if (origConfiguration != null) {
                this.setTo(origConfiguration)
                //this.densityDpi = config.densityDpi
                this.densityDpi = (origConfiguration.densityDpi * densityDpiFactor).toInt()
            }
        }
        val newContext = context.createConfigurationContext(newConfiguration)

        // Creating themed context
        return ContextThemeWrapper(newContext, themeResId)
    }

    override fun getContext(): Context? {
        return superContext?.let(::createPreviewContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPlayButtonAlwaysVisible()
    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        return super.onGetLayoutInflater(savedInstanceState).run {
            cloneInContext(createPreviewContext(superContext ?: context))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.background = StyleUtils.resolveDrawable(view.context, android.R.attr.windowBackground)
    }

    override fun applyContentInsets(left: Int, top: Int, right: Int, bottom: Int) {
        // Do not clip
    }

    companion object {

        private const val ARG_THEME_RES_ID = "theme_res_id"
        private const val ARG_DENSITY_DPI_FACTOR = "density_dpi_factor"

        // Factory
        fun newInstance(album: Album, @StyleRes themeResId: Int, densityDpiFactor: Float) = ThemePreviewFragment().apply {
            arguments = bundleOf(
                ARG_ALBUM to (album as Serializable),
                ARG_THEME_RES_ID to themeResId,
                ARG_DENSITY_DPI_FACTOR to densityDpiFactor
            )
        }
    }

}