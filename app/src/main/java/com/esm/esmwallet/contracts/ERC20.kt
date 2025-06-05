package com.esm.esmwallet.contracts

import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.RemoteFunctionCall
import org.web3j.tx.ClientTransactionManager
import org.web3j.tx.Contract
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger
import java.util.Arrays
import java.util.Collections

/**
 * Auto generated code.
 *
 * @dev ERC20 interface
 */
@Suppress("rawtypes")
class ERC20 internal constructor(
    contractAddress: String?,
    web3j: Web3j?,
    transactionManager: TransactionManager?,
    gasProvider: ContractGasProvider?
) : Contract(
    BINARY,
    contractAddress,
    web3j,
    transactionManager,
    gasProvider
) {
    companion object {
        private const val BINARY = "0x"

        @JvmStatic
        fun load(
            contractAddress: String?,
            web3j: Web3j?,
            credentials: Credentials?,
            gasProvider: ContractGasProvider?
        ): ERC20 {
            return ERC20(
                contractAddress,
                web3j,
                ClientTransactionManager(web3j, credentials?.address ?: contractAddress),
                gasProvider
            )
        }

        @JvmStatic
        fun load(
            contractAddress: String?,
            web3j: Web3j?,
            transactionManager: TransactionManager?,
            gasProvider: ContractGasProvider?
        ): ERC20 {
            return ERC20(contractAddress, web3j, transactionManager, gasProvider)
        }
    }

    fun name(): RemoteFunctionCall<String> {
        val function = Function(
            "name",
            Collections.emptyList(),
            Arrays.asList(TypeReference.create(Utf8String::class.java)) as List<TypeReference<*>> // تغییر در اینجا
        )
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun symbol(): RemoteFunctionCall<String> {
        val function = Function(
            "symbol",
            Collections.emptyList(),
            Arrays.asList(TypeReference.create(Utf8String::class.java)) as List<TypeReference<*>> // تغییر در اینجا
        )
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun decimals(): RemoteFunctionCall<BigInteger> {
        val function = Function(
            "decimals",
            Collections.emptyList(),
            Arrays.asList(TypeReference.create(Uint8::class.java)) as List<TypeReference<*>> // تغییر در اینجا
        )
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }
}