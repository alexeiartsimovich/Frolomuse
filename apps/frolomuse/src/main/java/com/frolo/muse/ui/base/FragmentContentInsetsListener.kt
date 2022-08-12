package com.frolo.muse.ui.base


/**
 * Listener for applying content insets to a fragment.
 */
fun interface FragmentContentInsetsListener {

    fun applyContentInsets(left: Int, top: Int, right: Int, bottom: Int)

}