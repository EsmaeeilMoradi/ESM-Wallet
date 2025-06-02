package com.esm.esmwallet.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esm.esmwallet.R
import com.esm.esmwallet.data.model.Token
import com.esm.esmwallet.data.repository.WalletRepositoryImpl
import com.esm.esmwallet.domain.usecase.GetEthBalanceUseCase
import com.esm.esmwallet.domain.usecase.SendErc20TokenUseCase
import com.esm.esmwallet.domain.usecase.SendEthUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.RoundingMode


class WalletViewModel : ViewModel() {

    private val testPrivateKey = "50865d1f1dc2de719049c411a96f1d1be1e42d5af345ff6ec29fd6e53b801e10"
    val testWalletAddress = "0x2c6497d4492cdBAbB38D226353d5C656d4D71eB8"

    private val walletRepository = WalletRepositoryImpl()
    private val getEthBalanceUseCase = GetEthBalanceUseCase(walletRepository)
    private val sendEthUseCase = SendEthUseCase(walletRepository)
    private val sendErc20TokenUseCase =
        SendErc20TokenUseCase(walletRepository)

    // **  MTT  **
    private val mttContractAddress = "0xe4CB9f751Fe035B6365d233b780cd8c637D80cBe"
    private val mttDecimals = 18
    private val mttSymbol = "MTT"
    private val mttName = "My Test Token"



    private val _tokens = MutableStateFlow<List<Token>>(emptyList())
    val tokens: StateFlow<List<Token>> = _tokens.asStateFlow()

    private val _selectedToken = MutableStateFlow<Token?>(null)
    val selectedToken: StateFlow<Token?> = _selectedToken.asStateFlow()

    private val _sendStatus = MutableStateFlow<String?>(null)
    val sendStatus: StateFlow<String?> = _sendStatus.asStateFlow()

    fun setSendStatus(status: String?) {
        _sendStatus.value = status
    }

    init {
        loadInitialTokens()
        generateAndLogMnemonic()

    }
    private fun generateAndLogMnemonic() {
        viewModelScope.launch {
            try {
                val mnemonic = walletRepository.generateMnemonicPhrase()
                Log.d("MnemonicGen", "Generated Mnemonic: $mnemonic")
            } catch (e: Exception) {
                Log.e("MnemonicGen", "Error generating mnemonic", e)
            }
        }
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


                Log.d("WalletViewModel", "Attempting to load initial MTT balance for: $testWalletAddress")
                val mttBalanceWei = walletRepository.getErc20TokenBalance(
                    mttContractAddress,
                    testWalletAddress
                )
                val mttBalanceFormatted = BigDecimal(mttBalanceWei)
                    .divide(
                        BigDecimal(10).pow(mttDecimals),
                        mttDecimals, // از دسیمال واقعی توکن استفاده شود
                        RoundingMode.HALF_UP
                    )
                    .toPlainString()



//
//                // --- UPDATED TOKEN CONTRACT ADDRESSES FOR SEPOLIA ---
//                // **IMPORTANT:** Please verify these addresses on sepolia.etherscan.io
//                // WETH (Wrapped Ether) contract address on Sepolia
//                val wethContractAddress = "0x7b799e03F54e19bB4b2d699042646695cB8256F8" // Common WETH Sepolia address
//                val wethBalanceWei = walletRepository.getErc20TokenBalance(
//                    wethContractAddress,
//                    testWalletAddress
//                )
//                val wethDecimals = 18 // WETH usually has 18 decimals
//                val wethSymbol = "WETH"
//
//                val wethBalanceFormatted = BigDecimal(wethBalanceWei)
//                    .divide(
//                        BigDecimal(10).pow(wethDecimals),
//                        wethDecimals,
//                        RoundingMode.HALF_UP
//                    )
//                    .toPlainString()
//
//                // USDC (or another stablecoin) contract address on Sepolia as a replacement for DAI
//                val stableCoinContractAddress = "0x1c7D4B196Cb0C7B01d743Fbc6dEe94d93ad97a8c" // Common USDC Sepolia address
//                val stableCoinBalanceWei =
//                    walletRepository.getErc20TokenBalance(
//                        stableCoinContractAddress,
//                        testWalletAddress
//                    )
//                val stableCoinDecimals = 6 // USDC usually has 6 decimals
//                val stableCoinSymbol = "USDC" // Changing from DAI to USDC
//
//                val stableCoinBalanceFormatted = BigDecimal(stableCoinBalanceWei)
//                    .divide(
//                        BigDecimal(10).pow(stableCoinDecimals),
//                        stableCoinDecimals,
//                        RoundingMode.HALF_UP
//                    )
//                    .toPlainString()

                val initialTokens = listOf(
                    Token(
                        name = "Ethereum",
                        symbol = "ETH",
                        contractAddress = "",
                        decimals = 18,
                        balance = ethBalanceFormatted + " ETH",
                        iconResId = R.drawable.eth
                    ),
                    Token(
                        name = mttName,
                        symbol = mttSymbol,
                        contractAddress = mttContractAddress,
                        decimals = mttDecimals,
                        balance = "$mttBalanceFormatted $mttSymbol",
                        iconResId = R.drawable.ic_launcher_foreground // آیکون دلخواه برای MTT
                    )
                )
//                    Token(
//                        name = "Wrapped Ether",
//                        symbol = wethSymbol,
//                        contractAddress = wethContractAddress,
//                        decimals = wethDecimals,
//                        balance = "$wethBalanceFormatted $wethSymbol",
//                        iconResId = R.drawable.wexpoly
//                    ),
//                    Token(
//                        name = stableCoinSymbol, // Changed from DAI to USDC
//                        symbol = stableCoinSymbol,
//                        contractAddress = stableCoinContractAddress,
//                        decimals = stableCoinDecimals,
//                        balance = "$stableCoinBalanceFormatted $stableCoinSymbol",
//                        iconResId = R.drawable.usdt // You might want to update this icon if you have a USDC icon
//                    )
//                )
                _tokens.value = initialTokens
                _selectedToken.value = initialTokens.firstOrNull()

                Log.d("WalletViewModel", "Initial tokens loaded: ${_tokens.value}")

            } catch (e: Exception) {
                Log.e("WalletViewModel", "Error loading initial tokens: ${e.localizedMessage}", e)
                val errorTokens = listOf(
                    Token(
                        name = "Ethereum",
                        symbol = "ETH",
                        contractAddress = "",
                        decimals = 18,
                        balance = "Error",
                        iconResId = R.drawable.eth
                    ),
                    Token(
                        name = mttName,
                        symbol = mttSymbol,
                        contractAddress = mttContractAddress,
                        decimals = mttDecimals,
                        balance = "Error",
                        iconResId = R.drawable.ic_launcher_foreground
                    )
                    ,
                    Token(
                        name = "Wrapped Ether",
                        symbol = "WETH",
                        contractAddress = "0x7b799e03F54e19bB4b2d699042646695cB8256F8", // Updated WETH address
                        decimals = 18,
                        balance = "Error",
                        iconResId = R.drawable.wexpoly
                    ),
                    Token(
                        name = "USDC", // Changed from DAI to USDC
                        symbol = "USDC", // Changed from DAI to USDC
                        contractAddress = "0x1c7D4B196Cb0C7B01d743Fbc6dEe94d93ad97a8c", // Updated USDC address
                        decimals = 6, // Updated decimals for USDC
                        balance = "Error",
                        iconResId = R.drawable.usdt
                    )
                )
                _tokens.value = errorTokens
                _selectedToken.value = errorTokens.firstOrNull()
            }
        }
    }

    fun setSelectedToken(token: Token) {
        _selectedToken.value = token
        Log.d("WalletViewModel", "Selected token set to: ${token.symbol}")
    }

    fun updateTokenBalance(tokenSymbol: String, address: String) {
        viewModelScope.launch {
            try {
                val updatedBalance: String = when (tokenSymbol) {
                    "ETH" -> {
                        val balanceWei = getEthBalanceUseCase.invoke(address)
                        val balanceEther = BigDecimal(balanceWei)
                            .divide(BigDecimal(10).pow(18), 4, RoundingMode.HALF_UP)
                            .toPlainString()
                        "$balanceEther ETH"
                    }
                    mttSymbol -> {
                        Log.d("WalletViewModel", "Updating MTT balance for: $address")
                        val balanceWei = walletRepository.getErc20TokenBalance(mttContractAddress, address)
                        val balanceFormatted = BigDecimal(balanceWei)
                            .divide(
                                BigDecimal(10).pow(mttDecimals),
                                mttDecimals,
                                RoundingMode.HALF_UP
                            )
                            .toPlainString()
                        Log.d("WalletViewModel", "Updated MTT balance: $balanceFormatted $mttSymbol")
                        "$balanceFormatted $mttSymbol"
                    }


//                    "USDC" -> { // Changed from DAI to USDC
//                        val stableCoinContractAddress = "0x1c7D4B196Cb0C7B01d743Fbc6dEe94d93ad97a8c" // Updated USDC address
//                        val stableCoinBalanceWei =
//                            walletRepository.getErc20TokenBalance(stableCoinContractAddress, address)
//                        val stableCoinDecimals = 6 // Updated decimals for USDC
//                        val stableCoinBalanceFormatted = BigDecimal(stableCoinBalanceWei)
//                            .divide(
//                                BigDecimal(10).pow(stableCoinDecimals),
//                                stableCoinDecimals,
//                                RoundingMode.HALF_UP
//                            )
//                            .toPlainString()
//
//                        "$stableCoinBalanceFormatted USDC" // Changed symbol
//                    }
//                    "WETH" -> {
//                        val wethContractAddress = "0x7b799e03F54e19bB4b2d699042646695cB8256F8" // Updated WETH address
//                        val wethBalanceWei =
//                            walletRepository.getErc20TokenBalance(wethContractAddress, address)
//                        val wethDecimals = 18
//                        val wethBalanceFormatted = BigDecimal(wethBalanceWei)
//                            .divide(
//                                BigDecimal(10).pow(wethDecimals),
//                                wethDecimals,
//                                RoundingMode.HALF_UP
//                            )
//                            .toPlainString()
//                        "$wethBalanceFormatted WETH"
//                    }
                    else -> "Error: Unknown Token"
                }

                _tokens.value = _tokens.value.map { token ->
                    if (token.symbol == tokenSymbol) {
                        token.copy(balance = updatedBalance)
                    } else {
                        token
                    }
                }
                Log.d("WalletViewModel", "Balance updated for $tokenSymbol: $updatedBalance")

            } catch (e: Exception) {
                Log.e(
                    "WalletViewModel",
                    "Error updating $tokenSymbol balance: ${e.localizedMessage}",
                    e
                )
                e.printStackTrace()
                _tokens.value = _tokens.value.map { token ->
                    if (token.symbol == tokenSymbol) {
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

                updateTokenBalance("ETH", testWalletAddress)

            } catch (e: Exception) {
                _sendStatus.value = "Error sending ETH: ${e.localizedMessage}"
                Log.e("WalletViewModel", "Error sending ETH: ${e.localizedMessage}", e)
                e.printStackTrace()
            }
        }
    }

    fun sendErc20Token(
        tokenContractAddress: String,
        toAddress: String,
        amountDisplayValue: String,
        decimals: Int
    ) {
        _sendStatus.value = "Sending..."
        viewModelScope.launch {
            try {
                val amountBig = BigDecimal(amountDisplayValue)
                    .multiply(BigDecimal(10).pow(decimals))
                    .toBigInteger()

                val transactionHash = sendErc20TokenUseCase.invoke(
                    testPrivateKey,
                    tokenContractAddress,
                    toAddress,
                    amountBig
                )
                _sendStatus.value = "Transaction sent! Hash: $transactionHash"
                Log.d("WalletViewModel", "ERC-20 Transaction Hash: $transactionHash")

                _selectedToken.value?.let { selected ->
                    updateTokenBalance(selected.symbol, testWalletAddress)
                }

            } catch (e: Exception) {
                _sendStatus.value = "Error sending ERC-20 token: ${e.localizedMessage}"
                Log.e("WalletViewModel", "Error sending ERC-20 token: ${e.localizedMessage}", e)
                e.printStackTrace()
            }
        }
    }

//    suspend fun getErc20TokenBalance(
//        tokenContractAddress: String,
//        walletAddress: String
//    ): BigInteger {
//        return walletRepository.getErc20TokenBalance(tokenContractAddress, walletAddress)
//    }
}