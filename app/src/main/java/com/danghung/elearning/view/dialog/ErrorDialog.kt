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
import com.danghung.elearning.databinding.ViewLoginSuccessBinding

class ErrorDialog(context: Context) : Dialog(context), View.OnClickListener {
    private val binding: ViewErrorBinding = ViewErrorBinding.inflate(layoutInflater)
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
        binding.btnExit.setOnClickListener(this)
        binding.btnRetry.setOnClickListener(this)

    }

    @SuppressLint("PrivateResource")
    override fun onClick(v: View) {
        AnimationUtils.loadAnimation(context, R.anim.abc_fade_in)
        when (v.id) {
            binding.btnExit.id -> {
                callBack?.onExit()
                dismiss()
            }
            binding.btnRetry.id -> {
                callBack?.onRetry()
                dismiss()
            }
        }
    }

    interface OnMenuCallBack {
        fun onExit()
        fun onRetry()
    }


}