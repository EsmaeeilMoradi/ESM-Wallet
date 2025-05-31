package com.esm.esmwallet.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import java.math.BigInteger


class WalletRepositoryImpl : WalletRepository {

    private val nodeUrl = "https://dawn-newest-morning.ethereum-sepolia.quiknode.pro/704573517106871d7fba269128fcdce8acd1e0a2/"
    private val web3j: Web3j by lazy {
        Web3j.build(HttpService(nodeUrl))
    }

    private val chainId: Long = 11155111L //   Sepolia Testnet

    override suspend fun getEthBalance(walletAddress: String): BigInteger {
        return withContext(Dispatchers.IO) {
            try {
                val ethGetBalance = web3j.ethGetBalance(
                    walletAddress,
                    DefaultBlockParameterName.LATEST
                ).send()
                ethGetBalance.balance
            } catch (e: Exception) {
                throw Exception("Failed to get ETH balance: ${e.localizedMessage}", e)
            }
        }
    }


    override suspend fun sendEth(
        privateKey: String,
        toAddress: String,
        amountWei: BigInteger
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                val credentials = Credentials.create(privateKey)

                val nonce = web3j.ethGetTransactionCount(credentials.address, DefaultBlockParameterName.LATEST).send().transactionCount

                // 1.  Gas Price ( Legacy Transaction)
                val gasPriceResult = web3j.ethGasPrice().send()
                if (gasPriceResult.hasError()) {
                    throw Exception("Failed to get gas price: ${gasPriceResult.error.message}")
                }
                val gasPrice = gasPriceResult.gasPrice

                // 2.  Gas Limit ( Legacy Transaction)
                val gasLimit = BigInteger.valueOf(21000) // Default for simple ETH transfers


                // 3.  RawTransaction Legacy
                // ا createEtherTransaction  Legacy Transactio
                val rawTransaction = RawTransaction.createEtherTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    toAddress,
                    amountWei
                )

                // 4. Chain ID (EIP-155 compatible for Legacy)
                //   signMessage  Chain ID
                val signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials)

                // 5. Hex String
                val hexValue = Numeric.toHexString(signedMessage)

                // 6. (Signed Transaction)
                val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()

                // 7.
                if (ethSendTransaction.hasError()) {
                    val error = ethSendTransaction.error
                    throw Exception("Transaction error: ${error?.message ?: "Unknown error"}")
                }

                // 8.
                ethSendTransaction.transactionHash
            } catch (e: Exception) {
                throw Exception("Failed to send ETH: ${e.localizedMessage}", e)
            }
        }}

}