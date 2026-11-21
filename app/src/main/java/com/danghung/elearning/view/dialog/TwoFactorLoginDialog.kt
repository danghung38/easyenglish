package com.danghung.elearning.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.view.Window
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable
import com.danghung.elearning.R
import com.danghung.elearning.databinding.ViewTwoFactorLoginBinding

class TwoFactorLoginDialog(
    context: android.content.Context,
    private val onVerify: (String) -> Unit
) : Dialog(context) {
    private val binding = ViewTwoFactorLoginBinding.inflate(layoutInflater)

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        binding.btnLoginTwoFactor.setOnClickListener {
            val code = binding.edtLoginTwoFactorCode.text?.toString()?.trim().orEmpty()
            if (code.length != 6) {
                binding.edtLoginTwoFactorCode.error = context.getString(R.string.txt_2fa_code_hint)
                return@setOnClickListener
            }
            onVerify(code)
        }
        binding.btnCancelLoginTwoFactor.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.92f).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    fun setSubmitting(submitting: Boolean) {
        binding.btnLoginTwoFactor.isEnabled = !submitting
        binding.btnLoginTwoFactor.alpha = if (submitting) 0.55f else 1f
    }
}
