package com.esm.esmwallet.data.model

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import org.web3j.utils.Convert

data class Transaction(
    val hash: String,
    val from: String,
    val to: String,
    val value: BigInteger,
    val gasPrice: BigInteger,
    val gasUsed: BigInteger,
    val timestamp: Long,
    val isError: Boolean,
    val blockNumber: BigInteger,
    val tokenName: String?,
    val tokenSymbol: String?,
    val tokenDecimal: Int?,
    val type: TransactionType,
    val isSent: Boolean,
    val isReceived: Boolean
) {
    enum class TransactionType { SENT, RECEIVED, UNKNOWN }

    fun getFormattedValue(): String {
        if (tokenDecimal != null && tokenDecimal != 0) {
            val decimalValue =
                BigDecimal(value).divide(BigDecimal(10).pow(tokenDecimal), 4, RoundingMode.HALF_UP)
            return decimalValue.toPlainString()
        } else {
            val ethValue = Convert.fromWei(BigDecimal(value), Convert.Unit.ETHER)
            return ethValue.setScale(4, RoundingMode.HALF_UP).toPlainString()
        }
    }
}