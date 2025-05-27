package com.esm.esmwallet.data.repository


import java.math.BigInteger

interface WalletRepository {
    suspend fun getEthBalance(walletAddress: String): BigInteger
}