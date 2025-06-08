package com.esm.esmwallet.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creating a single DataStore for the whole application
val Context.walletDataStore by preferencesDataStore(name = "wallet_preferences")

class WalletDataStore(private val context: Context) {

    // Key for storing the encrypted Mnemonic
    private val MNEMONIC_KEY = stringPreferencesKey("mnemonic_phrase_encrypted")
    // Key for storing the wallet address
    private val WALLET_ADDRESS_KEY = stringPreferencesKey("wallet_address")

    // Master Key for EncryptedSharedPreferences
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    // EncryptedSharedPreferences for storing Mnemonic (since DataStore doesn't have direct built-in encryption)
    private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
        "encrypted_wallet_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Save mnemonic encrypted
    suspend fun saveMnemonic(mnemonic: String) {
        with(encryptedSharedPreferences.edit()) {
            putString(MNEMONIC_KEY.name, mnemonic)
            apply()
        }
    }

    // Retrieve encrypted mnemonic
    fun getMnemonic(): Flow<String?> {
        return context.walletDataStore.data.map { preferences ->
            // Read from EncryptedSharedPreferences, not from regular DataStore
            encryptedSharedPreferences.getString(MNEMONIC_KEY.name, null)
        }
    }

    // Save wallet address (can be unencrypted if high security is not needed, or stored with regular DataStore)
    suspend fun saveWalletAddress(address: String) {
        context.walletDataStore.edit { preferences ->
            preferences[WALLET_ADDRESS_KEY] = address
        }
    }

    // Retrieve wallet address
    fun getWalletAddress(): Flow<String?> {
        return context.walletDataStore.data.map { preferences ->
            preferences[WALLET_ADDRESS_KEY]
        }
    }

    // Clear wallet information
    suspend fun clearWalletData() {
        context.walletDataStore.edit { preferences ->
            preferences.clear()
        }
        with(encryptedSharedPreferences.edit()) {
            clear() //  EncryptedSharedPreferences
            apply()
        }
    }
}