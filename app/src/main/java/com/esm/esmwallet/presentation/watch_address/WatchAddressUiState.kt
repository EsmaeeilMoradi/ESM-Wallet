package com.esm.esmwallet.presentation.watch_address

data class WatchAddressUiState(
    val address: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null,
    val isAddressValid: Boolean = false,
    val isButtonEnabled: Boolean = false
)