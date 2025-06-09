package com.esm.esmwallet.data.remote

import com.esm.esmwallet.data.remote.response.EtherscanBalanceResponse
import com.esm.esmwallet.data.remote.response.EtherscanTxHistoryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface EtherscanApi {

    @GET("api")
    suspend fun getEthBalance(
        @Query("module") module: String = "account",
        @Query("action") action: String = "balance",
        @Query("address") address: String,
        @Query("tag") tag: String = "latest",
        @Query("apikey") apiKey: String,
        @Query("chainid") chainId: String
    ): EtherscanBalanceResponse

    @GET("api")
    suspend fun getNormalTransactions(
        @Query("module") module: String = "account",
        @Query("action") action: String = "txlist",
        @Query("address") address: String,
        @Query("startblock") startBlock: String = "0",
        @Query("endblock") endBlock: String = "99999999",
        @Query("sort") sort: String = "desc",
        @Query("apikey") apiKey: String,
        @Query("chainid") chainId: String
    ): EtherscanTxHistoryResponse

    @GET("api")
    suspend fun getErc20TokenTransactions(
        @Query("module") module: String = "account",
        @Query("action") action: String = "tokentx",
        @Query("address") address: String,
        @Query("contractaddress") contractAddress: String? = null,
        @Query("startblock") startBlock: String = "0",
        @Query("endblock") endBlock: String = "99999999",
        @Query("sort") sort: String = "desc",
        @Query("apikey") apiKey: String,
        @Query("chainid") chainId: String
    ): EtherscanTxHistoryResponse
}