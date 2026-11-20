package com.danghung.elearning.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.R
import androidx.core.graphics.drawable.toDrawable
import com.danghung.elearning.databinding.ViewErrorBinding

class SecurityErrorDialog(
    context: Context
) : Dialog(context), View.OnClickListener {

    private val binding: ViewErrorBinding = ViewErrorBinding.inflate(layoutInflater)
    private var callBack: OnSecurityDialogCallBack? = null

    fun setCallBack(callBack: OnSecurityDialogCallBack) {
        this.callBack = callBack
    }

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        initViews()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.tvTitle.text = "Security Alert"
        binding.btnRetry.text = "Settings"
        binding.btnExit.text = "Exit"

        binding.btnRetry.setOnClickListener(this)
        binding.btnExit.setOnClickListener(this)
    }

    fun updateState(message: String, isRooted: Boolean) {
        binding.tvDescription.text = message
        // Nếu bị Root thì ẩn nút Settings, chỉ giữ lại nút Exit
        binding.btnRetry.visibility = if (isRooted) View.GONE else View.VISIBLE
    }

    @SuppressLint("PrivateResource")
    override fun onClick(v: View) {
        v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.abc_fade_in))
        when (v.id) {
            binding.btnRetry.id -> {
                callBack?.onOpenSettings()
            }
            binding.btnExit.id -> {
                callBack?.onExit()
                dismiss()
            }
        }
    }

    interface OnSecurityDialogCallBack {
        fun onOpenSettings()
        fun onExit()
    }
}