package com.danghung.elearning.view.activity

import android.content.Intent
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import com.danghung.elearning.CommonUtils
import com.danghung.elearning.databinding.ActivityHomeBinding
import com.danghung.elearning.utils.SecurityCheckUtils
import com.danghung.elearning.utils.SessionTimeoutManager
import com.danghung.elearning.view.dialog.SecurityErrorDialog
import com.danghung.elearning.view.fragment.SplashFragment
import com.danghung.elearning.viewmodel.CommomVM
import com.danghung.elearning.viewmodel.LoginVM

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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        }

        setupSessionTimeout()
        showFragment(SplashFragment.TAG, null, false)
    }

    override fun onResume() {
        super.onResume()
        verifyDeviceSecurity()
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
                CommonUtils.getInstance().clearPref(LoginVM.KEY_REFRESH_TOKEN)
                Toast.makeText(
                    this,
                    "Session expired due to inactivity. Please log in again.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            val intent = Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
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
                        } catch (_: Exception) {
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
        securityDialog?.dismiss()
        super.onDestroy()
    }
}
