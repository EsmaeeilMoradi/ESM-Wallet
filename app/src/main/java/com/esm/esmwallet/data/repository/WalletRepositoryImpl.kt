package com.esm.esmwallet.data.repository

import android.util.Log
import com.esm.esmwallet.data.model.Transaction
import com.esm.esmwallet.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
// REMOVED: import org.bitcoinj.crypto.MnemonicCode
// REMOVED: import org.bitcoinj.crypto.MnemonicException
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
// REMOVED: import java.security.SecureRandom // No longer needed for generateMnemonicPhrase here
import java.util.Arrays
import com.esm.esmwallet.data.remote.EtherscanApi
import com.esm.esmwallet.data.remote.RetrofitInstance
import com.esm.esmwallet.data.remote.response.TransactionObject
import com.esm.esmwallet.BuildConfig


class WalletRepositoryImpl : WalletRepository {

    private val nodeUrl = BuildConfig.ALCHEMY_NODE_URL
    private val hardcodedApiKey = BuildConfig.ETHERSCAN_API_KEY
    private val etherscanApi: EtherscanApi = RetrofitInstance.api

    private val web3j: Web3j by lazy {
        Web3j.build(HttpService(nodeUrl))
    }

    // Set to Sepolia testnet chain ID
    private val chainId: Long = 11155111L

    override suspend fun getEthBalance(walletAddress: String): BigInteger {
        return withContext(Dispatchers.IO) {
            try {
                val ethGetBalance = web3j.ethGetBalance(
                    walletAddress,
                    DefaultBlockParameterName.LATEST
                ).send()

                Log.d("ETH_DEBUG", "----------------------------------------------------")
                Log.d("ETH_DEBUG", "Calling ethGetBalance for address: $walletAddress")
                Log.d(
                    "ETH_DEBUG",
                    "EthGetBalance raw response JSON: ${ethGetBalance.jsonrpc}"
                )
                Log.d("ETH_DEBUG", "EthGetBalance ID: ${ethGetBalance.id}")
                Log.d("ETH_DEBUG", "EthGetBalance result (hex value): ${ethGetBalance.result}")
                Log.d("ETH_DEBUG", "EthGetBalance hasError: ${ethGetBalance.hasError()}")


                if (ethGetBalance.hasError()) {
                    val errorMessage = ethGetBalance.error?.message ?: "Unknown error"
                    Log.e("ETH_DEBUG", "EthGetBalance error message: $errorMessage")
                    throw Exception("Failed to get ETH balance: $errorMessage")
                }
                if (ethGetBalance.result.isNullOrEmpty()) {
                    Log.e(
                        "ETH_DEBUG",
                        "EthGetBalance result is null or empty, but no explicit error."
                    )
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

                val gasPriceResult = web3j.ethGasPrice().send()
                if (gasPriceResult.hasError()) {
                    throw Exception("Failed to get gas price: ${gasPriceResult.error.message}")
                }
                val gasPrice = gasPriceResult.gasPrice
                val gasLimit = BigInteger.valueOf(21000)
                val rawTransaction = RawTransaction.createEtherTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    toAddress,
                    amountWei
                )
                val signedMessage =
                    TransactionEncoder.signMessage(rawTransaction, chainId, credentials)
                val hexValue = Numeric.toHexString(signedMessage)
                val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()

                if (ethSendTransaction.hasError()) {
                    val error = ethSendTransaction.error
                    throw Exception("Transaction error: ${error?.message ?: "Unknown error"}")
                }
                ethSendTransaction.transactionHash
            } catch (e: Exception) {
                throw Exception("Failed to send ETH: ${e.localizedMessage}", e)
            }
        }
    }

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
                    emptyList(),
                    listOf(object : TypeReference<Uint8>() {})
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
                    emptyList(),
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

    override suspend fun sendErc20Token(
        privateKey: String,
        tokenContractAddress: String,
        toAddress: String,
        amount: BigInteger
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                val credentials = Credentials.create(privateKey)
                val nonce = web3j.ethGetTransactionCount(
                    credentials.address,
                    DefaultBlockParameterName.LATEST
                ).send().transactionCount
                val gasPriceResult = web3j.ethGasPrice().send()
                if (gasPriceResult.hasError()) {
                    throw Exception("Failed to get gas price for ERC-20 transaction: ${gasPriceResult.error.message}")
                }
                val gasPrice = gasPriceResult.gasPrice
                val function = Function(
                    "transfer",
                    Arrays.asList<Type<*>>(Address(toAddress), Uint256(amount)),
                    Arrays.asList<TypeReference<*>>(object :
                        TypeReference<org.web3j.abi.datatypes.Bool>() {})
                )
                val encodedFunction = FunctionEncoder.encode(function)
                val gasLimitResponse = web3j.ethEstimateGas(
                    org.web3j.protocol.core.methods.request.Transaction.createFunctionCallTransaction(
                        credentials.address,
                        nonce,
                        gasPrice,
                        BigInteger.ZERO,
                        tokenContractAddress,
                        encodedFunction
                    )
                ).send()

                if (gasLimitResponse.hasError()) {
                    throw Exception("Failed to estimate gas for ERC-20 transaction: ${gasLimitResponse.error.message}")
                }
                val gasLimit = gasLimitResponse.amountUsed

                val rawTransaction = RawTransaction.createTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    tokenContractAddress,
                    BigInteger.ZERO,
                    encodedFunction
                )
                val signedMessage =
                    TransactionEncoder.signMessage(rawTransaction, chainId, credentials)
                val hexValue = Numeric.toHexString(signedMessage)

                val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send()

                if (ethSendTransaction.hasError()) {
                    val error = ethSendTransaction.error
                    throw Exception("ERC-20 Transaction error: ${error?.message ?: "Unknown error"}")
                }

                ethSendTransaction.transactionHash

            } catch (e: Exception) {
                throw Exception("Failed to send ERC-20 token: ${e.localizedMessage}", e)
            }
        }
    }

    // REMOVED: This function is now handled by WalletManager (using MnemonicUtils)
    // override fun generateMnemonicPhrase(): String {
    //     val initialEntropy = ByteArray(16)
    //     SecureRandom().nextBytes(initialEntropy)
    //     return try {
    //         // MnemonicCode uses the English wordlist by default
    //         val wordList = MnemonicCode.INSTANCE.toMnemonic(initialEntropy)
    //         wordList.joinToString(" ")
    //     } catch (e: MnemonicException) {
    //         throw RuntimeException("Error generating mnemonic phrase", e)
    //     }
    // }

    override suspend fun getEthTransactionHistory(address: String): Resource<List<Transaction>> {
        return withContext(Dispatchers.IO) {
            try {
                val formattedAddress = addHexPrefix(address)
                val response = etherscanApi.getNormalTransactions(
                    address = formattedAddress,
                    apiKey = hardcodedApiKey,
                    chainId = chainId.toString()
                )

                if (response.status == "1") {
                    val transactions = response.result.map { it.toTransaction(address) }
                    Log.d(
                        "EtherscanHistory",
                        "Fetched ${transactions.size} ETH transactions for $address"
                    )
                    Resource.Success(transactions)
                } else {
                    val errorMessage = response.message
                    Log.e(
                        "EtherscanHistory",
                        "Etherscan API Error fetching ETH history: $errorMessage"
                    )
                    Resource.Error("Etherscan API Error: $errorMessage")
                }
            } catch (e: Exception) {
                Log.e(
                    "EtherscanHistory",
                    "Network Error fetching ETH transaction history: ${e.localizedMessage}",
                    e
                )
                Resource.Error("Failed to fetch ETH transaction history: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun getErc20TransactionHistory(
        address: String,
        contractAddress: String?
    ): Resource<List<Transaction>> {
        return withContext(Dispatchers.IO) {
            try {
                val formattedAddress = addHexPrefix(address)
                val response = etherscanApi.getErc20TokenTransactions(
                    address = formattedAddress,
                    contractAddress = contractAddress,
                    apiKey = hardcodedApiKey,
                    chainId = chainId.toString()
                )

                if (response.status == "1") {
                    val transactions = response.result.map { it.toTransaction(address) }
                    Log.d(
                        "EtherscanHistory",
                        "Fetched ${transactions.size} ERC-20 transactions for $address (Contract: $contractAddress)"
                    )
                    Resource.Success(transactions)
                } else {
                    val errorMessage = response.message
                    Log.e(
                        "EtherscanHistory",
                        "Etherscan API Error fetching ERC-20 history: $errorMessage"
                    )
                    Resource.Error("Etherscan API Error: $errorMessage")
                }
            } catch (e: Exception) {
                Log.e(
                    "EtherscanHistory",
                    "Network Error fetching ERC-20 transaction history: ${e.localizedMessage}",
                    e
                )
                Resource.Error("Failed to fetch ERC-20 transaction history: ${e.localizedMessage}")
            }
        }
    }

    suspend fun getEthBalanceForTest(walletAddress: String): Result<String> {
        return try {
            val formattedAddress = addHexPrefix(walletAddress)

            val response = etherscanApi.getEthBalance(
                address = formattedAddress,
                apiKey = hardcodedApiKey,
                chainId = chainId.toString()
            )

            if (response.status == "1") {
                Log.d("EtherscanTest", "ETH Balance fetched successfully: ${response.result}")
                Result.success(response.result)
            } else {
                Log.e(
                    "EtherscanTest",
                    "Etherscan API Error: ${response.message} - ${response.result}"
                )
                Result.failure(Exception("Etherscan API Error: ${response.message} - ${response.result}"))
            }
        } catch (e: Exception) {
            Log.e("EtherscanTest", "Exception during ETH balance fetch: ${e.localizedMessage}")
            Result.failure(e)
        }
    }
}

fun TransactionObject.toTransaction(myAddress: String): Transaction {
    return Transaction(
        hash = this.hash,
        from = this.from,
        to = this.to,
        value = this.value.toBigIntegerOrNull() ?: BigInteger.ZERO,
        gasPrice = this.gasPrice.toBigIntegerOrNull() ?: BigInteger.ZERO,
        gasUsed = this.gasUsed.toBigIntegerOrNull() ?: BigInteger.ZERO,
        timestamp = this.timeStamp.toLongOrNull() ?: 0L,
        isError = this.isError == "1",
        blockNumber = this.blockNumber.toBigIntegerOrNull() ?: BigInteger.ZERO,
        tokenName = this.tokenName,
        tokenSymbol = this.tokenSymbol,
        tokenDecimal = this.tokenDecimal?.toIntOrNull(),
        type = when {
            from.equals(myAddress, ignoreCase = true) -> Transaction.TransactionType.SENT
            to.equals(myAddress, ignoreCase = true) -> Transaction.TransactionType.RECEIVED
            else -> Transaction.TransactionType.UNKNOWN
        },
        isSent = from.equals(myAddress, ignoreCase = true),
        isReceived = to.equals(myAddress, ignoreCase = true)
    )
}
fun addHexPrefix(address: String): String {
    return if (address.startsWith("0x")) address else "0x$address"
}