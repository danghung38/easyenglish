package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.danghung.elearning.databinding.FragmentSecurityBinding
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_PROFILE
import com.danghung.elearning.viewmodel.SecurityVM
import com.danghung.elearning.viewmodel.SecurityVM.Companion.CHANGE_PASSWORD
import com.danghung.elearning.viewmodel.SecurityVM.Companion.LOGOUT


class SecurityFragment : BaseFragment<FragmentSecurityBinding, SecurityVM>() {
    companion object {
        val TAG: String = SecurityFragment::class.java.name
    }

    override fun getClassVM(): Class<SecurityVM> = SecurityVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSecurityBinding = FragmentSecurityBinding.inflate(inflater, container, false)

    override fun initViews() {
        binding.btnLogout.setOnClickListener(this)
        binding.btnUpdatePassword.setOnClickListener(this)
        setUpOnBackPressed()
    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.btnLogout.id -> handleLogout()
            binding.btnUpdatePassword.id -> handleChangePassword()
        }
    }

    private fun handleLogout() {
        callBack.showLoading()
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
                callBack.showLoading()
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

    private fun setUpOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    callBack.showFragment(MenuFragment.TAG, TYPE_PROFILE, false)
                }
            })
    }
}