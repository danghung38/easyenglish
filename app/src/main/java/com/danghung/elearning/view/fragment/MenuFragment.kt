package com.danghung.elearning.view.fragment

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.danghung.elearning.R
import com.danghung.elearning.databinding.FragmentMenuBinding
import com.danghung.elearning.view.OnMainCallback
import com.danghung.elearning.viewmodel.CommomVM


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MenuFragment : BaseFragment<FragmentMenuBinding, CommomVM>(), OnMainCallback {
    companion object {
        val TAG: String = MenuFragment::class.java.name
        const val TYPE_HOME: Int = 1
        const val TYPE_EXAMS: Int = 2
        const val TYPE_LEARN: Int = 3
        const val TYPE_PROFILE: Int = 4
    }

    override fun getClassVM(): Class<CommomVM> = CommomVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMenuBinding = FragmentMenuBinding.inflate(inflater, container, false)

    override fun initViews() {

        binding.lnHome.setOnClickListener(this)
        binding.lnExams.setOnClickListener(this)
        binding.lnLearn.setOnClickListener(this)
        binding.lnProfile.setOnClickListener(this)

        //check quay về từ màn khác
        val menuType = data as? Int
        if (menuType != null) {
            showScreen(menuType)
        } else {
            showScreen(TYPE_HOME)
        }

    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.lnHome.id -> showScreen(TYPE_HOME)
            binding.lnExams.id -> showScreen(TYPE_EXAMS)
            binding.lnLearn.id -> showScreen(TYPE_LEARN)
            binding.lnProfile.id -> showScreen(TYPE_PROFILE)
        }
    }

    private fun showScreen(type: Int) {
        val colorNor = ContextCompat.getColor(requireContext(), R.color.color_nor)
        val tintNor = ColorStateList.valueOf(colorNor)
        listOf(
            binding.ivHome to binding.tvHome,
            binding.ivExams to binding.tvExams,
            binding.ivLearn to binding.tvLearn,
            binding.ivProfile to binding.tvProfile
        ).forEach { (iv, tv) ->
            iv.imageTintList = tintNor
            tv.setTextColor(colorNor)
        }
        when (type) {
            TYPE_HOME -> gotoScreen(binding.ivHome, binding.tvHome, R.string.txt_title_home, HomeFragment.TAG)
            TYPE_EXAMS -> gotoScreen(binding.ivExams, binding.tvExams, R.string.txt_title_exams, ExamsFragment.TAG)
            TYPE_LEARN -> gotoScreen(binding.ivLearn, binding.tvLearn, R.string.txt_title_learn, LearnFragment.TAG)
            TYPE_PROFILE -> gotoScreen(binding.ivProfile, binding.tvProfile, R.string.txt_title_profile, ProfileFragment.TAG)
        }
    }

    private fun gotoScreen(iv: ImageView, tv: TextView, title: Int, tag: String) {
        val colorApp = ContextCompat.getColor(requireContext(), R.color.color_app)
        val tintApp = ColorStateList.valueOf(colorApp)
        iv.imageTintList = tintApp
        tv.setTextColor(colorApp)
        binding.tvTitle.text = getString(title)
        showFragmentInMenu(tag, null)
    }

    fun showFragmentInMenu(tag: String, data: Any?) {
        try {
            val clazz = Class.forName(tag)
            val constructor = clazz.getConstructor()
            val baseFragment = constructor.newInstance() as BaseFragment<*, *>
            baseFragment.setAttachData(data)
            baseFragment.setOnCallBack(this)
            childFragmentManager.beginTransaction()
                .replace(R.id.fr_frg_main, baseFragment, tag)
                .commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun showFragment(tag: String, data: Any?, isBacked: Boolean) {
        callBack.showFragment(tag, data, isBacked)
    }

    override fun showLoading() {
        callBack.showLoading()
    }

    override fun hideLoading() {
        callBack.hideLoading()
    }

}