package com.danghung.elearning.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.graphics.drawable.toDrawable
import com.danghung.elearning.databinding.ViewRegisterSuccessBinding

class RegisterSuccessDialog(context: Context) : Dialog(context), View.OnClickListener {
    private val binding: ViewRegisterSuccessBinding = ViewRegisterSuccessBinding.inflate(layoutInflater)
    private var callBack: OnLoginCallBack? = null

    fun setCallBack(callBack: OnLoginCallBack) {
        this.callBack = callBack
    }

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        initViews()
    }

    private fun initViews() {
        binding.btnLoginNow.setOnClickListener(this)
    }

    @SuppressLint("PrivateResource")
    override fun onClick(v: View) {
        AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)
        when (v.id) {
            binding.btnLoginNow.id -> {
                callBack?.onLoginNow()
                dismiss()
            }
        }
    }

    interface OnLoginCallBack {
        fun onLoginNow()
    }


}