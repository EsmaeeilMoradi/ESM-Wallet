package com.esm.esmwallet.data.repository

import com.esm.esmwallet.data.model.TokenInfo
import com.esm.esmwallet.data.remote.AlchemyApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Erc20Repository(private val alchemyApiService: AlchemyApiService) {

    suspend fun getTokenInfo(contractAddress: String): TokenInfo {
        return withContext(Dispatchers.IO) {
            try {
                val erc20Contract = AlchemyApiService.getErc20Contract(alchemyApiService.web3j, contractAddress)

                val name = erc20Contract.name().send()
                val symbol = erc20Contract.symbol().send()
                val decimals = erc20Contract.decimals().send()

                println("D/Erc20Repository: Fetched token: Name=$name, Symbol=$symbol, Decimals=$decimals")

                TokenInfo(
                    name = name,
                    symbol = symbol,
                    decimals = decimals.toInt()
                )
            } catch (e: Exception) {
                println("E/Erc20Repository: Error in getTokenInfo: ${e.localizedMessage}")
                throw e
            }
        }
    }
}