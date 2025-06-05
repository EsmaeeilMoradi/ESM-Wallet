package com.esm.esmwallet.data.remote

import com.esm.esmwallet.BuildConfig
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService

object Web3jClient {

    private const val ALCHEMY_NODE_URL = BuildConfig.ALCHEMY_NODE_URL_MAINNET

    private var web3jInstance: Web3j? = null

    fun buildWeb3j(): Web3j {
        if (web3jInstance == null) {
            web3jInstance = Web3j.build(HttpService(ALCHEMY_NODE_URL))
        }
        return web3jInstance!!
    }

}