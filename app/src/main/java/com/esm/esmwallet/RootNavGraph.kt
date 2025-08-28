package com.esm.esmwallet

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.esm.esmwallet.navigation.Screen
import com.esm.esmwallet.presentation.createwallet.CreateWalletScreen
import com.esm.esmwallet.presentation.history.TransactionHistoryScreen
import com.esm.esmwallet.presentation.home.HomeScreen
import com.esm.esmwallet.presentation.importtoken.ImportTokenScreen
import com.esm.esmwallet.presentation.importwallet.ImportWalletScreen
import com.esm.esmwallet.presentation.onboarding.OnboardingScreen
import com.esm.esmwallet.presentation.onboarding.OnboardingViewModel
import com.esm.esmwallet.presentation.receive.ReceiveScreen
import com.esm.esmwallet.presentation.send.SendScreen
import com.esm.esmwallet.presentation.token.TokenSelectionScreen
import com.esm.esmwallet.presentation.viewmodel.Erc20ViewModel
import com.esm.esmwallet.presentation.watch_address.WatchAddressScreen
import com.esm.esmwallet.presentation.welcome.WelcomeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootNavGraph(dependencyGraph: DependencyGraph) {
    // Accessing shared ViewModels and other data from the appState.
    val sharedWalletViewModel = dependencyGraph.walletViewModel
    val appStateViewModel = dependencyGraph.appStateViewModel

    val navController = rememberNavController()
    val startDestination by appStateViewModel.startDestination.collectAsStateWithLifecycle()

    val walletAddress by sharedWalletViewModel.walletAddress.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // The rest of the UI logic and data (bottom nav items, etc.) remains here.
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Trending,
        BottomNavItem.Swap,
        BottomNavItem.Earn,
        BottomNavItem.Discover,
        BottomNavItem.History
    )


    // A separate state that handles the bottom bar visibility
    val isBottomBarVisible =
        walletAddress != null && bottomNavItems.any { it.route == currentRoute }
    val isTopBarVisible =
        bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            if (isTopBarVisible) {
                CenterAlignedTopAppBar(
                    title = { Text("ESM Wallet") })
            }
        },

        bottomBar = {
            // Conditionally show the NavigationBar based on the state.
            if (isBottomBarVisible) {
                NavigationBar {
                    val navBackStackEntry = navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry.value?.destination?.route

                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (startDestination != "loading_route") {
            NavHost(
                navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Onboarding.route) {
                    val onboardingViewModel: OnboardingViewModel = viewModel(
                        factory = dependencyGraph.onboardingViewModelFactory
                    )
                    OnboardingScreen(viewModel = onboardingViewModel, navController = navController)
                }

                composable(route = Screen.Welcome.route) {
                    WelcomeScreen(navController = navController)
                }
                composable(route = Screen.WatchAddress.route) {
                    WatchAddressScreen(navController = navController)
                }

                composable(Screen.Welcome.route) {
                    WelcomeScreen(navController = navController)
                }
                composable(Screen.CreateWallet.route) {
                    CreateWalletScreen(
                        navController = navController,
                        walletViewModel = sharedWalletViewModel
                    )
                }
                composable(Screen.ImportWallet.route) {
                    ImportWalletScreen(
                        navController = navController,
                        walletViewModel = sharedWalletViewModel
                    )
                }
                composable(BottomNavItem.Home.route) { backStackEntry ->
                    HomeScreen(
                        paddingValues = innerPadding,
                        walletViewModel = sharedWalletViewModel
                    )
                }

                composable(BottomNavItem.Trending.route) { backStackEntry ->
                    SendScreen(
                        navController = navController,
                        paddingValues = innerPadding,
                        walletViewModel = sharedWalletViewModel
                    )

                }
                composable(BottomNavItem.Swap.route) {
                    Text(text = "Swap Screen Content", modifier = Modifier.padding(innerPadding))
                }
                composable(BottomNavItem.Earn.route) { backStackEntry ->
                    ReceiveScreen(
                        paddingValues = innerPadding,
                        walletViewModel = sharedWalletViewModel
                    )

                }
                composable(BottomNavItem.Discover.route) {
                    Text(
                        text = "Discover Screen Content",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                composable(Screen.Send.route) { backStackEntry ->
                    SendScreen(
                        navController = navController,
                        paddingValues = innerPadding,
                        walletViewModel = sharedWalletViewModel
                    )
                }

                composable(Screen.TokenSelection.route) { backStackEntry ->
                    TokenSelectionScreen(
                        navController = navController,
                        paddingValues = innerPadding,
                        walletViewModel = sharedWalletViewModel
                    )
                }
                composable(BottomNavItem.History.route) { // New composable route for History
                    TransactionHistoryScreen(
                        paddingValues = innerPadding, walletViewModel = sharedWalletViewModel
                    )
                }
                composable(Screen.ImportToken.route) {

                    val erc20ViewModel: Erc20ViewModel = viewModel(
                        factory = dependencyGraph.erc20ViewModelFactory // Use the factory from the graph
                    )


                    val tokenInfoState by erc20ViewModel.tokenInfo.collectAsState()
                    val isLoading by erc20ViewModel.isLoading.collectAsState()
                    val snackbarMessage by erc20ViewModel.snackbarMessage.collectAsState()

                    var contractAddressInput by remember { mutableStateOf("") }
                    var tokenNameInput by remember { mutableStateOf("") }
                    var tokenSymbolInput by remember { mutableStateOf("") }
                    var tokenDecimalsInput by remember { mutableStateOf("") }

                    LaunchedEffect(tokenInfoState) {
                        tokenInfoState?.let { info ->
                            tokenNameInput = info.name
                            tokenSymbolInput = info.symbol
                            tokenDecimalsInput = info.decimals.toString()
                            Log.d("ImportTokenScreen", "Token info updated in UI: $info")
                        }
                    }

                    ImportTokenScreen(
                        onBackClick = { navController.popBackStack() },
                        onImportClick = { contractAddress, name, symbol, decimals ->
                            if (contractAddress.isNotBlank()) {
                                erc20ViewModel.fetchTokenInfoByAddress(contractAddress)
                                contractAddressInput = contractAddress
                            } else {
                                erc20ViewModel.dismissSnackbar()
                                erc20ViewModel.snackbarMessage.value.equals("Contract address cannot be empty.")
                            }

                            Log.d(
                                "ImportTokenScreen",
                                "Import clicked: $contractAddress, $name, $symbol, $decimals"
                            )
                        },
                        isLoading = isLoading,
                        snackbarMessage = snackbarMessage,
                        onSnackbarDismiss = { erc20ViewModel.dismissSnackbar() },
                        contractAddress = contractAddressInput,
                        tokenName = tokenNameInput,
                        tokenSymbol = tokenSymbolInput,
                        tokenDecimals = tokenDecimalsInput,
                        onContractAddressChange = { contractAddressInput = it },
                        onTokenNameChange = { tokenNameInput = it },
                        onTokenSymbolChange = { tokenSymbolInput = it },
                        onTokenDecimalsChange = { tokenDecimalsInput = it })
                }

            }
        }
    }
}

