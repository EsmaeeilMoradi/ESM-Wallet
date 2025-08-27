// app/src/main/java/com/esm/esmwallet/data/wallet/SecureWalletStorage.kt

package com.esm.esmwallet.data.wallet

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.IOException
import java.security.GeneralSecurityException

// بهتر است این ثابت ها را در یک فایل Constants.kt یا BuildConfig.kt نگهداری کنید
private const val WALLET_PREFS_FILE_NAME = "wallet_prefs"
private const val PRIVATE_KEY_ALIAS = "private_key" // کلیدی که Private Key رمز شده را با آن ذخیره می کنیم
private const val MASTER_KEY_ALIAS = "esm_wallet_master_key" // نام مستعار برای کلید اصلی Keystore

class SecureWalletStorage(private val context: Context) {

    private lateinit var encryptedSharedPreferences: SharedPreferences

    init {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            encryptedSharedPreferences = EncryptedSharedPreferences.create(
                WALLET_PREFS_FILE_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            Log.d("","SecureWalletStorage: EncryptedSharedPreferences initialized successfully.")
        } catch (e: GeneralSecurityException) {
            Log.e("e", "SecureWalletStorage: Failed to initialize EncryptedSharedPreferences due to GeneralSecurityException.")
            // در این مرحله، باید یک مکانیزم مدیریت خطا در نظر بگیرید.
            // مثلاً کاربر را از این مشکل مطلع کرده و از ادامه کار با برنامه جلوگیری کنید.
            // برای MVP، می توانید یک Exception پرتاب کنید یا برنامه را کرش کنید تا متوجه شوید.
            throw RuntimeException("Failed to initialize secure storage: ${e.message}", e)
        } catch (e: IOException) {
            Log.e("e", "SecureWalletStorage: Failed to initialize EncryptedSharedPreferences due to IOException.")
            throw RuntimeException("Failed to initialize secure storage: ${e.message}", e)
        }
    }

    // Private Key را به صورت امن ذخیره می کند
    fun savePrivateKey(privateKey: String) {
        try {
            encryptedSharedPreferences.edit()
                .putString(PRIVATE_KEY_ALIAS, privateKey)
                .apply()
            Log.d("","SecureWalletStorage: Private key saved securely.")
        } catch (e: Exception) {
            Log.e("e", "SecureWalletStorage: Failed to save private key.")
            throw e // برای مدیریت خطا در لایه های بالاتر
        }
    }

    // Private Key را به صورت امن بازیابی می کند
    fun getPrivateKey(): String? {
        return try {
            val privateKey = encryptedSharedPreferences.getString(PRIVATE_KEY_ALIAS, null)
            if (privateKey != null) {
                Log.d("","SecureWalletStorage: Private key retrieved securely.")
            } else {
                Log.d("","SecureWalletStorage: No private key found.")
            }
            privateKey
        } catch (e: Exception) {
            Log.e("e", "SecureWalletStorage: Failed to retrieve private key.")
            throw e // برای مدیریت خطا در لایه های بالاتر
        }
    }

    // Private Key را حذف می کند (مثلاً هنگام خروج از حساب کاربری)
    fun clearPrivateKey() {
        try {
            encryptedSharedPreferences.edit()
                .remove(PRIVATE_KEY_ALIAS)
                .apply()
            Log.d("","SecureWalletStorage: Private key cleared.")
        } catch (e: Exception) {
            Log.e("e", "SecureWalletStorage: Failed to clear private key.")
            throw e // برای مدیریت خطا در لایه های بالاتر
        }
    }
}