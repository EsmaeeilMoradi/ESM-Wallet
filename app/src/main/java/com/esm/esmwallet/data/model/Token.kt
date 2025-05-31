package com.esm.esmwallet.data.model

import androidx.annotation.DrawableRes


data class Token(
    val name: String,
    val symbol: String,
    val balance: String,
    @DrawableRes val iconResId: Int
)