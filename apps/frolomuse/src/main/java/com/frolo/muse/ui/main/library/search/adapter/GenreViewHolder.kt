package com.frolo.muse.ui.main.library.search.adapter

import android.view.View
import com.frolo.music.model.Genre
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getNameString
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_genre.view.*


class GenreViewHolder(
    private val itemView: View,
    private val thumbnailLoader: ThumbnailLoader
): MediaAdapter.MediaViewHolder(itemView) {

    override val viewOptionsMenu: View? = itemView.view_options_menu

    fun bind(
        item: Genre,
        selected: Boolean,
        selectionChanged: Boolean,
        query: String
    ) {

        with(itemView) {
            tv_genre_name.text = highlight(text = item.getNameString(resources), part = query)

            thumbnailLoader.loadGenreThumbnail(item, imv_genre_art)

            imv_check.setChecked(selected, selectionChanged)

            isSelected = selected
        }
    }
}