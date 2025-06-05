package com.esm.esmwallet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_tokens")
data class CustomToken(
    @PrimaryKey
    val contractAddress: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val iconUrl: String? = null,
    val isCustom: Boolean = true
)