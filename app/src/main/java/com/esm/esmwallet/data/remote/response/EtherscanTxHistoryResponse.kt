package com.esm.esmwallet.data.remote.response

data class EtherscanTxHistoryResponse(
    val status: String,
    val message: String,
    val result: List<TransactionObject>
)

data class TransactionObject(
    val blockNumber: String,
    val timeStamp: String,
    val hash: String,
    val nonce: String,
    val blockHash: String,
    val transactionIndex: String,
    val from: String,
    val to: String,
    val value: String,
    val gasPrice: String,
    val gasUsed: String,
    val isError: String,
    val input: String,
    val contractAddress: String,
    val cumulativeGasUsed: String,
    val confirmations: String,
    val methodId: String,
    val functionName: String?,
    val tokenName: String?,
    val tokenSymbol: String?,
    val tokenDecimal: String?
)