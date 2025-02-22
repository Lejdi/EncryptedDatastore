package pl.lejdi.encrypteddatastore.encryption

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

private fun loadKeystore() : KeyStore {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)
    return keyStore
}

internal fun generateKey(alias: String) {
    val keyGenerator = KeyGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_AES,
        "AndroidKeyStore"
    )
    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
        alias,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setRandomizedEncryptionRequired(true)
        .build()
    keyGenerator.init(keyGenParameterSpec)
    keyGenerator.generateKey()
}

internal fun encrypt(data: ByteArray, alias: String, algorithm: String): Pair<ByteArray, ByteArray> {
    val keyStore = loadKeystore()

    val secretKeyEntry = keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry
    val secretKey = secretKeyEntry.secretKey

    val cipher = Cipher.getInstance(algorithm)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    return Pair(cipher.doFinal(data), cipher.iv)
}

internal fun decrypt(encryptedData: ByteArray, alias: String, algorithm: String, spec: AlgorithmParameterSpec): ByteArray? {
    return try {
        val keyStore = loadKeystore()

        val secretKeyEntry = keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry
        val secretKey = secretKeyEntry.secretKey

        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        cipher.doFinal(encryptedData)
    } catch (e: Exception) {
        null
    }
}