package com.danghung.elearning.view.activity

import android.content.Intent
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.media3.common.BuildConfig
import com.danghung.elearning.CommonUtils
import com.danghung.elearning.databinding.ActivityHomeBinding
import com.danghung.elearning.utils.SecurityCheckUtils
import com.danghung.elearning.utils.SessionTimeoutManager
import com.danghung.elearning.view.dialog.SecurityErrorDialog
import com.danghung.elearning.view.fragment.LoginFragment
import com.danghung.elearning.view.fragment.SplashFragment
import com.danghung.elearning.viewmodel.CommomVM
import com.danghung.elearning.viewmodel.LoginVM
import com.danghung.elearning.utils.PrivacyShieldManager
class HomeActivity : BaseActivity<ActivityHomeBinding, CommomVM>() {

    private var securityDialog: SecurityErrorDialog? = null

    override fun getClassVM(): Class<CommomVM> = CommomVM::class.java

    override fun initViewBinding(): ActivityHomeBinding =
        ActivityHomeBinding.inflate(layoutInflater)

    override fun initViews() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        // bảo vệ màn hình Recent Apps (Màn mờ)
        PrivacyShieldManager.register(this)

        setupSessionTimeout()
        showFragment(SplashFragment.TAG, null, false)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // 1. Khi quay lại app: Gỡ màn che logo VÀ bật lại FLAG_SECURE ngay lập tức
            PrivacyShieldManager.hideShield()
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            // 2. Khi vuốt đa nhiệm / bấm Home: Phủ màn che logo lên trước,
            // sau đó tạm mở FLAG_SECURE để hệ thống chụp đúng tấm hình logo làm thumbnail
            PrivacyShieldManager.showShield()
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun setupSessionTimeout() {
        SessionTimeoutManager.init { requireBiometric ->
            if (requireBiometric) {
                Toast.makeText(
                    this,
                    "Session timed out. Please verify with fingerprint.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                CommonUtils.getInstance().clearPref(LoginVM.KEY_TOKEN)
                Toast.makeText(
                    this,
                    "Session expired due to inactivity. Please log in again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            // Đẩy về màn Login (nếu có vân tay, LoginFragment sẽ tự bật BiometricPrompt)
            showFragment(LoginFragment.TAG, null, false)
        }
    }

    override fun onResume() {
        super.onResume()
        verifyDeviceSecurity()
    }

    private fun verifyDeviceSecurity() {
        val securityIssue = SecurityCheckUtils.checkDeviceSecurity(this)

        if (securityIssue.isCompromised) {
            showSecurityDialog(securityIssue.reason, securityIssue.isRooted)
        } else {
            securityDialog?.dismiss()
            securityDialog = null
        }
    }

    private fun showSecurityDialog(message: String, isRooted: Boolean) {
        if (securityDialog == null) {
            securityDialog = SecurityErrorDialog(this).apply {
                setCallBack(object : SecurityErrorDialog.OnSecurityDialogCallBack {
                    override fun onOpenSettings() {
                        try {
                            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                        } catch (e: Exception) {
                            startActivity(Intent(Settings.ACTION_SETTINGS))
                        }
                    }

                    override fun onExit() {
                        finishAffinity()
                    }
                })
            }
        }

        securityDialog?.updateState(message, isRooted)
        if (securityDialog?.isShowing == false) {
            securityDialog?.show()
        }
    }

    override fun onDestroy() {
        PrivacyShieldManager.unregister()
        securityDialog?.dismiss()
        super.onDestroy()
    }
}