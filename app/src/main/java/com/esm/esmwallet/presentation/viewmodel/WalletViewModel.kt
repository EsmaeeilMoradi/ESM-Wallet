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
import com.esm.esmwallet.data.model.Transaction
import com.esm.esmwallet.data.wallet.WalletManager
import com.esm.esmwallet.util.Resource
import org.bitcoinj.crypto.MnemonicException


open class WalletViewModel ( private val walletManager: WalletManager = WalletManager()): ViewModel() {

    private val testPrivateKey = "50865d1f1dc2de719049c411a96f1d1be1e42d5af345ff6ec29fd6e53b801e10"
    val testWalletAddress = "0x2c6497d4492cdBAbB38D226353d5C656d4D71eB8"

    private val walletRepository = WalletRepositoryImpl()
    private val getEthBalanceUseCase = GetEthBalanceUseCase(walletRepository)
    private val sendEthUseCase = SendEthUseCase(walletRepository)
    private val sendErc20TokenUseCase =
        SendErc20TokenUseCase(walletRepository)

    private val mttContractAddress = "0xe4CB9f751Fe035B6365d233b780cd8c637D80cBe"
    private val mttDecimals = 18
    private val mttSymbol = "MTT"
    private val mttName = "My Test Token"


    private val _tokens = MutableStateFlow<List<Token>>(emptyList())
    open val tokens: StateFlow<List<Token>> = _tokens.asStateFlow()

    private val _selectedToken = MutableStateFlow<Token?>(null)
    open val selectedToken: StateFlow<Token?> = _selectedToken.asStateFlow()

    private val _sendStatus = MutableStateFlow<String?>(null)
    open val sendStatus: StateFlow<String?> = _sendStatus.asStateFlow()

    private val _transactionHistory =
        MutableStateFlow<Resource<List<Transaction>>>(Resource.Loading())
    val transactionHistory: StateFlow<Resource<List<Transaction>>> =
        _transactionHistory.asStateFlow()

    fun setSendStatus(status: String?) {
        _sendStatus.value = status
    }
    private val _mnemonicPhrase = MutableStateFlow<List<String>?>(null)
    val mnemonicPhrase: StateFlow<List<String>?> = _mnemonicPhrase

    private val _currentWallet = MutableStateFlow<org.bitcoinj.wallet.Wallet?>(null)
    val currentWallet: StateFlow<org.bitcoinj.wallet.Wallet?> = _currentWallet

    private val _walletAddress = MutableStateFlow<String?>(null)
    val walletAddress: StateFlow<String?> = _walletAddress

    private val _walletBalance = MutableStateFlow<String>("0.0 ETH") // Initial balance, can be updated later
    val walletBalance: StateFlow<String> = _walletBalance // Expose as StateFlow
    private val _isImportingWallet = MutableStateFlow(false)
    val isImportingWallet: StateFlow<Boolean> = _isImportingWallet.asStateFlow()
    init {
        loadInitialTokens()
        generateAndLogMnemonic()
        viewModelScope.launch {
            Log.d(
                "EtherscanTest",
                "Attempting to fetch ETH balance from Etherscan via getEthBalanceForTest()..."
            )
            val result =
                walletRepository.getEthBalanceForTest(testWalletAddress)
            result.onSuccess { balance ->
                Log.d("EtherscanTest", "SUCCESS! ETH Balance from Etherscan (init): $balance")
            }.onFailure { error ->
                Log.e(
                    "EtherscanTest",
                    "ERROR! Failed to fetch ETH Balance from Etherscan (init): ${error.message}"
                )
            }
        }

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


                Log.d(
                    "WalletViewModel",
                    "Attempting to load initial MTT balance for: $testWalletAddress"
                )
                val mttBalanceWei = walletRepository.getErc20TokenBalance(
                    mttContractAddress,
                    testWalletAddress
                )
                val mttBalanceFormatted = BigDecimal(mttBalanceWei)
                    .divide(
                        BigDecimal(10).pow(mttDecimals),
                        mttDecimals,
                        RoundingMode.HALF_UP
                    )
                    .toPlainString()

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
                        iconResId = R.drawable.ic_launcher_foreground
                    )
                )
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
                    ),
                    Token(
                        name = "Wrapped Ether",
                        symbol = "WETH",
                        contractAddress = "0x7b799e03F54e19bB4b2d699042646695cB8256F8",
                        decimals = 18,
                        balance = "Error",
                        iconResId = R.drawable.wexpoly
                    ),
                    Token(
                        name = "USDC",
                        symbol = "USDC",
                        contractAddress = "0x1c7D4B196Cb0C7B01d743Fbc6dEe94d93ad97a8c",
                        decimals = 6,
                        balance = "Error",
                        iconResId = R.drawable.usdt
                    )
                )
                _tokens.value = errorTokens
                _selectedToken.value = errorTokens.firstOrNull()
            }
        }
    }

    open fun setSelectedToken(token: Token) {
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
                        val balanceWei =
                            walletRepository.getErc20TokenBalance(mttContractAddress, address)
                        val balanceFormatted = BigDecimal(balanceWei)
                            .divide(
                                BigDecimal(10).pow(mttDecimals),
                                mttDecimals,
                                RoundingMode.HALF_UP
                            )
                            .toPlainString()
                        Log.d(
                            "WalletViewModel",
                            "Updated MTT balance: $balanceFormatted $mttSymbol"
                        )
                        "$balanceFormatted $mttSymbol"
                    }

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

    fun fetchTransactionHistory(address: String) {
        viewModelScope.launch {
            _transactionHistory.value = Resource.Loading()
            val ethHistoryResource = walletRepository.getEthTransactionHistory(address)

            if (ethHistoryResource is Resource.Success) {
                val combinedHistory = (ethHistoryResource.data
                    ?: emptyList())
                val sortedHistory = combinedHistory.sortedByDescending { it.timestamp }
                _transactionHistory.value = Resource.Success(sortedHistory)
            } else {
                _transactionHistory.value = Resource.Error(
                    (ethHistoryResource as? Resource.Error)?.message
                        ?: "Unknown error fetching ETH transaction history."
                )
            }
        }
    }

    /**
     * Generates a new Mnemonic Phrase and updates the UI state.
     * @param numWords The desired number of words (12 or 24).
     */
    fun generateNewMnemonic(numWords: Int = 12) {
        viewModelScope.launch {
            try {
                val newMnemonic = walletManager.generateMnemonic(numWords)
                _mnemonicPhrase.value = newMnemonic
                // Optionally, generate wallet immediately after mnemonic
                // val wallet = walletManager.restoreWalletFromMnemonic(newMnemonic)
                // _currentWallet.value = wallet
                // _walletAddress.value = walletManager.getEthAddressFromPrivateKey(walletManager.getPrivateKeyFromWallet(wallet))
                println("D/WalletViewModel: Mnemonic generated (${newMnemonic.size} words): ${newMnemonic.joinToString(" ")}")
                // After generating, you might immediately try to import it for testing,
                // or let the UI trigger the importWalletFromMnemonic
            } catch (e: MnemonicException) {
                println("E/WalletViewModel: Mnemonic generation error: ${e.message}")
                _mnemonicPhrase.value = null // Clear mnemonic on error
                // TODO: Update a user-facing error message state here (e.g., for a SnackBar)
            } catch (e: IllegalArgumentException) {
                println("E/WalletViewModel: Invalid mnemonic word count requested: ${e.message}")
                _mnemonicPhrase.value = null
            } catch (e: Exception) {
                println("E/WalletViewModel: General error generating mnemonic: ${e.message}")
                _mnemonicPhrase.value = null
            }
        }
    }

    /**
     * Imports a wallet from a given Mnemonic Phrase and updates the UI states.
     * Also derives the Ethereum address from the restored wallet.
     * @param mnemonic A list of words representing the mnemonic phrase.
     */
    fun importWalletFromMnemonic(mnemonic: List<String>) {
        viewModelScope.launch {
            _isImportingWallet.value = true
            try {
                val restoredWallet = walletManager.restoreWalletFromMnemonic(mnemonic)
                _currentWallet.value = restoredWallet
                val privateKey = walletManager.getPrivateKeyFromWallet(restoredWallet)
                val address = walletManager.getEthAddressFromPrivateKey(privateKey)
                _walletAddress.value = address

                println("D/WalletViewModel: Wallet imported successfully. Address: ${address}")
            } catch (e: MnemonicException.MnemonicLengthException) {
                println("E/WalletViewModel: Invalid mnemonic length: ${e.message}")
            } catch (e: MnemonicException.MnemonicChecksumException) {
                println("E/WalletViewModel: Invalid mnemonic checksum: ${e.message}")
            } catch (e: MnemonicException) {
                println("E/WalletViewModel: Invalid mnemonic: ${e.message}")
            } catch (e: IllegalStateException) {
                println("E/WalletViewModel: Wallet key extraction error: ${e.message}")
            } catch (e: Exception) {
                println("E/WalletViewModel: General error importing wallet: ${e.message}")
            } finally {
                _isImportingWallet.value = false
            }
        }
    }

    /**
     * Clears the current mnemonic and wallet states.
     * Useful when logging out or preparing for a new wallet creation/import process.
     */
    fun clearWalletState() {
        _mnemonicPhrase.value = null
        _currentWallet.value = null
        _walletAddress.value = null
        _walletBalance.value = "0.0 ETH"
        println("D/WalletViewModel: Wallet state cleared.")
    }

}