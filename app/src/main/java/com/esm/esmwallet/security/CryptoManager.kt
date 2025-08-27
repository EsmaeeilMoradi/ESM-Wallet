package com.esm.esmwallet.security

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

/**
 * Handles the encryption and decryption of sensitive data using the Android Keystore.
 * This class leverages AES-256 with GCM mode for secure data handling.
 */
class CryptoManager(private val keyStoreManager: KeyStoreManager) {

    private val cipher: Cipher by lazy { Cipher.getInstance("AES/GCM/NoPadding") }

    /**
     * Encrypts the provided plaintext (e.g., Private Key) using the AES key from Keystore.
     * @param plaintext The data to encrypt (e.g., Private Key as a ByteArray).
     * @return A ByteArray containing the encrypted data (ciphertext) combined with the IV.
     * The format is: [IV_LENGTH_IN_BYTES][IV][CIPHERTEXT].
     */
    fun encrypt(plaintext: ByteArray): ByteArray {
        val secretKey = keyStoreManager.getOrCreateSecretKey() // Get or create AES key

        // Generate a new IV for each encryption
        val iv = ByteArray(12) // GCM recommends 12-byte IVs
        SecureRandom().nextBytes(iv)
        val gcmParameterSpec = GCMParameterSpec(128, iv) // 128-bit authentication tag

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)
        val ciphertext = cipher.doFinal(plaintext)

        // Combine IV and ciphertext for storage
        val output = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, output, 0, iv.size)
        System.arraycopy(ciphertext, 0, output, iv.size, ciphertext.size)
        return output
    }

    /**
     * Decrypts the provided ciphertext (e.g., encrypted Private Key) using the AES key from Keystore.
     * @param encryptedData The data to decrypt, in the format: [IV_LENGTH_IN_BYTES][IV][CIPHERTEXT].
     * @return The original plaintext (e.g., Private Key) as a ByteArray.
     * @throws Exception if decryption fails (e.g., due to corrupted data or incorrect key).
     */
    fun decrypt(encryptedData: ByteArray): ByteArray {
        val secretKey = keyStoreManager.getOrCreateSecretKey() // Get the AES key

        // Extract IV and ciphertext from the stored data
        val iv = ByteArray(12)
        System.arraycopy(encryptedData, 0, iv, 0, iv.size)
        val ciphertext = ByteArray(encryptedData.size - iv.size)
        System.arraycopy(encryptedData, iv.size, ciphertext, 0, ciphertext.size)

        val gcmParameterSpec = GCMParameterSpec(128, iv)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
        return cipher.doFinal(ciphertext)
    }
}