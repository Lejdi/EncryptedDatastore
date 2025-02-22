package pl.lejdi.encrypteddatastore.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import pl.lejdi.encrypteddatastore.encryption.decrypt
import pl.lejdi.encrypteddatastore.encryption.encrypt
import pl.lejdi.encrypteddatastore.encryption.generateKey
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.spec.GCMParameterSpec

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "encrypted_datastore")

class EncryptedDataStore(context: Context, private val gson: Gson) {
    private val dataStore = context.dataStore

    suspend fun insert(key: String, value: Any) {
        generateKey(key)
        val dataToSave = dataToByteArray(value)
        val encryptionResult = encrypt(dataToSave, key, getEncryptionAlgorithm())
        val dataKey = stringPreferencesKey(key)
        val ivKey = stringPreferencesKey(getIvKey(key))
        dataStore.edit { preferences ->
            preferences[dataKey] = String(encryptionResult.first, Charsets.ISO_8859_1)
            preferences[ivKey] = String(encryptionResult.second, Charsets.ISO_8859_1)
        }
    }

    suspend fun <T> get(key: String, className: Class<T>): T? {
        try {
            val preferences = dataStore.data.first()

            val dataKey = stringPreferencesKey(key)
            val ivKey = stringPreferencesKey(getIvKey(key))
            val encryptedData = preferences[dataKey] ?: ""
            val iv = preferences[ivKey] ?: ""

            if (!(encryptedData.isEmpty() || iv.isEmpty())) {
                val restoredData = decrypt(
                    encryptedData.toByteArray(Charsets.ISO_8859_1),
                    key,
                    getEncryptionAlgorithm(),
                    getAlgorithmParameterSpec(iv.toByteArray(Charsets.ISO_8859_1))
                ) ?: return null
                return gson.fromJson(String(restoredData, Charsets.ISO_8859_1), className)
            }
            return null
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun remove(key: String) {
        val dataKey = stringPreferencesKey(key)
        val ivKey = stringPreferencesKey(getIvKey(key))
        dataStore.edit { preferences ->
            preferences[dataKey] = ""
            preferences[ivKey] = ""
        }
    }

    protected fun getEncryptionAlgorithm() = "AES/GCM/NoPadding"

    protected fun getAlgorithmParameterSpec(vararg additionalParams: Any) : AlgorithmParameterSpec {
        val iv = additionalParams[0] as ByteArray
        return GCMParameterSpec(128, iv)
    }

    private fun dataToByteArray(data: Any): ByteArray {
        val json = gson.toJson(data)
        return json.toByteArray(Charsets.ISO_8859_1)
    }

    private fun getIvKey(key: String): String {
        return key + "_iv"
    }
}
