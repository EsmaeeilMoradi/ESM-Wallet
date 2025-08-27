package com.esm.esmwallet.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.esm.esmwallet.data.onboarding.OnboardingRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val repository: OnboardingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState.initial())
    val uiState = _uiState.asStateFlow()
    // A Channel to send one-shot navigation events
    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.getOnboardingPages().collect() { pages ->
                _uiState.update { currentState ->
                    currentState.copy(
                        pages = pages,
                        isFinalPage = pages.size <= 1
                    )
                }
            }
        }
    }

    fun onNextClicked() {
        _uiState.update { currentState ->
            val newIndex = minOf(currentState.currentPageIndex + 1, currentState.pages.size - 1)

            // If we are on the final page and the button is clicked, we send a navigation event.
            if (newIndex == currentState.pages.size - 1) {
                viewModelScope.launch {
                    _navigationEvent.send(NavigationEvent.NavigateToWelcomeScreen)
                }
            }
            currentState.copy(
                currentPageIndex = newIndex,
                isFinalPage = newIndex == currentState.pages.size - 1
            )
        }
    }

    fun onPageChanged(newPageIndex: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                currentPageIndex = newPageIndex,
                isFinalPage = newPageIndex == currentState.pages.size - 1
            )


        }

    }

    // It's a static factory method that provides a way to create the ViewModel
    // with its dependencies.
    class Factory(private val repository: OnboardingRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return OnboardingViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}