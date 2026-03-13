package com.danghung.elearning.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.R
import androidx.core.graphics.drawable.toDrawable
import com.danghung.elearning.databinding.ViewSubmitSuccessBinding

class SubmitSuccessDialog(context: Context) : Dialog(context), View.OnClickListener {
    private val binding: ViewSubmitSuccessBinding = ViewSubmitSuccessBinding.inflate(layoutInflater)
    private var callBack: OnResultCallBack? = null

    fun setCallBack(callBack: OnResultCallBack) {
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
        binding.btnGoResult.setOnClickListener(this)
    }

    @SuppressLint("PrivateResource")
    override fun onClick(v: View) {
        AnimationUtils.loadAnimation(context, R.anim.abc_fade_in)
        when (v.id) {
            binding.btnGoResult.id -> {
                callBack?.onResult()
                dismiss()
            }
        }
    }

    interface OnResultCallBack {
        fun onResult()
    }

}