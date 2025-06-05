package com.esm.esmwallet.data.wallet

import org.bitcoinj.core.ECKey
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.Wallet
import java.security.SecureRandom
import java.math.BigInteger
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys

class WalletManager {


    fun generateMnemonic(numWords: Int = 12): List<String> {
        val entropyBits = when (numWords) {
            12 -> 128
            24 -> 256
            else -> throw IllegalArgumentException("Unsupported mnemonic word count: $numWords. Only 12 or 24 words are supported.")
        }

        val random = SecureRandom()
        val entropy = ByteArray(entropyBits / 8)
        random.nextBytes(entropy)

        return MnemonicCode.INSTANCE.toMnemonic(entropy)
    }

    fun restoreWalletFromMnemonic(
        mnemonic: List<String>,
        networkParameters: NetworkParameters = MainNetParams.get()
    ): Wallet {
        MnemonicCode.INSTANCE.check(mnemonic)

        val seedBytes = MnemonicCode.toSeed(mnemonic, "")

        val seed = DeterministicSeed(seedBytes, "", System.currentTimeMillis() / 1000L)

        val wallet = Wallet.fromSeed(networkParameters, seed)

        return wallet
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
        val ecKey = ECKeyPair.create(privateKeyBigInt)
        return Keys.getAddress(ecKey.publicKey)
    }
}