package com.esm.esmwallet.data.remote.response

data class EtherscanApiResponse(
    val status: String,
    val message: String,
    val result: String
)