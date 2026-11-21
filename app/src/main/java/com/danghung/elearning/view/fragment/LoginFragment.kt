package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.danghung.elearning.CommonUtils
import com.danghung.elearning.api.res.ApiErrorRes
import com.danghung.elearning.api.res.AuthRes
import com.danghung.elearning.databinding.FragmentLoginBinding
import com.danghung.elearning.view.dialog.LoginSuccessDialog
import com.danghung.elearning.view.dialog.TwoFactorLoginDialog
import com.danghung.elearning.viewmodel.LoginVM
import com.danghung.elearning.viewmodel.LoginVM.Companion.LOGIN
import com.danghung.elearning.viewmodel.LoginVM.Companion.GOOGLE_LOGIN
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.danghung.elearning.view.fragment.SecurityFragment.Companion.KEY_LOGIN_FINGER
import com.danghung.elearning.viewmodel.ProfileVM
import kotlinx.coroutines.launch

class LoginFragment : BaseFragment<FragmentLoginBinding, LoginVM>(),
    LoginSuccessDialog.OnMenuCallBack {

    companion object {
        val TAG: String = LoginFragment::class.java.name
    }

    private var failCount = 0
    private var twoFactorLoginDialog: TwoFactorLoginDialog? = null
    private var googleLoginInProgress = false

    override fun getClassVM(): Class<LoginVM> = LoginVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentLoginBinding = FragmentLoginBinding.inflate(inflater, container, false)

    override fun initViews() {
        setupLoginUI()

        val token = CommonUtils.getInstance().getPref(LoginVM.KEY_TOKEN)
        val refreshToken = CommonUtils.getInstance().getPref(LoginVM.KEY_REFRESH_TOKEN)
        val isFinger = CommonUtils.getInstance().getPref(KEY_LOGIN_FINGER) != null

        if (!token.isNullOrBlank() && !refreshToken.isNullOrBlank() && isFinger) {
            binding.ivFingerprint.visibility = View.VISIBLE
            showBiometricPrompt()
        } else {
            // Không bật vân tay -> xóa sạch token cũ để tránh lưu thừa hoặc tự động đăng nhập
            CommonUtils.getInstance().clearPref(LoginVM.KEY_TOKEN)
            CommonUtils.getInstance().clearPref(LoginVM.KEY_REFRESH_TOKEN)
            CommonUtils.getInstance().clearPref(KEY_LOGIN_FINGER)
            binding.ivFingerprint.visibility = View.GONE
        }
    }

    private fun setupLoginUI() {
        binding.tvSignUp.setOnClickListener(this)
        binding.tvForgotPassword.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.btnGoogleLogin.setOnClickListener(this)
        binding.ivFingerprint.setOnClickListener(this)
    }

    private fun clearAndForceLogin() {
        CommonUtils.getInstance().clearPref(LoginVM.KEY_TOKEN)
        CommonUtils.getInstance().clearPref(LoginVM.KEY_REFRESH_TOKEN)
        CommonUtils.getInstance().clearPref(KEY_LOGIN_FINGER)
        failCount = 0
        binding.ivFingerprint.visibility = View.GONE
        notify("Authentication failed multiple times. Please log in again.")
        setupLoginUI()
    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.tvSignUp.id -> callBack.showFragment(RegisterFragment.TAG, null, true)
            binding.tvForgotPassword.id -> callBack.showFragment(ForgotPasswordFragment.TAG, null, true)
            binding.btnLogin.id -> handleLogin()
            binding.btnGoogleLogin.id -> startGoogleLogin()
            binding.ivFingerprint.id -> showBiometricPrompt()
        }
    }

    private fun startGoogleLogin() {
        if (googleLoginInProgress) return

        val webClientId = getString(com.danghung.elearning.R.string.google_web_client_id)
        if (webClientId.isBlank()) {
            notify("Google Client ID is not configured")
            return
        }

        googleLoginInProgress = true
        binding.btnGoogleLogin.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val googleOption = GetSignInWithGoogleOption.Builder(
                    serverClientId = webClientId
                ).build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build()

                val activity = requireActivity()
                val credentialManager = CredentialManager.create(activity)
                val result = credentialManager.getCredential(
                    context = activity,
                    request = request
                )

                val credential = result.credential
                if (credential !is CustomCredential ||
                    credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    notify("Google sign-in configuration error")
                    return@launch
                }

                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleCredential.idToken
                if (idToken.isBlank()) {
                    notify("Google sign-in did not return an ID token")
                    return@launch
                }

                callBack.showLoad()
                viewModel.loginWithGoogle(idToken)
            } catch (_: GetCredentialCancellationException) {
                notify("Google sign-in was cancelled or failed")
            } catch (_: GetCredentialException) {
                notify("Google sign-in was cancelled or failed")
            } catch (_: GoogleIdTokenParsingException) {
                notify("Google sign-in did not return an ID token")
            } catch (_: Exception) {
                notify("Google sign-in was cancelled or failed")
            } finally {
                googleLoginInProgress = false
                binding.btnGoogleLogin.isEnabled = true
            }
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
            LOGIN, GOOGLE_LOGIN, LoginVM.LOGIN_2FA -> {
                val auth = data as? AuthRes
                if (auth?.requires2fa == true) {
                    showTwoFactorLoginDialog(auth.tempToken ?: return)
                } else {
                    twoFactorLoginDialog?.dismiss()
                    twoFactorLoginDialog = null
                    handleLoginSuccess()
                }
            }
            LoginVM.VERIFY_TOKEN -> handleLoginSuccess()
        }
    }

    private fun showTwoFactorLoginDialog(tempToken: String) {
        twoFactorLoginDialog?.dismiss()
        twoFactorLoginDialog = TwoFactorLoginDialog(requireContext()) { code ->
            twoFactorLoginDialog?.setSubmitting(true)
            callBack.showLoad()
            viewModel.loginWithTwoFactor(tempToken, code)
        }.also { it.show() }
    }

    override fun handleApiError(key: String, code: Int, error: ApiErrorRes?) {
        if (key == LoginVM.LOGIN_2FA) {
            twoFactorLoginDialog?.setSubmitting(false)
        }
    }

    private fun handleLoginSuccess() {
        val dialog = LoginSuccessDialog(requireContext())
        dialog.setCallBack(this)
        dialog.show()
    }

    override fun onMenu() = callBack.showFragment(MenuFragment.TAG, null, false)
}