package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.danghung.elearning.databinding.FragmentForgotPasswordBinding
import com.danghung.elearning.viewmodel.RecoveryVM
import com.danghung.elearning.viewmodel.RecoveryVM.Companion.FORGOT_PASSWORD

class ForgotPasswordFragment : BaseFragment<FragmentForgotPasswordBinding, RecoveryVM>() {
    companion object {
        val TAG: String = ForgotPasswordFragment::class.java.name
    }

    override fun getClassVM(): Class<RecoveryVM> = RecoveryVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentForgotPasswordBinding =
        FragmentForgotPasswordBinding.inflate(inflater, container, false)

    override fun initViews() {
        binding.btnSendOtp.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.ivBack.id -> requireActivity().onBackPressedDispatcher.onBackPressed()
            binding.btnSendOtp.id -> sendOtp()
        }
    }

    private fun sendOtp() {
        val email = binding.edtEmail.text?.toString()?.trim().orEmpty()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            notify("Please enter a valid email address")
            return
        }
        callBack.showLoad()
        viewModel.forgotPassword(email)
    }

    override fun handleApiSuccess(key: String, data: Any?) {
        if (key == FORGOT_PASSWORD) {
            callBack.showFragment(
                ResetPasswordFragment.TAG,
                binding.edtEmail.text?.toString()?.trim().orEmpty(),
                true
            )
        }
    }
}
