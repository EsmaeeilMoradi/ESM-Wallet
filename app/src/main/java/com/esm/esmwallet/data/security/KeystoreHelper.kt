package com.esm.esmwallet.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class KeystoreHelper(private val context: Context) {

    private val KEYSTORE_ALIAS = "esm_wallet_private_key_alias" // Unique alias for our key
    private val ANDROID_KEYSTORE = "AndroidKeyStore"

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    // Generates a new AES key in the Android Keystore
    private fun getOrCreateSecretKey(): SecretKey {
        val existingKey = keyStore.getKey(KEYSTORE_ALIAS, null)
        if (existingKey != null && existingKey is SecretKey) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false) // For simplicity, no biometric prompt for now
                .setRandomizedEncryptionRequired(true) // Ensures a new IV is used for each encryption
                .build()
        )
        return keyGenerator.generateKey()
    }

    // Encrypts data using the Keystore key
    fun encrypt(data: ByteArray): Pair<ByteArray, ByteArray> { // Returns Pair<EncryptedData, IV>
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(data)
        val iv = cipher.iv // Initialization Vector (IV) is crucial for decryption
        return Pair(encryptedBytes, iv)
    }

    // Decrypts data using the Keystore key and provided IV
    fun decrypt(encryptedData: ByteArray, iv: ByteArray): ByteArray {
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        return cipher.doFinal(encryptedData)
    }

    companion object {
        private const val CIPHER_TRANSFORMATION = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
    }
}