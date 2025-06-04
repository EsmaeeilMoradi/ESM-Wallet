package com.esm.esmwallet.data.repository

import com.esm.esmwallet.data.model.Transaction
import com.esm.esmwallet.util.Resource


import java.math.BigInteger

interface WalletRepository {
    suspend fun getEthBalance(walletAddress: String): BigInteger
    suspend fun sendEth(
        privateKey: String,
        toAddress: String,
        amountWei: BigInteger
    ): String //  Transaction Hash

    suspend fun getErc20TokenBalance(
        tokenContractAddress: String,
        walletAddress: String
    ): BigInteger

    suspend fun getErc20TokenDecimals(tokenContractAddress: String): Int

    suspend fun getErc20TokenSymbol(tokenContractAddress: String): String

    suspend fun sendErc20Token(
        privateKey: String,
        tokenContractAddress: String,
        toAddress: String,
        amount: BigInteger
    ): String

    fun generateMnemonicPhrase(): String
    suspend fun getEthTransactionHistory(address: String): Resource<List<Transaction>>
    suspend fun getErc20TransactionHistory(
        address: String,
        contractAddress: String? = null
    ): Resource<List<Transaction>>
}