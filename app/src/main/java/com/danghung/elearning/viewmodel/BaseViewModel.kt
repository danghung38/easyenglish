package com.danghung.elearning.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danghung.elearning.CommonUtils
import com.danghung.elearning.OnAPICallBack
import com.danghung.elearning.api.ApiClient
import com.danghung.elearning.api.res.ApiErrorRes
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class BaseViewModel : ViewModel() {
    companion object {
        val TAG = BaseViewModel::class.java.name
    }

    protected var callBack: OnAPICallBack? = null

    protected val token: String?
        get() = CommonUtils.getInstance().getPref(LoginVM.KEY_TOKEN)

    fun setOnCallBack(callBack: OnAPICallBack) {
        this.callBack = callBack
    }

    var lastApiCall: (() -> Unit)? = null

    fun retryLastApi(){
        lastApiCall?.invoke()
    }

    protected fun executeApi(apiCall: () -> Unit) {
        lastApiCall = apiCall
        apiCall()
    }

    //countdown for exam
    private val _timeSeconds = MutableLiveData<Int>()
    private val _formattedTime = MutableLiveData<String>()
    val formattedTime: LiveData<String> = _formattedTime

    var job: Job? = null
    var autoSubmitted = false

    fun startCountdown(minutes: Int) {
        if (job?.isActive == true) return

        _timeSeconds.value = minutes * 60
        _formattedTime.value = formatTime(minutes * 60)

        job = viewModelScope.launch {
            while ((_timeSeconds.value ?: 0) > 0) {
                delay(1000)
                val remain = (_timeSeconds.value ?: 0) - 1
                _timeSeconds.value = remain
                _formattedTime.value = formatTime(remain)
            }
        }
    }

    fun isTimeUp(): Boolean = (_timeSeconds.value ?: 0) <= 0

    fun markAutoSubmitted() {
        autoSubmitted = true
    }

    fun isAutoSubmitted(): Boolean = autoSubmitted

    fun stopCountdown() {
        job?.cancel()
        job = null
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }

    // TẠO API THEO CLASS
    protected fun <T> api(clazz: Class<T>): T {
        return ApiClient.retrofit.create(clazz)
    }


    protected fun <T> initHandleResponse(key: String): Callback<T> {
        return object : Callback<T> {
            override fun onResponse(call: Call<T?>, response: Response<T?>) {
                if (response.code() == 200 || response.code() == 201) {
                    if (response.body() != null) {
                        handleSuccess(key, response.body())
                    } else {
                        handleFail(key, response.code(), response.errorBody())
                    }
                } else {
                    handleFail(key, response.code(), response.errorBody())
                }
            }

            override fun onFailure(call: Call<T?>, t: Throwable) {
                Log.e(TAG, "onFailure: " + t.message)
                handleException(key, t)
            }
        }
    }

    protected open fun handleException(key: String?, t: Throwable) {
        callBack!!.apiError(key!!, 999, t.message)
    }

    protected open fun handleFail(key: String?, code: Int, errorBody: ResponseBody? ) {
        val apiError = try {
            if (errorBody != null) {
                Gson().fromJson(errorBody.string(), ApiErrorRes::class.java)
            } else {
                ApiErrorRes(code, "Unknown error", null)
            }
        } catch (_: Exception) {
            ApiErrorRes(code, "Parse error", null)
        }
        callBack!!.apiError(key!!, code, apiError)
    }

    protected open fun handleSuccess(key: String?, data: Any?) {
        callBack!!.apiSuccess(key!!, data)
    }
}