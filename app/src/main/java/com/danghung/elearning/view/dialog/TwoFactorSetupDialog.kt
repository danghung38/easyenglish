package com.danghung.elearning.view.dialog

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import com.danghung.elearning.R
import com.danghung.elearning.databinding.ViewTwoFactorSetupBinding

class TwoFactorSetupDialog(
    context: Context,
    secretKey: String,
    qrCodeUri: String,
    private val onVerify: (String) -> Unit
) : Dialog(context) {
    private val binding = ViewTwoFactorSetupBinding.inflate(layoutInflater)

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        binding.tvSecretKey.text = secretKey
        binding.ivQrCode.setImageBitmap(TwoFactorQrUtils.createQrCode(qrCodeUri))
        binding.btnCopySecret.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("EasyEnglish 2FA secret", secretKey))
            Toast.makeText(context, R.string.txt_copied, Toast.LENGTH_SHORT).show()
        }
        binding.btnVerifyTwoFactor.setOnClickListener {
            val code = binding.edtTwoFactorCode.text?.toString()?.trim().orEmpty()
            if (code.length != 6) {
                binding.edtTwoFactorCode.error = context.getString(R.string.txt_2fa_code_hint)
                return@setOnClickListener
            }
            onVerify(code)
        }
        binding.btnCancelTwoFactor.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.92f).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    fun setSubmitting(submitting: Boolean) {
        binding.btnVerifyTwoFactor.isEnabled = !submitting
        binding.btnVerifyTwoFactor.alpha = if (submitting) 0.55f else 1f
    }
}
