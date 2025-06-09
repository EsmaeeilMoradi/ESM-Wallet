package com.esm.esmwallet.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey // Import for ByteArray
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull // For direct read (blocking but necessary for decryption flow)
import kotlinx.coroutines.flow.map
import com.esm.esmwallet.data.security.KeystoreHelper // Import the new KeystoreHelper
import android.util.Base64 // For Base64 encoding/decoding IV

// Creating a single DataStore for the whole application
val Context.walletDataStore by preferencesDataStore(name = "wallet_preferences")

class WalletDataStore(private val context: Context) {

    // --- OLD: Key for storing the encrypted Mnemonic ---
    // private val MNEMONIC_KEY = stringPreferencesKey("mnemonic_phrase_encrypted")

    // --- NEW: Keys for storing encrypted Private Key and its IV ---
    private val ENCRYPTED_PRIVATE_KEY = stringPreferencesKey("encrypted_private_key")
    private val ENCRYPTION_IV = stringPreferencesKey("encryption_iv") // IV needs to be saved alongside encrypted data

    // Key for storing the wallet address
    private val WALLET_ADDRESS_KEY = stringPreferencesKey("wallet_address")

    // Master Key for EncryptedSharedPreferences (We'll remove direct usage of this for Mnemonic, but keep if other sensitive data is stored here)
    // private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    // private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
    //     "encrypted_wallet_prefs",
    //     masterKeyAlias,
    //     context,
    //     EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    //     EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    // )

    // NEW: Instance of KeystoreHelper
    private val keystoreHelper = KeystoreHelper(context)

    // Save encrypted Private Key and its IV
    suspend fun saveEncryptedPrivateKey(privateKeyHex: String) {
        val (encryptedBytes, iv) = keystoreHelper.encrypt(privateKeyHex.toByteArray(Charsets.UTF_8))

        // Store encrypted bytes and IV as Base64 strings in DataStore
        context.walletDataStore.edit { preferences ->
            preferences[ENCRYPTED_PRIVATE_KEY] = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            preferences[ENCRYPTION_IV] = Base64.encodeToString(iv, Base64.NO_WRAP)
        }
    }

    // Retrieve and decrypt Private Key
    // NOTE: This is a suspending function because it needs to read from DataStore first.
    suspend fun getDecryptedPrivateKey(): String? {
        val preferences = context.walletDataStore.data.firstOrNull() // Blocking read to get current preferences
        val encryptedPrivateKeyBase64 = preferences?.get(ENCRYPTED_PRIVATE_KEY)
        val ivBase64 = preferences?.get(ENCRYPTION_IV)

        if (encryptedPrivateKeyBase64 == null || ivBase64 == null) {
            return null
        }

        try {
            val encryptedBytes = Base64.decode(encryptedPrivateKeyBase64, Base64.NO_WRAP)
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val decryptedBytes = keystoreHelper.decrypt(encryptedBytes, iv)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // Log decryption errors, e.g., key not found, IV mismatch, corrupted data
            // Consider clearing wallet data if decryption consistently fails due to corruption
            e.printStackTrace()
            return null
        }
    }


    // Save wallet address (No change, uses regular DataStore)
    suspend fun saveWalletAddress(address: String) {
        context.walletDataStore.edit { preferences ->
            preferences[WALLET_ADDRESS_KEY] = address
        }
    }

    // Retrieve wallet address (No change, uses regular DataStore)
    fun getWalletAddress(): Flow<String?> {
        return context.walletDataStore.data.map { preferences ->
            preferences[WALLET_ADDRESS_KEY]
        }
    }

    // Clear wallet information
    suspend fun clearWalletData() {
        context.walletDataStore.edit { preferences ->
            preferences.clear() // Clears all from regular DataStore (including wallet address)
        }
        // No longer using EncryptedSharedPreferences directly for mnemonic, so this can be removed.
        // Also need to remove the key from Keystore if we want to completely "forget" the wallet
        // though typically clearing the DataStore is enough for app-level "forgetting".
        // If the app is uninstalled, Keystore keys are removed automatically.
        // We might want to add a specific method to remove the Keystore key if desired for full "logout" functionality.
        // For now, only clearing DataStore:
        // keystoreHelper.deleteKey() // This function needs to be added to KeystoreHelper if needed.
    }
}