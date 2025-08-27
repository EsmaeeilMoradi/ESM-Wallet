package com.esm.esmwallet.data.model

data class Wallet(
    val address: String,
    val mnemonic: String?,
    val privateKey: String?
)