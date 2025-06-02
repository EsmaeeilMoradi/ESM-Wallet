package com.esm.esmwallet.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.crypto.MnemonicException
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import java.math.BigInteger
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import java.security.SecureRandom
import java.util.Arrays

class WalletRepositoryImpl : WalletRepository {

    private val nodeUrl ="https://eth-sepolia.g.alchemy.com/v2/A3yCGdMaP7z1P3UoUyiP5YfAshA6jBii"
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

                // *** Alchemy/Web3j ***
                Log.d("ETH_DEBUG", "----------------------------------------------------")
                Log.d("ETH_DEBUG", "Calling ethGetBalance for address: $walletAddress")
                Log.d("ETH_DEBUG", "EthGetBalance raw response JSON: ${ethGetBalance.jsonrpc}") // Shows jsonrpc version
                Log.d("ETH_DEBUG", "EthGetBalance ID: ${ethGetBalance.id}")
                Log.d("ETH_DEBUG", "EthGetBalance result (hex value): ${ethGetBalance.result}")
                Log.d("ETH_DEBUG", "EthGetBalance hasError: ${ethGetBalance.hasError()}")


                if (ethGetBalance.hasError()) {
                    val errorMessage = ethGetBalance.error?.message ?: "Unknown error"
                    Log.e("ETH_DEBUG", "EthGetBalance error message: $errorMessage")
                    throw Exception("Failed to get ETH balance: $errorMessage")
                }
                if (ethGetBalance.result.isNullOrEmpty()) {
                    Log.e("ETH_DEBUG", "EthGetBalance result is null or empty, but no explicit error.")
                    throw Exception("Failed to get ETH balance: Empty result from API")
                }

                val balanceWei = ethGetBalance.balance
                Log.d("ETH_DEBUG", "EthGetBalance parsed balance (BigInteger Wei): $balanceWei")
                Log.d("ETH_DEBUG", "----------------------------------------------------")


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

                val nonce = web3j.ethGetTransactionCount(
                    credentials.address,
                    DefaultBlockParameterName.LATEST
                ).send().transactionCount

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
                val signedMessage =
                    TransactionEncoder.signMessage(rawTransaction, chainId, credentials)

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
        }
    }

    //  ERC-20 ---
    override suspend fun getErc20TokenBalance(
        tokenContractAddress: String,
        walletAddress: String
    ): BigInteger {
        return withContext(Dispatchers.IO) {
            try {
                val function = Function(
                    "balanceOf",
                    Arrays.asList<Type<*>>(Address(walletAddress)),
                    Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {})
                )

                val encodedFunction = FunctionEncoder.encode(function)
                Log.d("ERC20_DEBUG", "Encoded balanceOf function: $encodedFunction")

                val ethCall = web3j.ethCall(
                    org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                        null,
                        tokenContractAddress,
                        encodedFunction
                    ),
                    DefaultBlockParameterName.LATEST
                ).send()

                if (ethCall.hasError()) {
                    throw Exception("Failed to get ERC-20 balance: ${ethCall.error.message}")
                }

                val results = FunctionReturnDecoder.decode(
                    ethCall.value,
                    function.outputParameters
                )

                if (results.isNotEmpty() && results[0] is Uint256) {
                    return@withContext (results[0] as Uint256).value
                } else {
                    throw Exception("Could not decode ERC-20 balance from contract call.")
                }

            } catch (e: Exception) {
                throw Exception("Failed to get ERC-20 token balance: ${e.localizedMessage}", e)
            }
        }
    }

    override suspend fun getErc20TokenDecimals(tokenContractAddress: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                val function = Function(
                    "decimals",
                    emptyList(), // No input parameters
                    listOf(object : TypeReference<Uint8>() {}) // Output is Uint8
                )
                val encodedFunction = FunctionEncoder.encode(function)
                val ethCall = web3j.ethCall(
                    org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                        null,
                        tokenContractAddress,
                        encodedFunction
                    ),
                    DefaultBlockParameterName.LATEST
                ).send()

                if (ethCall.hasError()) {
                    throw Exception("Failed to get ERC-20 decimals: ${ethCall.error.message}")
                }

                val results = FunctionReturnDecoder.decode(
                    ethCall.value,
                    function.outputParameters
                )

                if (results.isNotEmpty() && results[0] is Uint8) {
                    return@withContext (results[0] as Uint8).value.toInt()
                } else {
                    throw Exception("Could not decode ERC-20 decimals from contract call.")
                }
            } catch (e: Exception) {
                throw Exception("Failed to get ERC-20 token decimals: ${e.localizedMessage}", e)
            }
        }
    }

    override suspend fun getErc20TokenSymbol(tokenContractAddress: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val function = Function(
                    "symbol",
                    emptyList(), // No input parameters
                    listOf(object : TypeReference<Utf8String>() {}) // Output is string
                )
                val encodedFunction = FunctionEncoder.encode(function)
                val ethCall = web3j.ethCall(
                    org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                        null,
                        tokenContractAddress,
                        encodedFunction
                    ),
                    DefaultBlockParameterName.LATEST
                ).send()

                if (ethCall.hasError()) {
                    throw Exception("Failed to get ERC-20 symbol: ${ethCall.error.message}")
                }

                val results = FunctionReturnDecoder.decode(
                    ethCall.value,
                    function.outputParameters
                )

                if (results.isNotEmpty() && results[0] is Utf8String) {
                    return@withContext (results[0] as Utf8String).value
                } else {
                    throw Exception("Could not decode ERC-20 symbol from contract call.")
                }
            } catch (e: Exception) {
                throw Exception("Failed to get ERC-20 token symbol: ${e.localizedMessage}", e)
            }
        }
    }

    // Implementation for sending ERC-20 tokens
    override suspend fun sendErc20Token(
        privateKey: String,
        tokenContractAddress: String,
        toAddress: String,
        amount: BigInteger
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                val credentials = Credentials.create(privateKey)

                // 1. Get Nonce
                val nonce = web3j.ethGetTransactionCount(
                    credentials.address,
                    DefaultBlockParameterName.LATEST
                ).send().transactionCount

                // 2. Gas Price (EIP-1559 Transaction)
                // For ERC-20 transactions, it's better to use EIP-1559 style gas estimation if the network supports it.
                // However, for simplicity and compatibility with your existing ETH send, we'll stick to legacy gas price for now.
                // In a real app, you would fetch maxFeePerGas and maxPriorityFeePerGas.
                val gasPriceResult = web3j.ethGasPrice().send()
                if (gasPriceResult.hasError()) {
                    throw Exception("Failed to get gas price for ERC-20 transaction: ${gasPriceResult.error.message}")
                }
                val gasPrice = gasPriceResult.gasPrice

                // 3. Encode the transfer function call for ERC-20
                // function transfer(address _to, uint256 _value) returns (bool success)
                val function = Function(
                    "transfer",
                    Arrays.asList<Type<*>>(Address(toAddress), Uint256(amount)),
                    Arrays.asList<TypeReference<*>>(object :
                        TypeReference<org.web3j.abi.datatypes.Bool>() {})
                )
                val encodedFunction = FunctionEncoder.encode(function)

                // 4. Estimate Gas Limit for ERC-20 transaction
                // This is crucial for ERC-20 as gas limit is not fixed at 21000
                val gasLimitResponse = web3j.ethEstimateGas(
                    org.web3j.protocol.core.methods.request.Transaction.createFunctionCallTransaction(
                        credentials.address,
                        nonce,
                        gasPrice,
                        BigInteger.ZERO, // Value is 0 for ERC-20 transfers
                        tokenContractAddress,
                        encodedFunction
                    )
                ).send()

                if (gasLimitResponse.hasError()) {
                    throw Exception("Failed to estimate gas for ERC-20 transaction: ${gasLimitResponse.error.message}")
                }
                val gasLimit = gasLimitResponse.amountUsed // Use the estimated gas limit

                // 5. Create the RawTransaction (Legacy style, sending to the token contract)
                val rawTransaction = RawTransaction.createTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    tokenContractAddress, // Destination is the token contract itself
                    BigInteger.ZERO, // Value is 0 ETH for token transfers
                    encodedFunction // Data field contains the encoded function call
                )

                // 6. Sign the transaction
                val signedMessage =
                    TransactionEncoder.signMessage(rawTransaction, chainId, credentials)
                val hexValue = Numeric.toHexString(signedMessage)

                // 7. Send the signed transaction
                val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()

                // 8. Check for errors
                if (ethSendTransaction.hasError()) {
                    val error = ethSendTransaction.error
                    throw Exception("ERC-20 Transaction error: ${error?.message ?: "Unknown error"}")
                }

                // 9. Return transaction hash
                ethSendTransaction.transactionHash

            } catch (e: Exception) {
                throw Exception("Failed to send ERC-20 token: ${e.localizedMessage}", e)
            }
        }
    }

    override fun generateMnemonicPhrase(): String {
        val initialEntropy = ByteArray(16) // 128 bits for 12 words
        SecureRandom().nextBytes(initialEntropy)
        return try {
            // MnemonicCode uses the English wordlist by default
            val wordList = MnemonicCode.INSTANCE.toMnemonic(initialEntropy)
            wordList.joinToString(" ")
        } catch (e: MnemonicException) {
            // Handle exception if entropy is not valid
            throw RuntimeException("Error generating mnemonic phrase", e)
        }
    }
}