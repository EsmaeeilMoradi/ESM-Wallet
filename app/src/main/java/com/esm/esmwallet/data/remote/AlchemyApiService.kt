package com.esm.esmwallet.data.remote

import com.esm.esmwallet.contracts.ERC20
import org.web3j.protocol.Web3j
import org.web3j.tx.ClientTransactionManager
import org.web3j.tx.gas.DefaultGasProvider

class AlchemyApiService(val web3j: Web3j) {

    companion object {
        fun getErc20Contract(web3j: Web3j, contractAddress: String): ERC20 {
            return ERC20.load(
                contractAddress,
                web3j,
                ClientTransactionManager(web3j, contractAddress),
                DefaultGasProvider()
            )
        }
    }
}