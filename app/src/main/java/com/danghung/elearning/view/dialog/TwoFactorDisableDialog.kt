package com.danghung.elearning.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.view.Window
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable
import com.danghung.elearning.R
import com.danghung.elearning.databinding.ViewTwoFactorDisableBinding

class TwoFactorDisableDialog(
    context: android.content.Context,
    private val onDisable: (String, String) -> Unit
) : Dialog(context) {
    private val binding = ViewTwoFactorDisableBinding.inflate(layoutInflater)

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        binding.btnDisableTwoFactor.setOnClickListener {
            val password = binding.edtDisablePassword.text?.toString().orEmpty()
            val code = binding.edtDisableCode.text?.toString()?.trim().orEmpty()
            when {
                password.isBlank() -> binding.edtDisablePassword.error = context.getString(R.string.txt_password)
                code.length != 6 -> binding.edtDisableCode.error = context.getString(R.string.txt_2fa_code_hint)
                else -> onDisable(password, code)
            }
        }
        binding.btnCancelDisableTwoFactor.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.92f).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    fun setSubmitting(submitting: Boolean) {
        binding.btnDisableTwoFactor.isEnabled = !submitting
        binding.btnDisableTwoFactor.alpha = if (submitting) 0.55f else 1f
    }
}
