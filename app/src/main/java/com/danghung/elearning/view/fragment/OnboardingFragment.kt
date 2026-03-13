package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.danghung.elearning.CommonUtils
import com.danghung.elearning.R
import com.danghung.elearning.databinding.FragmentOnboardingBinding
import com.danghung.elearning.model.OnboardingItem
import com.danghung.elearning.view.adapter.OnboardingAdapter
import com.danghung.elearning.viewmodel.OnboardingVM


class OnboardingFragment : BaseFragment<FragmentOnboardingBinding, OnboardingVM>() {
    companion object {
        val TAG: String = OnboardingFragment::class.java.name
        const val KEY_ONBOARDING_COMPLETED = "KEY_ONBOARDING_COMPLETED"
    }

    private lateinit var adapter: OnboardingAdapter


    override fun getClassVM(): Class<OnboardingVM> = OnboardingVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentOnboardingBinding = FragmentOnboardingBinding.inflate(inflater, container, false)

    override fun initViews() {
        val complete = CommonUtils.getInstance().getPref(KEY_ONBOARDING_COMPLETED)
        if (complete != null && complete == "true") {
            callBack.showFragment(LoginFragment.TAG, null, false)
            return
        }
        createOnboardingItems()
        adapter = OnboardingAdapter(viewModel.getOnboardingItems())
        binding.vpOnboarding.setAdapter(adapter)
        binding.dotOnboarding.attachTo(binding.vpOnboarding)
        binding.btnNext.setOnClickListener(this)
        binding.btnGetStarted.setOnClickListener(this)

        binding.vpOnboarding.registerOnPageChangeCallback(object :
            androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateBottomButton(position)
            }
        })
    }

    private fun updateBottomButton(position: Int) {
        val lastIndex = adapter.itemCount - 1
        if (position == lastIndex) {
            binding.btnNext.visibility = View.GONE
            binding.btnGetStarted.visibility = View.VISIBLE
        } else {
            binding.btnNext.visibility = View.VISIBLE
            binding.btnGetStarted.visibility = View.GONE
        }
    }


    private fun createOnboardingItems() {
        val items = listOf(
            OnboardingItem(
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1),
                R.drawable.ic_onboarding1
            ), OnboardingItem(
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2),
                R.drawable.ic_onboarding2
            ), OnboardingItem(
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3),
                R.drawable.ic_onboarding3
            ), OnboardingItem(
                getString(R.string.onboarding_title_4),
                getString(R.string.onboarding_desc_4),
                R.drawable.ic_onboarding4
            )
        )
        viewModel.getOnboardingItems().addAll(items)
    }

    override fun clickView(v: View) {
        val current = binding.vpOnboarding.currentItem
        val lastIndex = adapter.itemCount - 1

        if (current < lastIndex) {
            binding.vpOnboarding.currentItem = current + 1
        } else {
            completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        CommonUtils.getInstance().savePref(KEY_ONBOARDING_COMPLETED, "true")
        callBack.showFragment(LoginFragment.TAG, null, false)
    }

}