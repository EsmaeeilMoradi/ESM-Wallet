package com.esm.esmwallet.presentation.new_wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class NewWalletEvent {
    data object NavigateToNextScreen : NewWalletEvent()
    data class ShowSnackbar(val message: String) : NewWalletEvent()
}

class NewWalletViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NewWalletUiState())
    val uiState: StateFlow<NewWalletUiState> = _uiState.asStateFlow()

    private val _eventChannel = Channel<NewWalletEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    fun onWalletNameChanged(name: String) {
        _uiState.update { it.copy(walletName = name) }
        updateCreateButtonState()
    }

    fun onClearWalletNameClicked() {
        _uiState.update { it.copy(walletName = "") }
        updateCreateButtonState()
    }

    private fun updateCreateButtonState() {
        _uiState.update {
            it.copy(isCreateButtonEnabled = it.walletName.isNotBlank())
        }
    }

    fun onAdvancedClicked() {
        // TODO: Navigate to Advanced settings screen
    }

    fun onCreateClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // TODO: Call use-case to create the wallet
            // For now, we simulate a successful creation
            kotlinx.coroutines.delay(1000)
            _eventChannel.send(NewWalletEvent.NavigateToNextScreen)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}