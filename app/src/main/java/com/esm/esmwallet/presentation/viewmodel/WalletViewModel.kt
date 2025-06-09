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
import com.esm.esmwallet.data.preferences.WalletDataStore
import com.esm.esmwallet.data.wallet.WalletManager
import com.esm.esmwallet.util.Resource
import kotlinx.coroutines.flow.first

// Web3j imports
import org.web3j.crypto.Credentials
// REMOVED: import org.web3j.crypto.MnemonicException // <<-- این خط را حذف کنید!

open class WalletViewModel(
    private val walletManager: WalletManager = WalletManager(),
    private val walletDataStore: WalletDataStore
) : ViewModel() {

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

    private val _currentPrivateKey = MutableStateFlow<String?>(null)
    val currentPrivateKey: StateFlow<String?> = _currentPrivateKey

    private val _currentWalletCredentials = MutableStateFlow<Credentials?>(null)
    val currentWalletCredentials: StateFlow<Credentials?> = _currentWalletCredentials

    private val _walletAddress = MutableStateFlow<String?>(null)
    val walletAddress: StateFlow<String?> = _walletAddress

    private val _walletBalance =
        MutableStateFlow<String>("0.0 ETH")
    val walletBalance: StateFlow<String> = _walletBalance
    private val _isImportingWallet = MutableStateFlow(false)

    private val _isCreatingWallet = MutableStateFlow(false)
    val isCreatingWallet: StateFlow<Boolean> = _isCreatingWallet.asStateFlow()

    val isImportingWallet: StateFlow<Boolean> = _isImportingWallet.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        loadWalletFromStorage()
    }

    private fun loadInitialTokens() {
        viewModelScope.launch {
            val address = _walletAddress.value
            if (!address.isNullOrBlank()) {
                try {
                    val ethBalanceWei = getEthBalanceUseCase.invoke(address)
                    val ethBalanceFormatted =
                        BigDecimal(ethBalanceWei).divide(
                            BigDecimal(10).pow(18),
                            4,
                            RoundingMode.HALF_UP
                        ).toPlainString()

                    Log.d("WalletViewModel", "Attempting to load initial MTT balance for: $address")
                    val mttBalanceWei = walletRepository.getErc20TokenBalance(
                        mttContractAddress,
                        address
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
                            balance = "$ethBalanceFormatted ETH",
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
                        )
                    )
                    _tokens.value = errorTokens
                    _selectedToken.value = errorTokens.firstOrNull()
                    _snackbarMessage.value = "Failed to load token balances."
                }
            } else {
                Log.w("WalletViewModel", "Wallet address not available to load initial tokens.")
            }
        }
    }

    open fun setSelectedToken(token: Token) {
        _selectedToken.value = token
        Log.d("WalletViewModel", "Selected token set to: ${token.symbol}")
    }
    fun updateTokenBalance(tokenSymbol: String, address: String) {
        viewModelScope.launch {
            if (address.isBlank()) {
                Log.w("WalletViewModel", "Cannot update token balance: Wallet address is blank.")
                return@launch
            }
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
                        val mttBalanceFormatted = BigDecimal(balanceWei) // <<-- تعریف mttBalanceFormatted
                            .divide(
                                BigDecimal(10).pow(mttDecimals),
                                mttDecimals,
                                RoundingMode.HALF_UP
                            )
                            .toPlainString()
                        Log.d(
                            "WalletViewModel",
                            "Updated MTT balance: $mttBalanceFormatted $mttSymbol"
                        )
                        // <<-- تغییر اینجا: مستقیماً مقدار را برگردانید
                        "$mttBalanceFormatted $mttSymbol"
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
                _snackbarMessage.value = "Failed to update $tokenSymbol balance."
            }
        }
    }
    fun sendEth(toAddress: String, amountEther: String) {
        _sendStatus.value = "Sending..."
        viewModelScope.launch {
            val currentPrivateKey = _currentPrivateKey.value
            if (currentPrivateKey == null) {
                _sendStatus.value = "Error: Wallet not loaded. Please create or import a wallet."
                _snackbarMessage.value = _sendStatus.value
                return@launch
            }
            try {
                val amountWei = Convert.toWei(amountEther, Convert.Unit.ETHER).toBigInteger()
                val transactionHash = sendEthUseCase.invoke(currentPrivateKey, toAddress, amountWei)
                _sendStatus.value = "Transaction sent! Hash: $transactionHash"
                Log.d("WalletViewModel", "Transaction Hash: $transactionHash")

                _walletAddress.value?.let { address ->
                    updateTokenBalance("ETH", address)
                }

            } catch (e: Exception) {
                _sendStatus.value = "Error sending ETH: ${e.localizedMessage}"
                Log.e("WalletViewModel", "Error sending ETH: ${e.localizedMessage}", e)
                e.printStackTrace()
                _snackbarMessage.value = _sendStatus.value
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
            val currentPrivateKey = _currentPrivateKey.value
            if (currentPrivateKey == null) {
                _sendStatus.value = "Error: Wallet not loaded. Please create or import a wallet."
                _snackbarMessage.value = _sendStatus.value
                return@launch
            }
            try {
                val amountBig = BigDecimal(amountDisplayValue)
                    .multiply(BigDecimal(10).pow(decimals))
                    .toBigInteger()

                val transactionHash = sendErc20TokenUseCase.invoke(
                    currentPrivateKey,
                    tokenContractAddress,
                    toAddress,
                    amountBig
                )
                _sendStatus.value = "Transaction sent! Hash: $transactionHash"
                Log.d("WalletViewModel", "ERC-20 Transaction Hash: $transactionHash")

                _selectedToken.value?.let { selected ->
                    _walletAddress.value?.let { address ->
                        updateTokenBalance(selected.symbol, address)
                    }
                }

            } catch (e: Exception) {
                _sendStatus.value = "Error sending ERC-20 token: ${e.localizedMessage}"
                Log.e("WalletViewModel", "Error sending ERC-20 token: ${e.localizedMessage}", e)
                e.printStackTrace()
                _snackbarMessage.value = _sendStatus.value
            }
        }
    }

    fun fetchTransactionHistory(address: String) {
        if (address.isBlank()) {
            Log.w("WalletViewModel", "Cannot fetch transaction history: Wallet address is blank.")
            _transactionHistory.value = Resource.Error("Wallet address not available.")
            return
        }
        viewModelScope.launch {
            _transactionHistory.value = Resource.Loading()
            val ethHistoryResource = walletRepository.getEthTransactionHistory(address)

            if (ethHistoryResource is Resource.Success) {
                val combinedHistory = (ethHistoryResource.data ?: emptyList())
                val sortedHistory = combinedHistory.sortedByDescending { it.timestamp }
                _transactionHistory.value = Resource.Success(sortedHistory)
            } else {
                _transactionHistory.value = Resource.Error(
                    (ethHistoryResource as? Resource.Error)?.message
                        ?: "Unknown error fetching transaction history."
                )
                _snackbarMessage.value = _transactionHistory.value.message
            }
        }
    }

    /**
     * Generates a new Wallet, saves its Private Key securely, and updates UI state.
     */
    fun createNewWallet() {
        viewModelScope.launch {
            _isCreatingWallet.value = true
            _isLoading.value = true
            try {
                Log.d("WalletViewModel", "Attempting to create new wallet...")
                val (credentials, mnemonic) = walletManager.createNewWallet()

                val privateKey = walletManager.getPrivateKeyFromCredentials(credentials)
                walletDataStore.saveEncryptedPrivateKey(privateKey)

                val ethAddress = walletManager.getEthAddressFromPrivateKey(privateKey)
                walletDataStore.saveWalletAddress(ethAddress)

                _currentWalletCredentials.value = credentials
                _currentPrivateKey.value = privateKey
                _mnemonicPhrase.value = mnemonic
                _walletAddress.value = ethAddress

                Log.d("WalletViewModel", "Wallet created successfully. Address: $ethAddress")
                Log.d("WalletViewModel", "Private Key securely stored. Mnemonic: ${mnemonic.joinToString(" ")}")

                loadInitialTokens()

            } catch (e: Exception) { // Catch all exceptions
                Log.e("WalletViewModel", "Error creating new wallet: ${e.message}", e)
                _snackbarMessage.value = "Error creating wallet: ${e.localizedMessage}"
            } finally {
                _isCreatingWallet.value = false
                _isLoading.value = false
            }
        }
    }

    /**
     * Imports a wallet from a given Mnemonic Phrase, saves its Private Key securely,
     * and updates the UI states.
     * @param mnemonic A list of words representing the mnemonic phrase.
     */
    fun importWalletFromMnemonic(mnemonic: List<String>) {
        viewModelScope.launch {
            _isImportingWallet.value = true
            _isLoading.value = true
            try {
                Log.d("WalletViewModel", "Attempting to import wallet from mnemonic...")
                val credentials = walletManager.restoreWalletFromMnemonic(mnemonic)

                val privateKey = walletManager.getPrivateKeyFromCredentials(credentials)
                walletDataStore.saveEncryptedPrivateKey(privateKey)

                val ethAddress = walletManager.getEthAddressFromPrivateKey(privateKey)
                walletDataStore.saveWalletAddress(ethAddress)

                _currentWalletCredentials.value = credentials
                _currentPrivateKey.value = privateKey
                _mnemonicPhrase.value = mnemonic
                _walletAddress.value = ethAddress

                Log.d("WalletViewModel", "Wallet imported successfully. Address: $ethAddress")

                loadInitialTokens()
            } catch (e: IllegalArgumentException) { // Changed MnemonicException to IllegalArgumentException
                val errorMessage = "Invalid Mnemonic Phrase."
                Log.e("WalletViewModel", "Error importing wallet: ${e.message}", e)
                _snackbarMessage.value = errorMessage
            } catch (e: Exception) { // Catch all other exceptions
                Log.e("WalletViewModel", "Error importing wallet: ${e.message}", e)
                _snackbarMessage.value = "Error importing wallet: ${e.localizedMessage}"
            } finally {
                _isImportingWallet.value = false
                _isLoading.value = false
            }
        }
    }

    /**
     * Clears the current wallet data from storage and ViewModel states.
     */
    fun clearWalletData() {
        viewModelScope.launch {
            walletDataStore.clearWalletData()
            _currentWalletCredentials.value = null
            _currentPrivateKey.value = null
            _mnemonicPhrase.value = emptyList()
            _walletAddress.value = null
            _tokens.value = emptyList()
            _selectedToken.value = null
            _transactionHistory.value = Resource.Loading()

            Log.d("WalletViewModel", "Wallet data cleared (logout).")
            _snackbarMessage.value = "Wallet data cleared."
        }
    }

    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }

    private fun loadWalletFromStorage() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val decryptedPrivateKey = walletDataStore.getDecryptedPrivateKey()

                if (decryptedPrivateKey != null && decryptedPrivateKey.isNotBlank()) {
                    val address = walletManager.getEthAddressFromPrivateKey(decryptedPrivateKey)

                    val credentials = Credentials.create(decryptedPrivateKey)
                    _currentWalletCredentials.value = credentials

                    _currentPrivateKey.value = decryptedPrivateKey
                    _walletAddress.value = address

                    Log.d("WalletViewModel", "Wallet loaded from storage. Address: $address")

                    loadInitialTokens()
                    fetchTransactionHistory(address)

                } else {
                    Log.d("WalletViewModel", "No wallet (private key) found in storage.")
                    _currentWalletCredentials.value = null
                    _currentPrivateKey.value = null
                    _mnemonicPhrase.value = null
                    _walletAddress.value = null
                    _tokens.value = emptyList()
                    _selectedToken.value = null
                }
            } catch (e: Exception) {
                Log.e("WalletViewModel", "Error loading wallet from storage: ${e.localizedMessage}", e)
                _snackbarMessage.value = "Failed to load wallet: ${e.localizedMessage}"
                clearWalletData()
            } finally {
                _isLoading.value = false
            }
        }
    }
}