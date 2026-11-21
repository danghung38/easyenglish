package com.danghung.elearning.utils

import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.danghung.elearning.CommonUtils
import com.danghung.elearning.view.fragment.SecurityFragment.Companion.KEY_LOGIN_FINGER
import com.danghung.elearning.viewmodel.LoginVM

object SessionTimeoutManager : DefaultLifecycleObserver {

    // Thời gian tối đa cho phép chạy nền (ví dụ: 1 phút)
    private const val TIMEOUT_MS = 1 * 60 * 1000L

    private var backgroundTimestampMs: Long = 0L
    private var onSessionTimeoutListener: ((requireBiometric: Boolean) -> Unit)? = null

    fun init(onTimeout: (requireBiometric: Boolean) -> Unit) {
        this.onSessionTimeoutListener = onTimeout
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    // Gọi khi toàn bộ app bị ẩn xuống nền (bấm Home, tắt màn hình, chuyển app khác)
    override fun onStop(owner: LifecycleOwner) {
        if (hasStoredSession()) {
            backgroundTimestampMs = SystemClock.elapsedRealtime()
        }
    }

    // Gọi khi người dùng mở lại app từ nền
    override fun onStart(owner: LifecycleOwner) {
        if (backgroundTimestampMs == 0L) return

        val elapsedMs = SystemClock.elapsedRealtime() - backgroundTimestampMs
        backgroundTimestampMs = 0L

        if (!hasStoredSession()) return

        if (elapsedMs >= TIMEOUT_MS) {
            val hasBiometric = CommonUtils.getInstance().getPref(KEY_LOGIN_FINGER) != null
            onSessionTimeoutListener?.invoke(hasBiometric)
        }
    }

    // Reset mốc thời gian khi user chủ động đăng xuất
    fun reset() {
        backgroundTimestampMs = 0L
    }

    private fun hasStoredSession(): Boolean {
        val accessToken = CommonUtils.getInstance().getPref(LoginVM.KEY_TOKEN)
        val refreshToken = CommonUtils.getInstance().getPref(LoginVM.KEY_REFRESH_TOKEN)
        return !accessToken.isNullOrBlank() || !refreshToken.isNullOrBlank()
    }
}
