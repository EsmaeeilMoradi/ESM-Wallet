//package com.esm.esmwallet.data.wallet
//
//import org.bitcoinj.core.ECKey
//import org.bitcoinj.core.NetworkParameters
//import org.bitcoinj.params.MainNetParams
//import org.bitcoinj.params.TestNet3Params
//import org.bitcoinj.crypto.MnemonicCode
//import org.bitcoinj.crypto.MnemonicException
//import org.bitcoinj.wallet.DeterministicSeed
//import org.bitcoinj.wallet.Wallet
//import java.security.SecureRandom
//import java.math.BigInteger
//import org.web3j.crypto.ECKeyPair
//import org.web3j.crypto.Keys
//import java.util.Date
//import android.util.Log
//
//class WalletManager {
//
//    // Ideally, network parameters should be configurable, perhaps passed from ViewModel or DI
//    // For now, keeping it as TestNet3Params.
//    private val currentNetworkParameters: NetworkParameters = TestNet3Params.get() // Or MainNetParams.get()
//
//    /**
//     * Creates a new BIP39 compatible wallet.
//     * Returns the Wallet object and its Mnemonic Phrase.
//     */
//    fun createNewWallet(): Pair<Wallet, List<String>> { // Changed return type to Pair<Wallet, List<String>>
//        val seed = DeterministicSeed(SecureRandom(), DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS, "")
//
//        Log.d("WalletManager", "New DeterministicSeed created.")
//        val mnemonicCode = seed.mnemonicCode
//        Log.d("WalletManager", "Seed Mnemonic (from DeterministicSeed): $mnemonicCode")
//
//        val wallet = Wallet.fromSeed(currentNetworkParameters, seed)
//        Log.d("WalletManager", "New Wallet created using Wallet.fromSeed().")
//
//        if (mnemonicCode.isNullOrEmpty()) {
//            throw IllegalStateException("Mnemonic phrase was not generated for the new wallet.")
//        }
//        return Pair(wallet, mnemonicCode)
//    }
//
//    /**
//     * Restores a wallet from a given Mnemonic Phrase.
//     * @throws MnemonicException if the mnemonic is invalid.
//     * @throws IllegalArgumentException if mnemonic word count is incorrect.
//     */
//    @Throws(MnemonicException::class, IllegalArgumentException::class)
//    fun restoreWalletFromMnemonic(mnemonic: List<String>): Wallet {
//        if (mnemonic.size != 12 && mnemonic.size != 24) {
//            throw IllegalArgumentException("Mnemonic phrase must be 12 or 24 words.")
//        }
//        MnemonicCode.INSTANCE.check(mnemonic)
//        val creationTimeSeconds = Date().time / 1000 // Current time for DeterministicSeed constructor
//        val seed = DeterministicSeed(mnemonic, null, "", creationTimeSeconds)
//
//        Log.d("WalletManager", "DeterministicSeed restored from mnemonic.")
//        Log.d("WalletManager", "Restored Seed Mnemonic (from DeterministicSeed): ${seed.mnemonicCode}")
//
//        val wallet = Wallet.fromSeed(currentNetworkParameters, seed)
//        Log.d("WalletManager", "Wallet restored from mnemonic using Wallet.fromSeed().")
//
//        return wallet
//    }
//
//    /**
//     * Extracts the Mnemonic Phrase from a given Wallet object.
//     */
//    fun getMnemonicFromWallet(wallet: Wallet): List<String> {
//        return wallet.keyChainSeed?.mnemonicCode ?: emptyList()
//    }
//
//    /**
//     * Extracts the primary Private Key in Hex format from a given Wallet object.
//     * Throws IllegalStateException if no key is found.
//     */
//    fun getPrivateKeyFromWallet(wallet: Wallet): String {
//        val ecKey: ECKey? = wallet.currentReceiveKey()
//
//        if (ecKey == null) {
//            throw IllegalStateException("No receive key found in the wallet. Wallet might not be initialized with keys or needs address generation.")
//        }
//
//        return ecKey.privateKeyAsHex
//    }
//
//    /**
//     * Derives an Ethereum address from a Private Key in Hex format.
//     */
//    fun getEthAddressFromPrivateKey(privateKeyHex: String): String {
//        val privateKeyBigInt = BigInteger(privateKeyHex, 16)
//        val ecKeyPair = ECKeyPair.create(privateKeyBigInt)
//        // Keys.getAddress expects publicKey, which can be derived from ecKeyPair
//        return Keys.getAddress(ecKeyPair.publicKey)
//    }
//
//    /**
//     * Generates a new BIP39 Mnemonic Phrase.
//     * This function is now standalone, as createNewWallet generates its own mnemonic.
//     */
//    fun generateMnemonic(numWords: Int = 12): List<String> {
//        val entropyBits = when (numWords) {
//            12 -> 128 // 12 words = 128 bits of entropy
//            24 -> 256 // 24 words = 256 bits of entropy
//            else -> throw IllegalArgumentException("Number of words must be 12 or 24.")
//        }
//        val entropy = ByteArray(entropyBits / 8)
//        SecureRandom().nextBytes(entropy)
//        return MnemonicCode.INSTANCE.toMnemonic(entropy)
//    }
//}}
package com.esm.esmwallet.data.wallet

import android.util.Log
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.crypto.MnemonicUtils // این همان MnemonicUtils است که شما ارائه دادید
import org.web3j.utils.Numeric
import java.security.SecureRandom
import java.math.BigInteger
import java.util.Arrays // <<-- اضافه کنید!

// Bouncy Castle imports for HD Key Derivation (همه باید اینجا باشند)
import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.math.ec.FixedPointCombMultiplier

class WalletManager {

    // Standard Ethereum BIP-44 Derivation Path for the first account (Account 0 in MetaMask)
    private val ETH_DERIVATION_PATH = "m/44'/60'/0'/0/0" // m/44'/coin_type'/account'/change'/address_index

    // SECP256k1 parameters for Ethereum (from Bouncy Castle)
    private val CURVE_PARAMS: X9ECParameters = SECNamedCurves.getByName("secp256k1")
    private val CURVE_ORDER: BigInteger = CURVE_PARAMS.n // 'n' is the order of the curve
    private val GENERATOR_POINT = CURVE_PARAMS.g // 'g' is the generator point

    /**
     * Creates a new BIP39 compatible Ethereum wallet.
     * Returns Credentials (containing Private Key and Address) and its Mnemonic Phrase.
     */
    fun createNewWallet(): Pair<Credentials, List<String>> {
        val initialEntropy = ByteArray(16) // 12 words = 128 bits
        SecureRandom().nextBytes(initialEntropy)
        val mnemonic = MnemonicUtils.generateMnemonic(initialEntropy) // Use your MnemonicUtils
        val mnemonicWords = mnemonic.split(" ")

        val seed = MnemonicUtils.generateSeed(mnemonic, null) // No passphrase

        // Derive the private key using the custom deriveBip44Key function
        val privateKeyBytes = deriveBip44Key(seed, ETH_DERIVATION_PATH)
        val privateKeyHex = Numeric.toHexStringNoPrefix(privateKeyBytes)

        val credentials = Credentials.create(privateKeyHex)

        Log.d("WalletManager", "New Ethereum Wallet created.")
        Log.d("WalletManager", "Mnemonic: ${mnemonicWords.joinToString(" ")}")
        Log.d("WalletManager", "Derived Address: ${credentials.address}")
        Log.d("WalletManager", "Derived Private Key (HEX): $privateKeyHex")

        return Pair(credentials, mnemonicWords)
    }

    /**
     * Restores an Ethereum wallet from a given Mnemonic Phrase.
     * Uses the standard Ethereum derivation path.
     * @throws IllegalArgumentException if the mnemonic is invalid or word count is incorrect.
     */
    @Throws(IllegalArgumentException::class)
    fun restoreWalletFromMnemonic(mnemonicWords: List<String>): Credentials {
        val mnemonic = mnemonicWords.joinToString(" ")

        if (!MnemonicUtils.validateMnemonic(mnemonic)) { // Use your MnemonicUtils' validateMnemonic
            throw IllegalArgumentException("Invalid Mnemonic Phrase.")
        }
        if (mnemonicWords.size != 12 && mnemonicWords.size != 24) {
            throw IllegalArgumentException("Mnemonic phrase must be 12 or 24 words.")
        }

        val seed = MnemonicUtils.generateSeed(mnemonic, null) // No passphrase

        // Derive the private key using the custom deriveBip44Key function
        val privateKeyBytes = deriveBip44Key(seed, ETH_DERIVATION_PATH)
        val privateKeyHex = Numeric.toHexStringNoPrefix(privateKeyBytes)

        val credentials = Credentials.create(privateKeyHex)

        Log.d("WalletManager", "Ethereum Wallet restored from mnemonic.")
        Log.d("WalletManager", "Restored Mnemonic: ${mnemonicWords.joinToString(" ")}")
        Log.d("WalletManager", "Derived Address: ${credentials.address}")
        Log.d("WalletManager", "Derived Private Key (HEX): $privateKeyHex")

        return credentials
    }

    /**
     * Extracts the Private Key in Hex format from Web3j Credentials.
     */
    fun getPrivateKeyFromCredentials(credentials: Credentials): String {
        return Numeric.toHexStringNoPrefix(credentials.ecKeyPair.privateKey)
    }

    /**
     * Extracts the Ethereum Address from Web3j Credentials (checksummed).
     */
    fun getEthAddressFromCredentials(credentials: Credentials): String {
        return credentials.address
    }

    /**
     * Derives an Ethereum address from a Private Key in Hex format.
     */
    fun getEthAddressFromPrivateKey(privateKeyHex: String): String {
        val privateKeyBigInt = BigInteger(privateKeyHex, 16)
        val ecKeyPair = ECKeyPair.create(privateKeyBigInt)
        return Keys.getAddress(ecKeyPair.publicKey)
    }

    /**
     * Generates a new BIP39 Mnemonic Phrase.
     * This function uses the MnemonicUtils you provided.
     */
    fun generateMnemonic(numWords: Int = 12): List<String> {
        val entropyBits = when (numWords) {
            12 -> 128
            24 -> 256
            else -> throw IllegalArgumentException("Number of words must be 12 or 24.")
        }
        val entropy = ByteArray(entropyBits / 8)
        SecureRandom().nextBytes(entropy)
        return MnemonicUtils.generateMnemonic(entropy).split(" ")
    }

    // --- Custom BIP-32/BIP-44 Derivation Functions (using Bouncy Castle) ---

    // This data class holds the intermediate values for HD key derivation
    private data class HDKey(val privateKey: ByteArray, val chainCode: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as HDKey

            if (!privateKey.contentEquals(other.privateKey)) return false
            if (!chainCode.contentEquals(other.chainCode)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = privateKey.contentHashCode()
            result = 31 * result + chainCode.contentHashCode()
            return result
        }
    }

    // Main derivation function, now robustly implemented
    private fun deriveBip44Key(seed: ByteArray, path: String): ByteArray {
        // Master Key generation (from BIP-32)
        val hmacSha512 = HMac(SHA512Digest())
        hmacSha512.init(KeyParameter("Bitcoin seed".toByteArray(Charsets.UTF_8)))
        hmacSha512.update(seed, 0, seed.size)
        val iOut = ByteArray(hmacSha512.macSize)
        hmacSha512.doFinal(iOut, 0)

        var masterPrivateKey = BigInteger(1, Arrays.copyOfRange(iOut, 0, 32)) // Changed from ByteArray to BigInteger
        var masterChainCode = Arrays.copyOfRange(iOut, 32, 64)

        // Parse derivation path
        val pathElements = path.split("/").drop(1) // Remove "m"

        for (element in pathElements) {
            val isHardened = element.endsWith("'")
            val indexStr = if (isHardened) element.substring(0, element.length - 1) else element
            val index = indexStr.toInt()

            // Derive child key
            val i = if (isHardened) (0x80000000.toInt() or index) else index

            val hmacChild = HMac(SHA512Digest())
            hmacChild.init(KeyParameter(masterChainCode)) // Use parent chain code

            val input: ByteArray
            if (isHardened) {
                // For hardened derivation: 0x00 + parent_private_key + i
                val privateKeyBytesPadded = Numeric.toBytesPadded(masterPrivateKey, 32)
                val extendedPrivateKeyBytes = ByteArray(1 + privateKeyBytesPadded.size)
                System.arraycopy(privateKeyBytesPadded, 0, extendedPrivateKeyBytes, 1, privateKeyBytesPadded.size)
                extendedPrivateKeyBytes[0] = 0x00 // Leading zero for private key
                input = extendedPrivateKeyBytes + Numeric.toBytesPadded(BigInteger.valueOf(i.toLong()), 4)
            } else {
                // For normal derivation: parent_public_key + i
                val parentPublicKeyPoint = FixedPointCombMultiplier().multiply(GENERATOR_POINT, masterPrivateKey)
                val parentPublicKeyBytes = parentPublicKeyPoint.getEncoded(false) // Uncompressed public key (0x04 prefix)
                input = parentPublicKeyBytes + Numeric.toBytesPadded(BigInteger.valueOf(i.toLong()), 4)
            }

            hmacChild.update(input, 0, input.size)
            val iOutChild = ByteArray(hmacChild.macSize)
            hmacChild.doFinal(iOutChild, 0)

            val iL = BigInteger(1, Arrays.copyOfRange(iOutChild, 0, 32))
            val iR = Arrays.copyOfRange(iOutChild, 32, 64) // New chain code

            // new_private_key = (iL + parent_private_key) % curve_order
            val newPrivateKey = iL.add(masterPrivateKey).mod(CURVE_ORDER)

            if (newPrivateKey.equals(BigInteger.ZERO) || newPrivateKey.compareTo(CURVE_ORDER) >= 0) {
                // This should ideally lead to trying the next index, but for simplicity we throw
                throw IllegalStateException("Child key derivation resulted in an invalid private key.")
            }

            // Update for next iteration
            masterPrivateKey = newPrivateKey
            masterChainCode = iR
        }

        return Numeric.toBytesPadded(masterPrivateKey, 32) // Return the final private key as ByteArray
    }
}