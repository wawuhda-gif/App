package com.zivpn.app.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class CredentialsManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
        context,
        "vpn_credentials",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(host: String, password: String) {
        encryptedSharedPreferences.edit().apply {
            putString("vpn_host", host)
            putString("vpn_password", password)
            apply()
        }
    }

    fun getHost(): String = encryptedSharedPreferences.getString("vpn_host", "") ?: ""
    fun getPassword(): String = encryptedSharedPreferences.getString("vpn_password", "") ?: ""

    fun clearCredentials() {
        encryptedSharedPreferences.edit().clear().apply()
    }
}
