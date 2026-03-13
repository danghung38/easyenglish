package com.danghung.elearning.view.fragment

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.danghung.elearning.R
import com.danghung.elearning.api.req.UserUpdateReq
import com.danghung.elearning.api.res.UserRes
import com.danghung.elearning.databinding.FragmentProfileBinding
import com.danghung.elearning.viewmodel.ProfileVM


class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfileVM>() {
    companion object {
        val TAG: String = ProfileFragment::class.java.name
    }

    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var tempAvatarUri: Uri? = null

    override fun getClassVM(): Class<ProfileVM> = ProfileVM::class.java
    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentProfileBinding = FragmentProfileBinding.inflate(inflater, container, false)

    override fun initViews() {
        setupSpinner()
        binding.trSecurity.setOnClickListener(this)
        callBack.showLoading()
        viewModel.getMyInfo()

        initPickAvatar()
        binding.frAvatar.setOnClickListener(this)
        binding.btnUpdate.setOnClickListener(this)
        setUpOnBackPressed()
    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.trSecurity.id -> callBack.showFragment(SecurityFragment.TAG, null, true)
            binding.frAvatar.id -> pickImageLauncher.launch("image/*")
            binding.btnUpdate.id -> handleUpdateUser()
        }
    }

    private fun handleUpdateUser() {
        val fullName = binding.edtFullName.text.toString().trim()
        val dob = binding.edtDateOfBirth.text.toString().trim()
        if (fullName.isEmpty() || dob.isEmpty()) {
            notify("Full name and Date of Birth not be empty")
            return
        }
        if (!viewModel.isValidDate(dob)) {
            notify("Date of Birth is not valid. Please use format dd/MM/yyyy")
            return
        }

        val req = UserUpdateReq(
            fullName,
            binding.spGender.selectedItem.toString(),
            dob,
            binding.spTarget.selectedItem.toString().toDoubleOrNull()
        )
        callBack.showLoading()
        viewModel.updateUser(requireContext(), req, tempAvatarUri)
    }

    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            ProfileVM.GET_MY_INFO -> {
                val user = data as UserRes
                bindUserInfo(user)
            }

            ProfileVM.UPDATE_USER -> {
                notify("Update user info successfully")
                viewModel.getMyInfo() // load lại info mới
            }
        }
    }

    private fun bindUserInfo(user: UserRes) {
        if (!isAdded || context == null || isDetached) {
            return
        }
        binding.tvName.text = user.name ?: ""
        binding.edtFullName.setText(user.name ?: "")
        binding.tvEmail.text = user.email ?: ""
        binding.tvPhone.text = user.phoneNumber ?: ""
        binding.edtDateOfBirth.setText(user.dob?.replace("-", "/") ?: "")
        // gender and target
        user.gender?.let {
            val pos = resources.getStringArray(R.array.gender_list).indexOf(it)
            if (pos >= 0) binding.spGender.setSelection(pos)
        }
        user.bandsTarget?.let {
            val pos = resources.getStringArray(R.array.target_list).indexOf(it.toString())
            if (pos >= 0) binding.spTarget.setSelection(pos)
        }
        user.avatar?.let {
            Glide.with(binding.ivAvatar).load(it).placeholder(R.drawable.ic_avt)
                .error(R.drawable.ic_avt).into(binding.ivAvatar)
        }
    }

    private fun setupSpinner() {
        val genderList = resources.getStringArray(R.array.gender_list)
        val adapterGender =
            object : ArrayAdapter<String>(requireContext(), R.layout.item_selected, genderList) {
                override fun getDropDownView(
                    position: Int, convertView: View?, parent: ViewGroup
                ): View {
                    val view =
                        convertView ?: layoutInflater.inflate(R.layout.item_dropdown, parent, false)
                    view.findViewById<TextView>(R.id.tvItem).text = getItem(position)
                    return view
                }
            }
        binding.spGender.adapter = adapterGender
        binding.spGender.setSelection(1)

        val targetList = resources.getStringArray(R.array.target_list)
        val adapterTarget =
            object : ArrayAdapter<String>(requireContext(), R.layout.item_selected, targetList) {
                override fun getDropDownView(
                    position: Int, convertView: View?, parent: ViewGroup
                ): View {
                    val view =
                        convertView ?: layoutInflater.inflate(R.layout.item_dropdown, parent, false)
                    view.findViewById<TextView>(R.id.tvItem).text = getItem(position)
                    return view
                }
            }
        binding.spTarget.adapter = adapterTarget
        binding.spTarget.setSelection(1)
    }

    private fun initPickAvatar() {
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                tempAvatarUri = it
                binding.ivAvatar.setImageURI(it) // preview ngay
            }
        }
    }

    private fun setUpOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                }
            })
    }

}