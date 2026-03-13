package com.danghung.elearning.view.fragment

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import com.danghung.elearning.databinding.FragmentSplashBinding
import com.danghung.elearning.viewmodel.CommomVM


class SplashFragment : BaseFragment<FragmentSplashBinding, CommomVM>() {
    companion object {
        val TAG: String = SplashFragment::class.java.name
    }

    override fun getClassVM(): Class<CommomVM> = CommomVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSplashBinding = FragmentSplashBinding.inflate(inflater, container, false)

    override fun initViews() {
        Handler(Looper.getMainLooper()).postDelayed({
            callBack.showFragment(OnboardingFragment.TAG, null, false)
        }, 2000)
    }

}