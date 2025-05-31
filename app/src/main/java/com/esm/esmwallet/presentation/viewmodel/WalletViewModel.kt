package com.esm.esmwallet.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esm.esmwallet.R
import com.esm.esmwallet.data.model.Token
import com.esm.esmwallet.data.repository.WalletRepositoryImpl
import com.esm.esmwallet.domain.usecase.GetEthBalanceUseCase
import com.esm.esmwallet.domain.usecase.SendEthUseCase // <--- ایمپورت جدید
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.web3j.utils.Convert

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode


class WalletViewModel : ViewModel() {

    // !!! PRIVATE KEY    ( Sepolia Testnet) !!!
    private val testPrivateKey = "50865d1f1dc2de719049c411a96f1d1be1e42d5af345ff6ec29fd6e53b801e10"

    val testWalletAddress = "0x2c6497d4492cdBAbB38D226353d5C656d4D71eB8"

    private val walletRepository = WalletRepositoryImpl()
    private val getEthBalanceUseCase = GetEthBalanceUseCase(walletRepository)
    private val sendEthUseCase = SendEthUseCase(walletRepository)

    private val _ethBalance = MutableStateFlow("Loading...")
    val ethBalance: StateFlow<String> = _ethBalance.asStateFlow()

    private val _tokens = MutableStateFlow<List<Token>>(emptyList())
    val tokens: StateFlow<List<Token>> = _tokens.asStateFlow()

    private val _sendStatus = MutableStateFlow<String?>(null)
    val sendStatus: StateFlow<String?> = _sendStatus.asStateFlow()

    init {
        fetchEthBalance(testWalletAddress)
        _tokens.value = listOf(
            Token(
                "Ethereum",
                "ETH",
                "0.00 ETH",
                R.drawable.eth
            ),
            Token("Bitcoin", "BTC", "0.0000 BTC", R.drawable.btc),
            Token("Tether USD", "0.00 USDT", "0.00 USDT", R.drawable.usdt)
        )
    }

    fun fetchEthBalance(address: String) {
        viewModelScope.launch {
            try {
                val balanceWei = getEthBalanceUseCase.invoke(address)
                val balanceEther = BigDecimal(balanceWei)
                    .divide(BigDecimal(10).pow(18), 4, RoundingMode.HALF_UP)
                    .toPlainString()

                _ethBalance.value = "$balanceEther ETH"

                _tokens.value = _tokens.value.map { token ->
                    if (token.symbol == "ETH") {
                        token.copy(balance = "$balanceEther ETH", iconResId = R.drawable.eth)
                    } else {
                        token
                    }
                }
                Log.d("WalletViewModel", "Fetched balance: $balanceEther ETH")

            } catch (e: Exception) {
                _ethBalance.value = "Error: ${e.localizedMessage}"
                Log.e("WalletViewModel", "Error fetching balance: ${e.localizedMessage}", e)
                e.printStackTrace()
            }
        }
    }

    fun sendEth(toAddress: String, amountEther: String) {
        _sendStatus.value = "Sending..."
        viewModelScope.launch {
            try {
                //    Ether -> Wei
                val amountWei = Convert.toWei(amountEther, Convert.Unit.ETHER).toBigInteger()

                // For sending
                val transactionHash = sendEthUseCase.invoke(testPrivateKey, toAddress, amountWei)
                _sendStatus.value = "Transaction sent! Hash: $transactionHash"
                Log.d("WalletViewModel", "Transaction Hash: $transactionHash")

                // Re fetch
                fetchEthBalance(testWalletAddress)

            } catch (e: Exception) {
                _sendStatus.value = "Error sending ETH: ${e.localizedMessage}"
                Log.e("WalletViewModel", "Error sending ETH: ${e.localizedMessage}", e)
                e.printStackTrace()
            }
        }
    }
}