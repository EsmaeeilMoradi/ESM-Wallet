package com.esm.esmwallet.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esm.esmwallet.data.repository.WalletRepositoryImpl
import com.esm.esmwallet.domain.usecase.GetEthBalanceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class WalletViewModel : ViewModel() {
    private val getEthBalanceUseCase = GetEthBalanceUseCase(WalletRepositoryImpl())
    private val _ethBalance = MutableStateFlow<String>("Loading...")
    val ethBalance: StateFlow<String> = _ethBalance

    val testWalletAddress = "0xYourActualEthereumWalletAddressHere"

    init {
        fetchEthBalance(testWalletAddress)
    }

    fun fetchEthBalance(address: String) {
        viewModelScope.launch {
            try {
                val balanceWei = getEthBalanceUseCase(address)
                val balanceEther = BigDecimal(balanceWei)
                    .divide(BigDecimal(10).pow(18), 4, RoundingMode.HALF_UP)
                    .toPlainString()
                _ethBalance.value = "$balanceEther ETH"
            } catch (e: Exception) {
                _ethBalance.value = "Error: ${e.localizedMessage}"
                e.printStackTrace()
            }
        }
    }
}