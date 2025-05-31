package com.esm.esmwallet.data.repository


import java.math.BigInteger

interface WalletRepository {
    suspend fun getEthBalance(walletAddress: String): BigInteger
    suspend fun sendEth(
        privateKey: String,
        toAddress: String,
        amountWei: BigInteger
    ): String //  Transaction Hash

}