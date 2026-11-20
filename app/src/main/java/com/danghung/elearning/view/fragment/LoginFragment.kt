package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.danghung.elearning.CommonUtils
import com.danghung.elearning.databinding.FragmentLoginBinding
import com.danghung.elearning.view.dialog.LoginSuccessDialog
import com.danghung.elearning.viewmodel.LoginVM
import com.danghung.elearning.viewmodel.LoginVM.Companion.LOGIN
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.danghung.elearning.view.fragment.SecurityFragment.Companion.KEY_LOGIN_FINGER
import com.danghung.elearning.viewmodel.ProfileVM

class LoginFragment : BaseFragment<FragmentLoginBinding, LoginVM>(),
    LoginSuccessDialog.OnMenuCallBack {

    companion object {
        val TAG: String = LoginFragment::class.java.name
    }

    private var failCount = 0

    override fun getClassVM(): Class<LoginVM> = LoginVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentLoginBinding = FragmentLoginBinding.inflate(inflater, container, false)

    override fun initViews() {
        setupLoginUI()

        val token = CommonUtils.getInstance().getPref(LoginVM.KEY_TOKEN)
        val isFinger = CommonUtils.getInstance().getPref(KEY_LOGIN_FINGER) != null

        if (!token.isNullOrBlank() && isFinger) {
            binding.ivFingerprint.visibility = View.VISIBLE
            showBiometricPrompt()
        } else {
            // Không bật vân tay -> xóa sạch token cũ để tránh lưu thừa hoặc tự động đăng nhập
            if (!token.isNullOrBlank()) {
                CommonUtils.getInstance().clearPref(LoginVM.KEY_TOKEN)
            }
            binding.ivFingerprint.visibility = View.GONE
        }
    }

    private fun setupLoginUI() {
        binding.tvSignUp.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.ivFingerprint.setOnClickListener(this)
    }

    private fun clearAndForceLogin() {
        CommonUtils.getInstance().clearPref(LoginVM.KEY_TOKEN)
        CommonUtils.getInstance().clearPref(KEY_LOGIN_FINGER)
        failCount = 0
        binding.ivFingerprint.visibility = View.GONE
        notify("Authentication failed multiple times. Please log in again.")
        setupLoginUI()
    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.tvSignUp.id -> callBack.showFragment(RegisterFragment.TAG, null, true)
            binding.btnLogin.id -> handleLogin()
            binding.ivFingerprint.id -> showBiometricPrompt()
        }
    }
    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(requireContext())

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                failCount = 0
                callBack.showLoading()
                // Dùng ProfileVM để call getMyInfo xác thực token
                // Nếu token hết hạn -> app tự đẩy về Login (đã xử lý sẵn)
                viewModel.verifyToken()
            }

            override fun onAuthenticationFailed() {
                failCount++
                if (failCount >= 5) {
                    clearAndForceLogin()
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Người dùng bấm Cancel hoặc lỗi hệ thống -> cho nhập tk/mk
                setupLoginUI()
            }
        }

        val prompt = BiometricPrompt(this, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint Login")
            .setSubtitle("Verify your identity to continue")
            .setNegativeButtonText("Use Password")
            .build()

        prompt.authenticate(info)
    }

    private fun handleLogin() {
        val username = binding.edtUsername.text.toString()
        val password = binding.edtPassword.text.toString()
        if (username.isBlank() || password.isBlank()) {
            notify("Username and password must not be empty!")
            return
        }
        callBack.showLoad()
        viewModel.login(username, password)
    }

    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            LOGIN -> handleLoginSuccess()
            LoginVM.VERIFY_TOKEN -> handleLoginSuccess()
        }
    }

    private fun handleLoginSuccess() {
        val dialog = LoginSuccessDialog(requireContext())
        dialog.setCallBack(this)
        dialog.show()
    }

    override fun onMenu() = callBack.showFragment(MenuFragment.TAG, null, false)
}