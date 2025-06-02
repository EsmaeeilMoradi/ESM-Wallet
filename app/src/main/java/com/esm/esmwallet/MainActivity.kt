package com.esm.esmwallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.esm.esmwallet.presentation.home.HomeScreen
import com.esm.esmwallet.presentation.receive.ReceiveScreen
import com.esm.esmwallet.presentation.send.SendScreen
import com.esm.esmwallet.presentation.token.TokenSelectionScreen
import com.esm.esmwallet.presentation.viewmodel.WalletViewModel
import com.esm.esmwallet.ui.theme.ESMWalletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ESMWalletTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen()
                }
            }
        }
    }
}


sealed class BottomNavItem(var title: String, var icon: ImageVector, var route: String) {
    object Home : BottomNavItem("Home", Icons.Default.Home, "home")
    object Trending : BottomNavItem("Trending", Icons.Default.Info, "trending")
    object Swap : BottomNavItem("Swap", Icons.Default.Settings, "swap")
    object Earn : BottomNavItem("Earn", Icons.Default.Settings, "earn")
    object Discover : BottomNavItem("Discover", Icons.Default.Settings, "discover")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val sharedWalletViewModel: WalletViewModel = viewModel()

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Trending,
        BottomNavItem.Swap,
        BottomNavItem.Earn,
        BottomNavItem.Discover
    )

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text("ESM Wallet") })
    }, bottomBar = {
        NavigationBar {
            val navBackStackEntry = navController.currentBackStackEntryAsState() // <<== تغییر اینجا
            val currentRoute = navBackStackEntry.value?.destination?.route // <<== تغییر اینجا

            items.forEach { item ->
                NavigationBarItem(
                    icon = { Icon(item.icon, contentDescription = item.title) },
                    label = { Text(item.title) },
                    selected = currentRoute == item.route, // <<== این رو تغییر بده
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    })
            }
        }
    }) { innerPadding ->
        NavHost(navController, startDestination = BottomNavItem.Home.route) {

            composable(BottomNavItem.Home.route) { backStackEntry ->
                HomeScreen(paddingValues = innerPadding, walletViewModel = sharedWalletViewModel)
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
                ReceiveScreen(paddingValues = innerPadding, walletViewModel = sharedWalletViewModel)

            }
            composable(BottomNavItem.Discover.route) {
                Text(text = "Discover Screen Content", modifier = Modifier.padding(innerPadding))
            }
            composable("send_screen") { backStackEntry ->
                SendScreen(
                    navController = navController,
                    paddingValues = innerPadding,
                    walletViewModel = sharedWalletViewModel
                )
            }

            composable("token_selection_screen") { backStackEntry ->
                TokenSelectionScreen(
                    navController = navController,
                    paddingValues = innerPadding,
                    walletViewModel = sharedWalletViewModel
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ESMWalletTheme {
        MainScreen()
    }
}
