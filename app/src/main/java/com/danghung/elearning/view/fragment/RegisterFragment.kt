package com.danghung.elearning.view.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.danghung.elearning.R
import com.danghung.elearning.api.req.UserCreationReq
import com.danghung.elearning.databinding.FragmentRegisterBinding
import com.danghung.elearning.view.dialog.RegisterSuccessDialog
import com.danghung.elearning.viewmodel.RegisterVM
import com.danghung.elearning.viewmodel.RegisterVM.Companion.CREATE_USER


class RegisterFragment : BaseFragment<FragmentRegisterBinding, RegisterVM>(),
    RegisterSuccessDialog.OnLoginCallBack {
    companion object {
        val TAG: String = RegisterFragment::class.java.name
    }

    override fun getClassVM(): Class<RegisterVM> = RegisterVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentRegisterBinding = FragmentRegisterBinding.inflate(inflater, container, false)

    override fun initViews() {
        setupGender()
        binding.ivBack.setOnClickListener(this)
        binding.tvHaveAccount.setOnClickListener(this)
        binding.btnRegister.setOnClickListener(this)

    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.ivBack.id -> requireActivity().onBackPressedDispatcher.onBackPressed()
            binding.tvHaveAccount.id -> requireActivity().onBackPressedDispatcher.onBackPressed()
            binding.btnRegister.id -> registerUser()
        }
    }

    private fun registerUser() {
        val fullName = binding.edtFullName.text.toString().trim()
        val username = binding.edtUsername.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val phone = binding.edtPhoneNumber.text.toString().trim()
        val password = binding.edtPassword.text.toString()
        val confirmPassword = binding.edtConfirmPassword.text.toString()
        val dob = binding.edtDateOfBirth.text.toString().trim()
        val gender = binding.spGender.selectedItem.toString()

        val error = viewModel.verifyRegister(
            fullName = fullName,
            username = username,
            email = email,
            phoneNumber = phone,
            gender = gender,
            dob = dob,
            password = password,
            confirmPassword = confirmPassword
        )

        if (error != null) {
            notify(error)
            return
        }

        val req = UserCreationReq(
            fullName = fullName,
            username = username,
            email = email,
            phoneNumber = phone,
            gender = gender,
            dob = dob,
            password = password
        )

        callBack.showLoading()
        viewModel.register(req)
    }

    private fun setupGender() {
        val genderList = resources.getStringArray(R.array.gender_list)
        val adapter =
            object : ArrayAdapter<String>(requireContext(), R.layout.item_selected, genderList) {
                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    val view =
                        convertView ?: layoutInflater.inflate(R.layout.item_dropdown, parent, false)
                    view.findViewById<TextView>(R.id.tvItem).text = getItem(position)
                    return view
                }
            }
        binding.spGender.adapter = adapter
        binding.spGender.setSelection(0)
    }

    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            CREATE_USER -> handleRegisterSuccess()
        }
    }

    private fun handleRegisterSuccess() {
        val dialog = RegisterSuccessDialog(requireContext())
        dialog.setCallBack(this)
        dialog.show()
    }

    override fun onLoginNow() {
        callBack.showFragment(LoginFragment.TAG, null, false)
    }

}