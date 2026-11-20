package com.danghung.elearning.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.graphics.drawable.toDrawable
import com.danghung.elearning.databinding.ViewLoginSuccessBinding

class LoginSuccessDialog(context: Context) : Dialog(context), View.OnClickListener {
    private val binding: ViewLoginSuccessBinding = ViewLoginSuccessBinding.inflate(layoutInflater)
    private var callBack: OnMenuCallBack? = null

    fun setCallBack(callBack: OnMenuCallBack) {
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
        binding.btnGoHome.setOnClickListener(this)
    }

    @SuppressLint("PrivateResource")
    override fun onClick(v: View) {
        AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)
        when (v.id) {
            binding.btnGoHome.id -> {
                callBack?.onMenu()
                dismiss()
            }
        }
    }

    interface OnMenuCallBack {
        fun onMenu()
    }


}