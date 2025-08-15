package com.fekraplatform.storemanger.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.secureDataStore by preferencesDataStore(name = "datastore_pm")
object TinkEncryptor {

    private const val KEYSET_NAME = "master_keyset"
    private const val PREF_FILE = "secure_prefs"
    private const val MASTER_KEY_URI = "android-keystore://master_key"

    fun getAead(context: Context): Aead {
        // Initialize Tink
        AeadConfig.register()

        val keysetHandle: KeysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREF_FILE)
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle

        return keysetHandle.getPrimitive(Aead::class.java)
    }

    fun encrypt(aead: Aead, plaintext: String, associatedData: ByteArray = byteArrayOf()): ByteArray {
        return aead.encrypt(plaintext.toByteArray(), associatedData)
    }

    fun decrypt(aead: Aead, ciphertext: ByteArray, associatedData: ByteArray = byteArrayOf()): String {
        return String(aead.decrypt(ciphertext, associatedData))
    }
}
class SecureDataStore(private val context: Context) {

    private val aead by lazy { TinkEncryptor.getAead(context) }

    suspend fun setSecureString(key: String, value: String) {
        val encrypted = TinkEncryptor.encrypt(aead, value)
        val encryptedBase64 = android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT)

        val dataKey = stringPreferencesKey(key)
        context.secureDataStore.edit { preferences ->
            preferences[dataKey] = encryptedBase64
        }
    }

    suspend fun getSecureString(key: String): String {
        val dataKey = stringPreferencesKey(key)
        val encryptedBase64 = context.secureDataStore.data
            .map { prefs -> prefs[dataKey] ?: "" }
            .first()

        return if (encryptedBase64.isNotEmpty()) {
            val encryptedBytes = android.util.Base64.decode(encryptedBase64, android.util.Base64.DEFAULT)
            TinkEncryptor.decrypt(aead, encryptedBytes)
        } else ""
    }

    suspend fun clearAll() {
        context.secureDataStore.edit { it.clear() }
    }
}
class StorageDataStore(private val context: Context) {

    suspend fun setData(key: String, value: String) {
        val dataKey = stringPreferencesKey(key)
        context.secureDataStore.edit { preferences ->
            preferences[dataKey] = value
        }
    }

    suspend fun getData(key: String): String {
        val dataKey = stringPreferencesKey(key)
        return context.secureDataStore.data
            .map { prefs -> prefs[dataKey] ?: "" }
            .first()
    }
}