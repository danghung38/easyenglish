package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import com.danghung.elearning.R
import com.danghung.elearning.CommonUtils
import com.danghung.elearning.databinding.FragmentSecurityBinding
import com.danghung.elearning.utils.SessionTimeoutManager
import com.danghung.elearning.api.res.TwoFactorSetupRes
import com.danghung.elearning.api.res.UserRes
import com.danghung.elearning.view.dialog.TwoFactorDisableDialog
import com.danghung.elearning.view.dialog.TwoFactorSetupDialog
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_LEARN
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_PROFILE
import com.danghung.elearning.viewmodel.SecurityVM
import com.danghung.elearning.viewmodel.SecurityVM.Companion.CHANGE_PASSWORD
import com.danghung.elearning.viewmodel.SecurityVM.Companion.LOGOUT


class SecurityFragment : BaseFragment<FragmentSecurityBinding, SecurityVM>() {
    companion object {
        val TAG: String = SecurityFragment::class.java.name
        const val KEY_LOGIN_FINGER = "KEY_LOGIN_FINGER"
    }

    private var isTwoFactorEnabled = false
    private var setupDialog: TwoFactorSetupDialog? = null
    private var disableDialog: TwoFactorDisableDialog? = null

    override fun getClassVM(): Class<SecurityVM> = SecurityVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSecurityBinding = FragmentSecurityBinding.inflate(inflater, container, false)

    override fun initViews() {
        setupFingerprintSwitch() // thêm dòng này
        setupTwoFactorSwitch()
        viewModel.getMyInfo()
        binding.btnLogout.setOnClickListener(this)
        binding.btnUpdatePassword.setOnClickListener(this)
        binding.ivBack.setOnClickListener {
            callBack.showFragment(MenuFragment.TAG, TYPE_PROFILE, false)
        }
        setUpOnBackMenu(TYPE_PROFILE)
    }

    private fun setupTwoFactorSwitch() {
        binding.switchTwoFactor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked == isTwoFactorEnabled) return@setOnCheckedChangeListener

            if (isChecked) {
                setTwoFactorState(false)
                callBack.showLoad()
                viewModel.setupTwoFactor()
            } else {
                setTwoFactorState(true)
                showDisableTwoFactorDialog()
            }
        }
    }

    private fun setTwoFactorState(enabled: Boolean) {
        isTwoFactorEnabled = enabled
        binding.switchTwoFactor.setOnCheckedChangeListener(null)
        binding.switchTwoFactor.isChecked = enabled
        binding.tvTwoFactorStatus.setText(
            if (enabled) R.string.txt_2fa_enabled else R.string.txt_2fa_disabled
        )
        binding.switchTwoFactor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked == isTwoFactorEnabled) return@setOnCheckedChangeListener
            if (isChecked) {
                setTwoFactorState(false)
                callBack.showLoad()
                viewModel.setupTwoFactor()
            } else {
                setTwoFactorState(true)
                showDisableTwoFactorDialog()
            }
        }
    }

    private fun showDisableTwoFactorDialog() {
        disableDialog?.dismiss()
        disableDialog = TwoFactorDisableDialog(requireContext()) { password, code ->
            disableDialog?.setSubmitting(true)
            callBack.showLoad()
            viewModel.disableTwoFactor(password, code)
        }.also { it.show() }
    }

    private fun showSetupTwoFactorDialog(setup: TwoFactorSetupRes) {
        val secret = setup.secretKey
        val qrCodeUri = setup.qrCodeUri
        if (secret.isNullOrBlank() || qrCodeUri.isNullOrBlank()) {
            setTwoFactorState(false)
            notify("Could not create the 2FA setup information")
            return
        }

        setupDialog?.dismiss()
        setupDialog = TwoFactorSetupDialog(requireContext(), secret, qrCodeUri) { code ->
            setupDialog?.setSubmitting(true)
            callBack.showLoad()
            viewModel.verifyEnableTwoFactor(code)
        }.also { it.show() }
    }

    private fun setupFingerprintSwitch() {
        val isEnabled = CommonUtils.getInstance().getPref(KEY_LOGIN_FINGER) != null

        // Tạm bỏ listener, set trạng thái, rồi mới gắn lại
        binding.switchFingerprint.setOnCheckedChangeListener(null)
        binding.switchFingerprint.isChecked = isEnabled
        binding.switchFingerprint.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkBiometricAndEnable()
            } else {
                CommonUtils.getInstance().clearPref(KEY_LOGIN_FINGER)
            }
        }
    }

    private fun checkBiometricAndEnable() {
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                CommonUtils.getInstance().savePref(KEY_LOGIN_FINGER, "true")
                notify("Fingerprint login has been enabled")
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                notify("This device does not support fingerprint authentication")
                binding.switchFingerprint.isChecked = false
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                notify("Fingerprint sensor is currently unavailable")
                binding.switchFingerprint.isChecked = false
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                notify("No fingerprint has been set up on this device")
                binding.switchFingerprint.isChecked = false
            }

            else -> {
                notify("Unable to enable fingerprint login")
                binding.switchFingerprint.isChecked = false
            }
        }
    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.btnLogout.id -> handleLogout()
            binding.btnUpdatePassword.id -> handleChangePassword()
        }
    }

    private fun handleLogout() {
        callBack.showLoad()
        viewModel.logout()
    }

    private fun handleChangePassword() {
        val oldPass = binding.edtOldPassword.text.toString().trim()
        val newPass = binding.edtNewPassword.text.toString().trim()
        val confirmPass = binding.edtConfirmPassword.text.toString().trim()
        when {
            oldPass.isBlank() || newPass.isBlank() || confirmPass.isBlank() -> notify("Password must not be empty")
            newPass.length < 6 -> notify("Password must be at least 6 characters")
            newPass != confirmPass -> notify("Confirm password does not match")
            else -> {
                callBack.showLoad()
                viewModel.changePassword(oldPass, newPass)
            }
        }
    }

    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            SecurityVM.GET_MY_INFO -> {
                val user = data as? UserRes ?: return
                setTwoFactorState(user.is2FA)
            }
            SecurityVM.SETUP_2FA -> showSetupTwoFactorDialog(data as? TwoFactorSetupRes ?: return)
            SecurityVM.VERIFY_ENABLE_2FA -> {
                setupDialog?.dismiss()
                setupDialog = null
                setTwoFactorState(true)
                notify("Two-factor authentication enabled")
            }
            SecurityVM.DISABLE_2FA -> {
                disableDialog?.dismiss()
                disableDialog = null
                setTwoFactorState(false)
                notify("Two-factor authentication disabled")
            }
            LOGOUT -> {
                SessionTimeoutManager.reset()
                notify("Logout successful!")
                callBack.showFragment(LoginFragment.TAG, null, false)
            }

            CHANGE_PASSWORD -> clearPasswordInput()
        }
    }

    override fun handleApiError(key: String, code: Int, error: com.danghung.elearning.api.res.ApiErrorRes?) {
        when (key) {
            SecurityVM.SETUP_2FA -> setTwoFactorState(false)
            SecurityVM.VERIFY_ENABLE_2FA -> setupDialog?.setSubmitting(false)
            SecurityVM.DISABLE_2FA -> {
                disableDialog?.setSubmitting(false)
                setTwoFactorState(true)
            }
        }
    }

    private fun clearPasswordInput() {
        notify("Password changed successfully")
        binding.edtOldPassword.text?.clear()
        binding.edtNewPassword.text?.clear()
        binding.edtConfirmPassword.text?.clear()
    }
}
