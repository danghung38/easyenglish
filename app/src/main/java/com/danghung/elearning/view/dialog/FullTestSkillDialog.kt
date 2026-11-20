package com.danghung.elearning.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import com.danghung.elearning.api.res.UserExamPartRes
import com.danghung.elearning.databinding.ViewFulltestSkillPickerBinding
import com.danghung.elearning.view.adapter.FullTestSkillAdapter

class FullTestSkillDialog(
    context: Context,
    private val title: String,
    private val items: List<UserExamPartRes>
) : Dialog(context), View.OnClickListener, FullTestSkillAdapter.OnSkillClickListener {

    private val binding: ViewFulltestSkillPickerBinding =
        ViewFulltestSkillPickerBinding.inflate(layoutInflater)
    private var callBack: OnSkillSelectedCallBack? = null

    fun setCallBack(callBack: OnSkillSelectedCallBack) {
        this.callBack = callBack
    }

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        initViews()
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding.tvTitle.text = title
        binding.btnClose.setOnClickListener(this)
        binding.rvSkills.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = FullTestSkillAdapter(items, this@FullTestSkillDialog)
        }
    }

    @SuppressLint("PrivateResource")
    override fun onClick(v: View) {
        AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)
        when (v.id) {
            binding.btnClose.id -> dismiss()
        }
    }

    override fun onSkillClick(item: UserExamPartRes) {
        callBack?.onSkillSelected(item)
        dismiss()
    }

    interface OnSkillSelectedCallBack {
        fun onSkillSelected(item: UserExamPartRes)
    }
}
