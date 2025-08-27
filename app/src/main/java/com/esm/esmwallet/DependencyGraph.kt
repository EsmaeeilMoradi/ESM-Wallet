package com.esm.esmwallet

import android.content.Context
import com.esm.esmwallet.data.db.AppDatabase
import com.esm.esmwallet.data.onboarding.OnboardingRepositoryImpl
import com.esm.esmwallet.data.preferences.WalletDataStore
import com.esm.esmwallet.data.remote.AlchemyApiService
import com.esm.esmwallet.data.remote.Web3jClient
import com.esm.esmwallet.data.repository.Erc20Repository
import com.esm.esmwallet.data.wallet.WalletManager
import com.esm.esmwallet.presentation.onboarding.OnboardingViewModel
import com.esm.esmwallet.presentation.viewmodel.Erc20ViewModel
import com.esm.esmwallet.presentation.viewmodel.WalletViewModel


class DependencyGraph(private val context: Context) {
    // Lazy initialization ensures these are created only when needed.
    // Repositories

    private val onboardingRepository: OnboardingRepositoryImpl by lazy { OnboardingRepositoryImpl() }
    // Services and Repositories
    private val walletDataStore: WalletDataStore by lazy { WalletDataStore(context) }
    private val appDatabase: AppDatabase by lazy { AppDatabase.getDatabase(context) }
    private val walletManager: WalletManager by lazy { WalletManager() }

    // ViewModels
    val onboardingViewModelFactory: OnboardingViewModel.Factory by lazy {
        OnboardingViewModel.Factory(onboardingRepository)
    }
    val appStateViewModelFactory: AppStateViewModel.Factory by lazy {
        AppStateViewModel.Factory(walletDataStore)
    }
    val walletViewModel: WalletViewModel by lazy {
        WalletViewModel(walletManager, walletDataStore)
    }

    val alchemyApiService: AlchemyApiService by lazy { AlchemyApiService(Web3jClient.buildWeb3j()) }
    val erc20Repository: Erc20Repository by lazy {
        Erc20Repository(alchemyApiService)
    }
    val erc20ViewModelFactory: Erc20ViewModel.Factory by lazy {
        Erc20ViewModel.Factory(
            repository = erc20Repository,
            customTokenDao = appDatabase.customTokenDao()
        )
    }

    // And a ViewModel for the application's global state and navigation logic
    val appStateViewModel: AppStateViewModel by lazy {
        AppStateViewModel(walletDataStore)
    }

}
