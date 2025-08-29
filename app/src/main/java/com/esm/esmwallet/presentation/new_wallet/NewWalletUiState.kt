package com.esm.esmwallet.presentation.new_wallet

data class NewWalletUiState(
    val walletName: String = "Wallet 1",
    val isCreateButtonEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
)