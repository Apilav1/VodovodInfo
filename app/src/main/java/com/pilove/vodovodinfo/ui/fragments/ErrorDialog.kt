package com.pilove.vodovodinfo.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pilove.vodovodinfo.R

class ErrorDialog : DialogFragment() {

    private var yesListener: (() -> Unit)? = null

    fun setYesListener(listener: () -> Unit) {
        yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getText(R.string.ERROR_DIALOG_TITLE))
            .setMessage(R.string.ERROR_DIALOG_MESSAGE)
            .setIcon(R.drawable.ic_baseline_error_outline_24)
            .setPositiveButton(getText(R.string.ERROR_DIALOG_YES)) { _, _ ->
                yesListener?.let { yes ->
                    yes()
                }
            }
            .setNegativeButton(getText(R.string.ERROR_DIALOG_NO)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
    }
}