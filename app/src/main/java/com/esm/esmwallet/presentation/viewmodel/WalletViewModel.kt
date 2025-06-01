package com.esm.esmwallet.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esm.esmwallet.R
import com.esm.esmwallet.data.model.Token
import com.esm.esmwallet.data.repository.WalletRepositoryImpl
import com.esm.esmwallet.domain.usecase.GetEthBalanceUseCase
import com.esm.esmwallet.domain.usecase.SendEthUseCase
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

    private val _tokens = MutableStateFlow<List<Token>>(emptyList())
    val tokens: StateFlow<List<Token>> = _tokens.asStateFlow()

    private val _sendStatus = MutableStateFlow<String?>(null)
    val sendStatus: StateFlow<String?> = _sendStatus.asStateFlow()

    init {
        loadInitialTokens() // Load ETH and DAI into _tokens list
    }


    private fun loadInitialTokens() {
        viewModelScope.launch {
            try {
                // Fetch ETH Balance
                val ethBalanceWei = getEthBalanceUseCase.invoke(testWalletAddress)
                val ethBalanceFormatted =
                    BigDecimal(ethBalanceWei).divide(
                        BigDecimal(10).pow(18),
                        4,
                        RoundingMode.HALF_UP
                    )
                        .toPlainString()

                // Fetch DAI Balance
                val daiContractAddress = "0x82fb927676b53b6ee07904780c7be9b4b50db80b"
                val daiBalanceWei =
                    walletRepository.getErc20TokenBalance( // Renamed variable to avoid conflict
                        daiContractAddress,
                        testWalletAddress
                    )
                val daiDecimals = walletRepository.getErc20TokenDecimals(daiContractAddress)
                val daiSymbol = walletRepository.getErc20TokenSymbol(daiContractAddress)

                // Convert DAI balance using its actual decimals
                val daiBalanceFormatted = BigDecimal(daiBalanceWei)
                    .divide(
                        BigDecimal(10).pow(daiDecimals),
                        daiDecimals,
                        RoundingMode.HALF_UP
                    ) // Corrected division with daiDecimals
                    .toPlainString()

                //  *** THIS IS THE CRITICAL FIX ***
                //  Assign the list of both ETH and DAI tokens to _tokens.value
                _tokens.value = listOf(
                    Token(
                        name = "Ethereum",
                        symbol = "ETH",
                        contractAddress = "", // ETH doesn't have a contract address
                        decimals = 18, // ETH has 18 decimals
                        balance = ethBalanceFormatted + " ETH",
                        iconResId = R.drawable.eth
                    ),
                    Token(
                        name = "DAI",
                        symbol = daiSymbol,
                        contractAddress = daiContractAddress,
                        decimals = daiDecimals,
                        balance = "$daiBalanceFormatted $daiSymbol", // Formatted balance with symbol
                        iconResId = R.drawable.usdt //  Replace with the correct DAI icon if you have one
                    )
                )

                Log.d("WalletViewModel", "Initial tokens loaded: ${_tokens.value}")

            } catch (e: Exception) {
                Log.e("WalletViewModel", "Error loading initial tokens: ${e.localizedMessage}", e)
                _tokens.value = listOf(
                    Token(
                        name = "Ethereum",
                        symbol = "ETH",
                        contractAddress = "",
                        decimals = 18,
                        balance = "Error",
                        iconResId = R.drawable.eth
                    ),
                    Token(
                        name = "DAI",
                        symbol = "DAI",
                        contractAddress = "0x82fb927676b53b6ee07904780c7be9b4b50db80b",
                        decimals = 18,
                        balance = "Error",
                        iconResId = R.drawable.usdt
                    )
                ) // Show error
            }
        }
    }


    // --- IMPORTANT: This function needs to update the _tokens list, not _ethBalance directly ---
    fun updateEthTokenBalance(address: String) {
        viewModelScope.launch {
            try {
                val balanceWei = getEthBalanceUseCase.invoke(address)
                val balanceEther = BigDecimal(balanceWei)
                    .divide(BigDecimal(10).pow(18), 4, RoundingMode.HALF_UP)
                    .toPlainString()

                // Update the ETH token in the _tokens list
                _tokens.value = _tokens.value.map { token ->
                    if (token.symbol == "ETH") {
                        token.copy(balance = "$balanceEther ETH")
                    } else {
                        token
                    }
                }
                Log.d("WalletViewModel", "ETH balance updated in tokens list: $balanceEther ETH")

            } catch (e: Exception) {
                Log.e(
                    "WalletViewModel",
                    "Error updating ETH token balance: ${e.localizedMessage}",
                    e
                )
                e.printStackTrace()
                // Update ETH token in list to "Error"
                _tokens.value = _tokens.value.map { token ->
                    if (token.symbol == "ETH") {
                        token.copy(balance = "Error")
                    } else {
                        token
                    }
                }
            }
        }
    }


    fun sendEth(toAddress: String, amountEther: String) {
        _sendStatus.value = "Sending..."
        viewModelScope.launch {
            try {
                val amountWei = Convert.toWei(amountEther, Convert.Unit.ETHER).toBigInteger()
                val transactionHash = sendEthUseCase.invoke(testPrivateKey, toAddress, amountWei)
                _sendStatus.value = "Transaction sent! Hash: $transactionHash"
                Log.d("WalletViewModel", "Transaction Hash: $transactionHash")

                // Re-fetch ETH balance after sending to update the tokens list
                updateEthTokenBalance(testWalletAddress)

            } catch (e: Exception) {
                _sendStatus.value = "Error sending ETH: ${e.localizedMessage}"
                Log.e("WalletViewModel", "Error sending ETH: ${e.localizedMessage}", e)
                e.printStackTrace()
            }
        }
    }

    // Keep this if you plan to use it for generic ERC-20 balance fetching elsewhere,
    // but loadDaiBalance is now integrated into loadInitialTokens
    suspend fun getErc20TokenBalance(
        tokenContractAddress: String,
        walletAddress: String
    ): BigInteger {
        return walletRepository.getErc20TokenBalance(tokenContractAddress, walletAddress)
    }

}
