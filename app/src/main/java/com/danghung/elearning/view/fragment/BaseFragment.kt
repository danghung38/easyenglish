package com.danghung.elearning.view.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.danghung.elearning.R
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.danghung.elearning.CommonUtils
import com.danghung.elearning.OnAPICallBack
import com.danghung.elearning.api.res.ApiErrorRes
import com.danghung.elearning.view.OnMainCallback
import com.danghung.elearning.view.dialog.ErrorDialog
import com.danghung.elearning.viewmodel.BaseViewModel
import com.danghung.elearning.viewmodel.LoginVM.Companion.KEY_TOKEN

abstract class BaseFragment<V: ViewBinding, M: BaseViewModel> : Fragment(), View.OnClickListener, OnAPICallBack, ErrorDialog.OnMenuCallBack {
    protected lateinit var mContext: Context
    protected lateinit var binding: V

    protected lateinit var viewModel: M
    protected lateinit var callBack: OnMainCallback
    protected var data: Any? = null

    fun setOnCallBack(callBack: OnMainCallback) {
        this.callBack = callBack
    }

    fun setAttachData(data: Any?) {
        this.data = data
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = initViewBinding(inflater, container)
        viewModel = ViewModelProvider(this)[getClassVM()]
        viewModel.setOnCallBack(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    @SuppressLint("PrivateResource")
    override fun onClick(v: View) {
        v.startAnimation(AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in))
        clickView(v)
    }

    protected open fun clickView(v: View){
        //do nothing
    }


    abstract fun initViews()

    abstract fun getClassVM(): Class<M>

    abstract fun initViewBinding(inflater: LayoutInflater, container: ViewGroup?): V

    protected fun notify(msg: String?) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    protected fun notify(msg: Int) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun apiSuccess(key: String, data: Any?) {
        callBack.hideLoading()
        handleApiSuccess(key, data)
    }

    override fun apiError(key: String, code: Int, data: Any?) {
        callBack.hideLoading()
        if (code == 401) {
            CommonUtils.getInstance().clearPref(KEY_TOKEN)
            notify(R.string.txt_err_expired_login)
            callBack.showFragment(LoginFragment.TAG, null, false)
            return
        }else if(code == 999){
            notify(R.string.txt_server_error)
            var dialog = ErrorDialog(requireContext())
            dialog.setCallBack(this)
            dialog.show()
        }
        else{
            val error = data as? ApiErrorRes
            notify("${error?.message ?: R.string.txt_err_expired_login}")
            handleApiError(key, code, error)
        }
    }

    protected open fun handleApiSuccess(key: String, data: Any?){
        //do nothing
    }

    protected open fun handleApiError(key: String, code: Int, error: ApiErrorRes?){
        //do nothing
    }

    override fun onExit() {
        requireActivity().finish()
    }

    override fun onRetry() {
        callBack.showLoading()
        viewModel.retryLastApi()
    }

}