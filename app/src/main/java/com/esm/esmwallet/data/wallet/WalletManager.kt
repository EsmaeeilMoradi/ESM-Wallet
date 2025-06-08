package com.esm.esmwallet.data.wallet

import org.bitcoinj.core.ECKey
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params // Assuming you want TestNet3Params
import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.crypto.MnemonicException
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.Wallet
import java.security.SecureRandom
import java.math.BigInteger
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import java.util.Date
import android.util.Log // Add this import for logging

class WalletManager {

    private val currentNetworkParameters: NetworkParameters = TestNet3Params.get() // Or MainNetParams.get()


    fun createNewWallet(): Wallet {
        val seed = DeterministicSeed(SecureRandom(), DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS, "")

        // Add logging to check seed content
        Log.d("WalletManager", "New DeterministicSeed created.")
        Log.d("WalletManager", "Seed Mnemonic (from DeterministicSeed): ${seed.mnemonicCode}")
        Log.d("WalletManager", "Seed Bytes (from DeterministicSeed): ${seed.seedBytes?.let { org.bitcoinj.core.Utils.HEX.encode(it) }}")

        // Use Wallet.fromSeed() directly, as it handles KeyChainGroup internally
        val wallet = Wallet.fromSeed(currentNetworkParameters, seed)

        // Add logging to check wallet's seed content
        Log.d("WalletManager", "New Wallet created using Wallet.fromSeed().")
        Log.d("WalletManager", "Wallet Seed Mnemonic: ${wallet.keyChainSeed?.mnemonicCode}")

        return wallet
    }

    @Throws(MnemonicException::class)
    fun restoreWalletFromMnemonic(mnemonic: List<String>): Wallet {
        if (mnemonic.size != 12 && mnemonic.size != 24) {
            throw IllegalArgumentException("Mnemonic phrase must be 12 or 24 words.")
        }
        MnemonicCode.INSTANCE.check(mnemonic)
        val creationTimeSeconds = Date().time / 1000 // زمان فعلی برای constructor DeterministicSeed
        val seed = DeterministicSeed(mnemonic, null, "", creationTimeSeconds)

        // Add logging to check seed content after restoration
        Log.d("WalletManager", "DeterministicSeed restored from mnemonic.")
        Log.d("WalletManager", "Restored Seed Mnemonic (from DeterministicSeed): ${seed.mnemonicCode}")

        // Use Wallet.fromSeed() directly
        val wallet = Wallet.fromSeed(currentNetworkParameters, seed)

        // Add logging to check wallet's seed content after restoration
        Log.d("WalletManager", "Wallet restored from mnemonic using Wallet.fromSeed().")
        Log.d("WalletManager", "Restored Wallet Seed Mnemonic: ${wallet.keyChainSeed?.mnemonicCode}")

        return wallet
    }


    fun getMnemonicFromWallet(wallet: Wallet): List<String> {
        return wallet.keyChainSeed.mnemonicCode ?: emptyList()
    }

    fun getPrivateKeyFromWallet(wallet: Wallet): String {
        val ecKey: ECKey? = wallet.currentReceiveKey()

        if (ecKey == null) {
            throw IllegalStateException("No receive key found in the wallet. Wallet might not be initialized with keys or needs address generation.")
        }

        return ecKey.privateKeyAsHex
    }

    fun getEthAddressFromPrivateKey(privateKeyHex: String): String {
        val privateKeyBigInt = BigInteger(privateKeyHex, 16)
        val ecKeyPair = ECKeyPair.create(privateKeyBigInt)
        return Keys.getAddress(ecKeyPair.publicKey)
    }


    fun generateMnemonic(numWords: Int = 12): List<String> {
        val entropyBits = when (numWords) {
            12 -> 128 // 12 کلمه = 128 بیت انتروپی
            24 -> 256 // 24 کلمه = 256 بیت انتروپی
            else -> throw IllegalArgumentException("Number of words must be 12 or 24.")
        }
        val entropy = ByteArray(entropyBits / 8)
        SecureRandom().nextBytes(entropy)
        return MnemonicCode.INSTANCE.toMnemonic(entropy)
    }
}