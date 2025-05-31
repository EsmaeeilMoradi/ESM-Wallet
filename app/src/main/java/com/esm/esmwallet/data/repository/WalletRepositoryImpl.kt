package com.esm.esmwallet.data.repository


import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.protocol.core.DefaultBlockParameterName // <--- این ایمپورت اضافه شده

class WalletRepositoryImpl : WalletRepository {
// private val web3j: Web3j = Web3j.build(HttpService("https://ethereum.publicnode.com"))
private val web3j: Web3j = Web3j.build(HttpService("https://dawn-newest-morning.ethereum-sepolia.quiknode.pro/704573517106871d7fba269128fcdce8acd1e0a2/"))

    override suspend fun getEthBalance(walletAddress: String): BigInteger {
        return withContext(Dispatchers.IO) {
            val balanceWei = web3j.ethGetBalance(
                walletAddress,
                DefaultBlockParameterName.LATEST
            ).send().balance
            balanceWei
        }
    }
}