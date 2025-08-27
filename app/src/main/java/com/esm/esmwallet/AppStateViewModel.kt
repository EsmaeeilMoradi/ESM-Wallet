package com.esm.esmwallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.esm.esmwallet.data.preferences.WalletDataStore
import com.esm.esmwallet.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppStateViewModel(
    private val walletDataStore: WalletDataStore
) : ViewModel() {
    private val _isFirstRun = MutableStateFlow(true)

    // The start destination is exposed as a Flow that the UI can observe.
    val startDestination: StateFlow<String> = _isFirstRun
        .map { isFirstRun ->
            if (isFirstRun) Screen.Onboarding.route else Screen.Welcome.route
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "loading_route")

    init {
        viewModelScope.launch {
          //  _isFirstRun.value = !walletDataStore.hasWallet().first()
            _isFirstRun.value = true
        }
    }
    // Your manual ViewModel factory to create this ViewModel
    class Factory(private val walletDataStore: WalletDataStore) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppStateViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AppStateViewModel(walletDataStore) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}