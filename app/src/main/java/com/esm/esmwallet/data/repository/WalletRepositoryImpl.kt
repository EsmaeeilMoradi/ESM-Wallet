package com.esm.esmwallet.data.repository


import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WalletRepositoryImpl : WalletRepository {
 private val web3j: Web3j = Web3j.build(HttpService("https://ethereum.publicnode.com"))
    override suspend fun getEthBalance(walletAddress: String): BigInteger {
        return withContext(Dispatchers.IO) {
            val balanceWei = web3j.ethGetBalance(
                walletAddress,
                org.web3j.protocol.core.DefaultBlockParameterName.LATEST
            ).send().balance
            balanceWei
        }
    }
}