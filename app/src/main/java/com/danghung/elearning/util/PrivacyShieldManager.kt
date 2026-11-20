package com.danghung.elearning.utils

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.danghung.elearning.R
import java.lang.ref.WeakReference

object PrivacyShieldManager {

    private var activityRef: WeakReference<Activity>? = null
    private var shieldView: View? = null

    fun register(activity: Activity) {
        this.activityRef = WeakReference(activity)
    }

    fun showShield() {
        val activity = activityRef?.get() ?: return
        if (activity.isFinishing || activity.isDestroyed) return

        val root = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        if (shieldView == null) {
            shieldView = LayoutInflater.from(activity).inflate(R.layout.view_privacy_shield, root, false)
        }

        // Đảm bảo không add trùng và luôn nằm trên cùng (z-index cao nhất)
        if (shieldView?.parent == null) {
            root.addView(shieldView)
            shieldView?.bringToFront()
        }
    }

    fun hideShield() {
        val activity = activityRef?.get() ?: return
        val root = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        shieldView?.let {
            if (it.parent != null) {
                root.removeView(it)
            }
        }
    }

    fun unregister() {
        hideShield()
        activityRef = null
        shieldView = null
    }
}