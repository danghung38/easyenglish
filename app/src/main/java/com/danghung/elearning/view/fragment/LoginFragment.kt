package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.danghung.elearning.CommonUtils
import com.danghung.elearning.databinding.FragmentLoginBinding
import com.danghung.elearning.view.dialog.LoginSuccessDialog
import com.danghung.elearning.viewmodel.LoginVM
import com.danghung.elearning.viewmodel.LoginVM.Companion.KEY_TOKEN
import com.danghung.elearning.viewmodel.LoginVM.Companion.LOGIN


class LoginFragment : BaseFragment<FragmentLoginBinding, LoginVM>(),
    LoginSuccessDialog.OnMenuCallBack {
    companion object {
        val TAG: String = LoginFragment::class.java.name
    }

    override fun getClassVM(): Class<LoginVM> = LoginVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentLoginBinding = FragmentLoginBinding.inflate(inflater, container, false)

    override fun initViews() {
        val token = CommonUtils.getInstance().getPref(KEY_TOKEN)
        if (!token.isNullOrBlank()) {
            callBack.showFragment(MenuFragment.TAG, null, false)
            return
        }

        binding.tvSignUp.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.tvSignUp.id -> callBack.showFragment(RegisterFragment.TAG, null, true)
            binding.btnLogin.id -> handleLogin()
        }
    }

    private fun handleLogin() {
        val username = binding.edtUsername.text.toString()
        val password = binding.edtPassword.text.toString()
        if (username.isBlank() || password.isBlank()) {
            notify("Username and password must not be empty!")
            return
        }
        callBack.showLoading()
        viewModel.login(username, password)
    }


    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            LOGIN -> handleLoginSuccess()
        }
    }

    private fun handleLoginSuccess() {
        val dialog = LoginSuccessDialog(requireContext())
        dialog.setCallBack(this)
        dialog.show()
    }

    override fun onMenu() = callBack.showFragment(MenuFragment.TAG, null, false)

}