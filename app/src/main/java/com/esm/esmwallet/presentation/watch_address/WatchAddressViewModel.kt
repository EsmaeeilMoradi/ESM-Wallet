package com.esm.esmwallet.presentation.watch_address


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esm.esmwallet.util.AddressValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class WatchAddressEvent {
    data class ShowSnackbar(val message: String) : WatchAddressEvent()
    data object NavigateToHome : WatchAddressEvent()
}

class WatchAddressViewModel : ViewModel() {

    // The UI State for the screen
    private val _uiState = MutableStateFlow(WatchAddressUiState())
    val uiState: StateFlow<WatchAddressUiState> = _uiState.asStateFlow()

    // Navigation events for the UI to consume
    private val _eventChannel = Channel<WatchAddressEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    /**
     * Updates the address field in the UI state.
     * @param address The new address string.
     */
    fun onAddressChanged(address: String) {
        _uiState.update { it.copy(address = address) }
        validateInput()
    }

    /**
     * Updates the name field in the UI state.
     * @param name The new name string.
     */
    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    /**
     * Validates the entered address and updates the UI state accordingly.
     */
    private fun validateInput() {
        val isValid = AddressValidator.isValidAddress(_uiState.value.address)
        _uiState.update { it.copy(isAddressValid = isValid) }
        updateButtonState()
    }

    /**
     * Updates the state of the "Next" button.
     */
    private fun updateButtonState() {
        _uiState.update { it.copy(isButtonEnabled = it.isAddressValid) }
    }

    /**
     * Called when the user clicks the "Watch" button.
     */
    fun onWatchClicked() {
        viewModelScope.launch {
            if (_uiState.value.isAddressValid) {
                // TODO: Here you would save the address to your repository
                // For now, we will just simulate a successful save.
                _uiState.update { it.copy(isLoading = true) }
                // Simulate saving process
                kotlinx.coroutines.delay(1000)

                // Navigate to the home screen
                _eventChannel.send(WatchAddressEvent.NavigateToHome)
            } else {
                _eventChannel.send(WatchAddressEvent.ShowSnackbar("Invalid address. Please check and try again."))
            }
        }
    }
}






