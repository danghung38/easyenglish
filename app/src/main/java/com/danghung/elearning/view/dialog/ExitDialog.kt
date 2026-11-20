package com.danghung.elearning.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.R
import androidx.core.graphics.drawable.toDrawable
import com.danghung.elearning.databinding.ViewExitBinding
import com.danghung.elearning.databinding.ViewStartExamBinding

class ExitDialog(
    context: Context,
) : Dialog(context), View.OnClickListener {

    private val binding: ViewExitBinding = ViewExitBinding.inflate(layoutInflater)
    private var callBack: OnExitCallBack? = null

    fun setCallBack(callBack: OnExitCallBack) {
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
        binding.btnExit.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
    }

    @SuppressLint("PrivateResource")
    override fun onClick(v: View) {
        AnimationUtils.loadAnimation(context, R.anim.abc_fade_in)
        when (v.id) {
            binding.btnExit.id -> {
                callBack?.onExit()
                dismiss()
            }
            binding.btnCancel.id -> {
                dismiss()
            }
        }
    }

    interface OnExitCallBack {
        fun onExit()
    }


}