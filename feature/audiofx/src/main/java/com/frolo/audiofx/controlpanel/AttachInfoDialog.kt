package com.frolo.audiofx.controlpanel

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialog
import com.frolo.audiofx.AudioFx2AttachInfo
import com.frolo.audiofx.ui.R
import com.frolo.ui.Screen
import kotlinx.android.synthetic.main.dialog_attach_info.*

internal class AttachInfoDialog(
    context: Context,
    private val attachInfo: AudioFx2AttachInfo
): AppCompatDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_attach_info)
        setUpWindow()
        loadUi()
    }

    private fun setUpWindow() {
        val window = this.window ?: return
        val dialogWidth: Int = (Screen.getScreenWidth(context) * 0.95).toInt()
        val dialogHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT
        window.setLayout(dialogWidth, dialogHeight)
    }

    private fun loadUi() = with(this) {
        icon.setImageDrawable(attachInfo.icon)
        title.text = attachInfo.name
        description.text = attachInfo.description
        close.setOnClickListener { dismiss() }
    }
}