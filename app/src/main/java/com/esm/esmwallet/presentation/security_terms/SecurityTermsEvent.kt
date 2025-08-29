package com.esm.esmwallet.presentation.security_terms


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class SecurityTermsEvent {
    // Navigate to the next screen (Create or Import wallet)
    data object NavigateToNextScreen : SecurityTermsEvent()
}

class SecurityTermsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityTermsUiState())
    val uiState: StateFlow<SecurityTermsUiState> = _uiState.asStateFlow()

    private val _eventChannel = Channel<SecurityTermsEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()

    /**
     * Called when the first checkbox's state changes.
     * @param accepted The new state of the checkbox.
     */
    fun onFirstTermAccepted(accepted: Boolean) {
        _uiState.update { it.copy(hasAcceptedFirstTerm = accepted) }
        updateButtonState()
    }

    /**
     * Called when the second checkbox's state changes.
     * @param accepted The new state of the checkbox.
     */
    fun onSecondTermAccepted(accepted: Boolean) {
        _uiState.update { it.copy(hasAcceptedSecondTerm = accepted) }
        updateButtonState()
    }

    /**
     * Updates the enabled state of the button based on the checkboxes.
     */
    private fun updateButtonState() {
        _uiState.update {
            it.copy(
                isButtonEnabled = it.hasAcceptedFirstTerm && it.hasAcceptedSecondTerm
            )
        }
    }

    /**
     * Called when the user clicks the "Next" button.
     * Navigates to the next screen.
     */
    fun onNextClicked() {
        viewModelScope.launch {
            _eventChannel.send(SecurityTermsEvent.NavigateToNextScreen)
        }
    }
}