package com.esm.esmwallet.security


import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Manages cryptographic keys within the Android Keystore.
 * This class is responsible for generating, retrieving, and deleting AES keys used for encryption.
 */
class KeyStoreManager {

    private val KEY_ALIAS = "esm_wallet_aes_key" // Alias for our AES key in Keystore
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    /**
     * Retrieves the AES SecretKey from the Android Keystore.
     * If the key does not exist, it generates a new one.
     * @return The AES SecretKey.
     */
    fun getOrCreateSecretKey(): SecretKey {
        return (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: generateNewSecretKey()
    }

    /**
     * Generates a new AES SecretKey and stores it in the Android Keystore.
     * The key is configured to be used for encryption/decryption with GCM mode and NoPadding.
     * It requires user authentication (fingerprint/face unlock) for key usage on Android 9+ (API 28+).
     * This setup ensures that the key cannot be used unless the user authenticates.
     *
     * IMPORTANT: For Android 9+ (API 28+), if `setUserAuthenticationRequired(true)` is set,
     * you MUST ensure user authentication happens before using the key for decryption/encryption.
     * For simplicity in this initial MVP, we'll keep `setUserAuthenticationRequired(false)`
     * to avoid requiring biometric prompt at every encryption/decryption, but for a real wallet,
     * this should ideally be `true` for sensitive operations like signing.
     * We will manage user authentication for sensitive operations at the application level (e.g., PIN).
     *
     * For `isInvalidatedByBiometricEnrollment` (Android 7.0+):
     * If true, the key is invalidated when new fingerprints/face data are enrolled.
     * This is a security feature to prevent attackers from enrolling their biometrics
     * and then using the existing keys. For a wallet, this is highly recommended.
     *
     * @return The newly generated AES SecretKey.
     * @throws Exception if key generation fails.
     */
    private fun generateNewSecretKey(): SecretKey {
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE) // GCM does not require padding
                    .setKeySize(256) // AES-256
                    // For initial MVP simplicity and to avoid biometric prompt at every encryption/decryption:
                    // .setUserAuthenticationRequired(true) // Requires user authentication (e.g., fingerprint, face unlock) for key use
                    // .setUserAuthenticationValidityDurationSeconds(-1) // Valid for a long time after authentication
                    .setRandomizedEncryptionRequired(true) // Ensures IV is randomized per encryption
                    .setInvalidatedByBiometricEnrollment(true) // Invalidate if biometrics change (highly recommended for security)
                    .build()
            )
        }.generateKey()
    }

    /**
     * Deletes the AES SecretKey from the Android Keystore.
     * This is useful for resetting the wallet or when the user wants to remove sensitive data.
     */
    fun deleteSecretKey() {
        keyStore.deleteEntry(KEY_ALIAS)
    }
}