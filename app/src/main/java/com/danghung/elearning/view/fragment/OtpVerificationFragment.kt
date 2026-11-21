package com.danghung.elearning.view.fragment

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.danghung.elearning.R
import com.danghung.elearning.databinding.FragmentOtpVerificationBinding
import com.danghung.elearning.viewmodel.RegisterVM
import com.danghung.elearning.viewmodel.RegisterVM.Companion.RESEND_VERIFICATION
import com.danghung.elearning.viewmodel.RegisterVM.Companion.VERIFY_REGISTER

class OtpVerificationFragment : BaseFragment<FragmentOtpVerificationBinding, RegisterVM>() {
    companion object {
        val TAG: String = OtpVerificationFragment::class.java.name
        private const val STATE_EMAIL = "verification_email"
    }

    private var email: String? = null
    private var requestedEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = savedInstanceState?.getString(STATE_EMAIL) ?: data as? String
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_EMAIL, email)
    }

    override fun getClassVM(): Class<RegisterVM> = RegisterVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOtpVerificationBinding =
        FragmentOtpVerificationBinding.inflate(inflater, container, false)

    override fun initViews() {
        updateVerificationStep()
        binding.btnVerifyOtp.setOnClickListener(this)
        binding.btnResendOtp.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
    }

    private fun updateVerificationStep() {
        val showOtp = !email.isNullOrBlank()
        binding.tvVerificationDescription.setText(
            if (showOtp) R.string.txt_verify_email_des else R.string.txt_verify_existing_email_des
        )
        binding.tvOtpEmail.text = email
        binding.tvOtpEmail.isVisible = showOtp
        binding.edtVerificationEmail.isVisible = !showOtp
        binding.edtOtp.isVisible = showOtp
        binding.btnVerifyOtp.isVisible = showOtp
        binding.btnResendOtp.setText(
            if (showOtp) R.string.txt_resend_otp else R.string.txt_send_verification_otp
        )
    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.ivBack.id -> requireActivity().onBackPressedDispatcher.onBackPressed()
            binding.btnVerifyOtp.id -> verifyOtp()
            binding.btnResendOtp.id -> resendOtp()
        }
    }

    private fun verifyOtp() {
        val otp = binding.edtOtp.text?.toString()?.trim().orEmpty()
        if (!otp.matches(Regex("\\d{8}"))) {
            notify(R.string.txt_verify_otp_invalid_length)
            return
        }
        callBack.showLoad()
        viewModel.verifyOtp(otp)
    }

    private fun resendOtp() {
        val targetEmail = email ?: binding.edtVerificationEmail.text.toString().trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(targetEmail).matches()) {
            binding.edtVerificationEmail.error = getString(R.string.txt_verify_email_invalid)
            binding.edtVerificationEmail.requestFocus()
            return
        }
        requestedEmail = targetEmail
        callBack.showLoad()
        viewModel.resendVerification(targetEmail)
    }

    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            VERIFY_REGISTER -> {
                notify(R.string.txt_verify_email_success)
                callBack.showFragment(LoginFragment.TAG, null, false)
            }
            RESEND_VERIFICATION -> {
                email = requestedEmail
                binding.edtOtp.text?.clear()
                updateVerificationStep()
                binding.edtOtp.requestFocus()
                notify(R.string.txt_verification_otp_sent)
            }
        }
    }
}
