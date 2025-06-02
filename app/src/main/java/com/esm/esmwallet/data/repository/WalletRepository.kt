package com.esm.esmwallet.data.repository


import java.math.BigInteger

interface WalletRepository {
    suspend fun getEthBalance(walletAddress: String): BigInteger
    suspend fun sendEth(
        privateKey: String,
        toAddress: String,
        amountWei: BigInteger
    ): String //  Transaction Hash

    //  ERC-20 ---
    suspend fun getErc20TokenBalance(
        tokenContractAddress: String,
        walletAddress: String
    ): BigInteger

    suspend fun getErc20TokenDecimals(tokenContractAddress: String): Int

    suspend fun getErc20TokenSymbol(tokenContractAddress: String): String

    // New: Function to send ERC-20 tokens
    suspend fun sendErc20Token(
        privateKey: String,
        tokenContractAddress: String,
        toAddress: String,
        amount: BigInteger // Amount in token's smallest unit (e.g., Wei for ETH, but for DAI it's based on its decimals)
    ): String // Transaction Hash

    // Generates a 12-word mnemonic phrase
    fun generateMnemonicPhrase(): String
}