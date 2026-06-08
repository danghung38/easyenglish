package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import com.danghung.elearning.CommonUtils
import com.danghung.elearning.databinding.FragmentSecurityBinding
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

    override fun getClassVM(): Class<SecurityVM> = SecurityVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSecurityBinding = FragmentSecurityBinding.inflate(inflater, container, false)

    override fun initViews() {
        setupFingerprintSwitch() // thêm dòng này
        binding.btnLogout.setOnClickListener(this)
        binding.btnUpdatePassword.setOnClickListener(this)
        binding.ivBack.setOnClickListener {
            callBack.showFragment(MenuFragment.TAG, TYPE_PROFILE, false)
        }
        setUpOnBackMenu(TYPE_PROFILE)
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
            LOGOUT -> {
                notify("Logout successful!")
                callBack.showFragment(LoginFragment.TAG, null, false)
            }

            CHANGE_PASSWORD -> clearPasswordInput()
        }
    }

    private fun clearPasswordInput() {
        notify("Password changed successfully")
        binding.edtOldPassword.text?.clear()
        binding.edtNewPassword.text?.clear()
        binding.edtConfirmPassword.text?.clear()
    }
}