package com.danghung.elearning.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.R
import androidx.core.graphics.drawable.toDrawable
import com.danghung.elearning.databinding.ViewStartExamBinding

class StartTestDialog(
    context: Context,
    private var skill: String
) : Dialog(context), View.OnClickListener {

    private val binding: ViewStartExamBinding = ViewStartExamBinding.inflate(layoutInflater)
    private var callBack: OnStartTestCallBack? = null

    fun setCallBack(callBack: OnStartTestCallBack) {
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
        binding.tvTitleSkill.text = "$skill TEST"
        binding.btnStart.setOnClickListener(this)
        binding.btnClose.setOnClickListener(this)
    }

    @SuppressLint("PrivateResource")
    override fun onClick(v: View) {
        AnimationUtils.loadAnimation(context, R.anim.abc_fade_in)
        when (v.id) {
            binding.btnStart.id -> {
                callBack?.onStart(skill = skill)
                dismiss()
            }
            binding.btnClose.id -> {
                dismiss()
            }
        }
    }

    interface OnStartTestCallBack {
        fun onStart(skill: String)
    }


}