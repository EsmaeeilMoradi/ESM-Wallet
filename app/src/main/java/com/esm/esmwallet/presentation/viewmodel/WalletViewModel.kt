package com.esm.esmwallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esm.esmwallet.R
import com.esm.esmwallet.data.model.Token
import com.esm.esmwallet.data.repository.WalletRepositoryImpl
import com.esm.esmwallet.domain.usecase.GetEthBalanceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode


class WalletViewModel : ViewModel() {

    private val getEthBalanceUseCase = GetEthBalanceUseCase(WalletRepositoryImpl())

    private val _ethBalance = MutableStateFlow<String>("Loading...")
    val ethBalance: StateFlow<String> = _ethBalance

    private val _tokens = MutableStateFlow<List<Token>>(emptyList())
    val tokens: StateFlow<List<Token>> = _tokens.asStateFlow()

    val testWalletAddress = "0xYourActualEthereumWalletAddressHere"

    init {
        fetchEthBalance(testWalletAddress)
        _tokens.value = listOf(

            Token(
                "Ethereum",
                "ETH",
                _ethBalance.value,
                R.drawable.eth
            ),
            Token("Bitcoin", "BTC", "0.0000 BTC", R.drawable.btc),
            Token("Tether USD", "USDT", "0.00 USDT", R.drawable.usdt)
        )
    }

    fun fetchEthBalance(address: String) {
        viewModelScope.launch {
            try {
                val balanceWei = getEthBalanceUseCase(address)
                val balanceEther = BigDecimal(balanceWei)
                    .divide(BigDecimal(10).pow(18), 4, RoundingMode.HALF_UP)
                    .toPlainString()

                _ethBalance.value = "$balanceEther ETH"

                _tokens.value = _tokens.value.map { token ->
                    if (token.symbol == "ETH") {
                        token.copy(balance = _ethBalance.value, iconResId = R.drawable.eth)


                    } else {
                        token
                    }
                }

            } catch (e: Exception) {
                _ethBalance.value = "Error: ${e.localizedMessage}"
                e.printStackTrace()
            }
        }
    }
}