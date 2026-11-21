package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.danghung.elearning.databinding.FragmentResetPasswordBinding
import com.danghung.elearning.viewmodel.RecoveryVM
import com.danghung.elearning.viewmodel.RecoveryVM.Companion.RESET_PASSWORD

class ResetPasswordFragment : BaseFragment<FragmentResetPasswordBinding, RecoveryVM>() {
    companion object {
        val TAG: String = ResetPasswordFragment::class.java.name
    }

    override fun getClassVM(): Class<RecoveryVM> = RecoveryVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentResetPasswordBinding =
        FragmentResetPasswordBinding.inflate(inflater, container, false)

    override fun initViews() {
        binding.tvResetEmail.text = data as? String ?: ""
        binding.btnResetPassword.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.ivBack.id -> requireActivity().onBackPressedDispatcher.onBackPressed()
            binding.btnResetPassword.id -> resetPassword()
        }
    }

    private fun resetPassword() {
        val code = binding.edtResetCode.text?.toString()?.trim().orEmpty()
        val password = binding.edtNewPassword.text?.toString().orEmpty()
        val confirm = binding.edtConfirmPassword.text?.toString().orEmpty()
        when {
            !code.matches(Regex("\\d{8}")) -> notify("Please enter the 8-digit reset code")
            password.length < 6 -> notify("Password must be at least 6 characters")
            password != confirm -> notify("Passwords do not match")
            else -> {
                callBack.showLoad()
                viewModel.resetPassword(code, password)
            }
        }
    }

    override fun handleApiSuccess(key: String, data: Any?) {
        if (key == RESET_PASSWORD) {
            notify("Password reset successfully. Please log in.")
            callBack.showFragment(LoginFragment.TAG, null, false)
        }
    }
}
