package com.esm.esmwallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.esm.esmwallet.data.model.TokenInfo
import com.esm.esmwallet.data.repository.Erc20Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.web3j.protocol.exceptions.ClientConnectionException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class Erc20ViewModel(private val repository: Erc20Repository) : ViewModel() {

    private val _tokenInfo = MutableStateFlow<TokenInfo?>(null)
    val tokenInfo: StateFlow<TokenInfo?> = _tokenInfo

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    fun fetchTokenInfoByAddress(contractAddress: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _snackbarMessage.value = "Fetching token details..."
            try {
                val info = repository.getTokenInfo(contractAddress)
                _tokenInfo.value = info
                _snackbarMessage.value = "Token details fetched successfully!"
            } catch (e: Exception) {
                _tokenInfo.value = null
                val errorMessage = when (e) {
                    is UnknownHostException -> "Network error: Host not found. Check your internet or proxy."
                    is SocketTimeoutException -> "Network error: Connection timed out. Try again."
                    is IOException -> "Network error: ${e.localizedMessage}. Check connection."
                    is ClientConnectionException -> "Blockchain connection error: ${e.localizedMessage}. Check Alchemy URL or proxy."
                    else -> "Error fetching token info: ${e.localizedMessage ?: "Unknown error"}"
                }
                _snackbarMessage.value = errorMessage
                e.printStackTrace()
                println("E/Erc20ViewModel: Error fetching token info: ${e.localizedMessage ?: "Unknown error"}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }

    class Factory(private val repository: Erc20Repository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(Erc20ViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return Erc20ViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}