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
import com.danghung.elearning.api.AuthApi
import com.danghung.elearning.api.req.RefreshReq
import com.danghung.elearning.api.res.ApiErrorRes
import com.danghung.elearning.api.res.ApiRes
import com.danghung.elearning.api.res.AuthRes
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
        const val REFRESH_TOKEN = "REFRESH_TOKEN"
    }

    protected var callBack: OnAPICallBack? = null

    protected val token: String?
        get() = CommonUtils.getInstance().getPref(LoginVM.KEY_TOKEN)

    protected val storedRefreshToken: String?
        get() = CommonUtils.getInstance().getPref(LoginVM.KEY_REFRESH_TOKEN)

    private var lastApiCall: (() -> Unit)? = null
    private val pendingApiCalls = mutableListOf<() -> Unit>()
    private var isRefreshingToken = false

    fun setOnCallBack(callBack: OnAPICallBack) {
        this.callBack = callBack
    }

    open fun requiresAuthenticatedSession(key: String): Boolean = true

    fun retryLastApi() {
        lastApiCall?.invoke()
    }

    protected fun executeApi(apiCall: () -> Unit) {
        lastApiCall = apiCall
        apiCall()
    }

    fun refreshToken() {
        val refreshToken = storedRefreshToken

        if (refreshToken.isNullOrBlank()) {
            callBack?.apiError(REFRESH_TOKEN, 401, null)
            return
        }

        if (isRefreshingToken) {
            lastApiCall?.let { pendingApiCalls.add(it) }
            return
        }

        lastApiCall?.let { pendingApiCalls.add(it) }
        isRefreshingToken = true

        // Lưu API cũ bị lỗi 401 để refresh xong gọi lại.

        // Không dùng executeApi ở đây.
        // Vì nếu dùng executeApi, lastApiCall sẽ bị ghi đè thành API refresh.
        api(AuthApi::class.java)
            .refresh(RefreshReq(refreshToken))
            .enqueue(initHandleResponse(REFRESH_TOKEN))
    }

    // countdown for exam
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
                Log.e(TAG, "onFailure: ${t.message}")
                handleException(key, t)
            }
        }
    }

    protected open fun handleException(key: String?, t: Throwable) {
        if (key == REFRESH_TOKEN) {
            isRefreshingToken = false
            pendingApiCalls.clear()
        }

        callBack?.apiError(key ?: "", 999, t.message)
    }

    protected open fun handleFail(key: String?, code: Int, errorBody: ResponseBody?) {
        if (key == REFRESH_TOKEN) {
            isRefreshingToken = false
            pendingApiCalls.clear()
        }

        val apiError = try {
            if (errorBody != null) {
                Gson().fromJson(errorBody.string(), ApiErrorRes::class.java)
            } else {
                ApiErrorRes(code, "Unknown error", null)
            }
        } catch (_: Exception) {
            ApiErrorRes(code, "Parse error", null)
        }

        callBack?.apiError(key ?: "", code, apiError)
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun handleSuccess(key: String?, data: Any?) {
        when (key) {
            REFRESH_TOKEN -> {
                isRefreshingToken = false

                val res = data as? ApiRes<AuthRes>
                val newAccessToken = res?.result?.accessToken
                val newRefreshToken = res?.result?.refreshToken

                if (newAccessToken.isNullOrBlank() || newRefreshToken.isNullOrBlank()) {
                    CommonUtils.getInstance().clearPref(LoginVM.KEY_TOKEN)
                    CommonUtils.getInstance().clearPref(LoginVM.KEY_REFRESH_TOKEN)
                    pendingApiCalls.clear()
                    callBack?.apiError(REFRESH_TOKEN, 401, null)
                    return
                }

                CommonUtils.getInstance().savePref(LoginVM.KEY_TOKEN, newAccessToken)
                CommonUtils.getInstance().savePref(LoginVM.KEY_REFRESH_TOKEN, newRefreshToken)

                val apiNeedRetry = pendingApiCalls.toList()
                pendingApiCalls.clear()

                apiNeedRetry.forEach { it.invoke() }
                return
            }
        }

        if (key != null) {
            callBack?.apiSuccess(key, data)
        }
    }
}
