package com.esm.esmwallet.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class Token(
    val name: String,
    val symbol: String,
    val balance: String,
    val icon: ImageVector
)