package com.danghung.elearning.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.io.File

object SecurityCheckUtils {

    data class SecurityIssue(
        val isCompromised: Boolean,
        val isRooted: Boolean = false,
        val reason: String = ""
    )

    fun checkDeviceSecurity(context: Context): SecurityIssue {
        if (isRooted()) {
            return SecurityIssue(
                isCompromised = true,
                isRooted = true,
                reason = "Your device is rooted. For content security reasons, this app cannot run on modified systems."
            )
        }
        if (isUsbDebuggingEnabled(context)) {
            return SecurityIssue(
                isCompromised = true,
                isRooted = false,
                reason = "USB Debugging is currently enabled. Please disable it in Developer Options to continue."
            )
        }
        if (isDeveloperOptionsEnabled(context)) {
            return SecurityIssue(
                isCompromised = true,
                isRooted = false,
                reason = "Developer Options is currently enabled. Please disable it in Settings to continue."
            )
        }
        return SecurityIssue(isCompromised = false)
    }

    private fun isDeveloperOptionsEnabled(context: Context): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) != 0
    }

    private fun isUsbDebuggingEnabled(context: Context): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED,
            0
        ) != 0
    }

    private fun isRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/system/xbin/daemonsu",
            "/sbin/magisk",
            "/system/bin/magisk"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }

        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val input = process.inputStream.bufferedReader().readLine()
            input != null
        } catch (e: Exception) {
            false
        } finally {
            process?.destroy()
        }
    }
}