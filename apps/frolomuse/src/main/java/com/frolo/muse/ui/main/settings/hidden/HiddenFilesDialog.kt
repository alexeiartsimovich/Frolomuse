package com.frolo.muse.ui.main.settings.hidden

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.frolo.muse.R
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.ui.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_hidden_files.*


class HiddenFilesDialog : BaseDialogFragment() {

    private val viewModel: HiddenFilesViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setContentView(R.layout.dialog_hidden_files)
            loadUI(this)

            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            setupDialogSize(this, 10 * width / 11, 10 * height / 11)
        }
    }

    private fun loadUI(dialog: Dialog) =  with(dialog) {
        btn_ok.setOnClickListener {
            dismiss()
        }

        rv_files.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = HiddenFileAdapter {
                viewModel.onRemoveClick(it)
            }
            ContextCompat.getDrawable(context, R.drawable.list_thin_divider_tinted)?.also { d ->
                val decor = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                decor.setDrawable(d)
                addItemDecoration(decor)
            }
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        hiddenFiles.observeNonNull(owner) {
            dialog?.apply {
                (rv_files.adapter as? HiddenFileAdapter)?.submitList(it)
            }
        }

        placeholderVisible.observeNonNull(owner) {
            dialog?.apply {
                view_placeholder.visibility = if (it) View.VISIBLE else View.GONE
            }
        }

        isLoading.observeNonNull(owner) {
            dialog?.apply {
                pb_loading.visibility = if (it) View.VISIBLE else View.GONE
            }
        }

        error.observeNonNull(owner) {
            postError(it)
        }
    }

    companion object {
        fun newInstance() = HiddenFilesDialog()
    }

}