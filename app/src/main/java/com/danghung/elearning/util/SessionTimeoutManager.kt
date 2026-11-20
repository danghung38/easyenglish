package com.danghung.elearning.utils

import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.danghung.elearning.CommonUtils
import com.danghung.elearning.view.fragment.SecurityFragment.Companion.KEY_LOGIN_FINGER
import com.danghung.elearning.viewmodel.LoginVM

object SessionTimeoutManager : DefaultLifecycleObserver {

    // Thời gian tối đa cho phép chạy nền (ví dụ: 5 phút = 5 * 60 * 1000L)
    private const val TIMEOUT_MS = 1 * 60 * 1000L

    private var backgroundTimestampMs: Long = 0L
    private var onSessionTimeoutListener: ((requireBiometric: Boolean) -> Unit)? = null

    fun init(onTimeout: (requireBiometric: Boolean) -> Unit) {
        this.onSessionTimeoutListener = onTimeout
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    // Gọi khi toàn bộ app bị ẩn xuống nền (bấm Home, tắt màn hình, chuyển app khác)
    override fun onStop(owner: LifecycleOwner) {
        val token = CommonUtils.getInstance().getPref(LoginVM.KEY_TOKEN)
        if (!token.isNullOrBlank()) {
            backgroundTimestampMs = SystemClock.elapsedRealtime()
        }
    }

    // Gọi khi người dùng mở lại app từ nền
    override fun onStart(owner: LifecycleOwner) {
        if (backgroundTimestampMs == 0L) return

        val elapsedMs = SystemClock.elapsedRealtime() - backgroundTimestampMs
        backgroundTimestampMs = 0L

        val token = CommonUtils.getInstance().getPref(LoginVM.KEY_TOKEN)
        if (token.isNullOrBlank()) return

        if (elapsedMs >= TIMEOUT_MS) {
            val hasBiometric = CommonUtils.getInstance().getPref(KEY_LOGIN_FINGER) != null
            onSessionTimeoutListener?.invoke(hasBiometric)
        }
    }

    // Reset mốc thời gian khi user chủ động đăng xuất
    fun reset() {
        backgroundTimestampMs = 0L
    }
}