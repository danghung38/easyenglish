package com.danghung.elearning

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class CommonUtils private constructor() {
    companion object {
        const val PREF_FILE = "secure_pref_saving"
        private const val LEGACY_PREF_FILE = "pref_saving"

        @Volatile
        private var instance: CommonUtils? = null

        fun getInstance(): CommonUtils {
            return instance ?: synchronized(this) {
                instance ?: CommonUtils().also { instance = it }
            }
        }
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        val context = App.instance
        // Xóa file SharedPreferences thường cũ (chưa mã hóa) nếu còn tồn tại trên máy
        context.deleteSharedPreferences(LEGACY_PREF_FILE)
        createEncryptedPrefs(context)
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        return try {
            val masterKey =
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

            EncryptedSharedPreferences.create(
                context,
                PREF_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Phòng trường hợp keystore bị hỏng khi restore backup sang thiết bị khác
            context.deleteSharedPreferences(PREF_FILE)
            val masterKey =
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

            EncryptedSharedPreferences.create(
                context,
                PREF_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    fun savePref(key: String, value: String) {
        encryptedPrefs.edit { putString(key, value) }
    }

    fun getPref(key: String): String? {
        return encryptedPrefs.getString(key, null)
    }

    fun clearPref(key: String) {
        encryptedPrefs.edit { remove(key) }
    }

    fun clearAllPref() {
        encryptedPrefs.edit { clear() }
    }
}