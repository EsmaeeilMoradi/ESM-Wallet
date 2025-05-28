package com.esm.esmwallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.esm.esmwallet.presentation.home.HomeScreen
import com.esm.esmwallet.presentation.send.SendScreen
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
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Trending,
        BottomNavItem.Swap,
        BottomNavItem.Earn,
        BottomNavItem.Discover
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ESM Wallet") }
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = false,
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
    ) { innerPadding ->
        NavHost(navController, startDestination = BottomNavItem.Home.route) {
            composable(BottomNavItem.Home.route) { backStackEntry ->
                val walletViewModel: WalletViewModel = viewModel(backStackEntry)
                HomeScreen(paddingValues = innerPadding, walletViewModel = walletViewModel)

//                Column(modifier = Modifier.padding(innerPadding)) {
//                    Text(
//                        text = "Home Screen Content",
//                        style = MaterialTheme.typography.headlineMedium,
//                        modifier = Modifier.padding(bottom = 8.dp)
//                    )
//                    Text(
//                        text = "ETH Balance: $ethBalance",
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                }

            }
            composable(BottomNavItem.Trending.route) {backStackEntry->
//                Text(text = "Trending Screen Content", modifier = Modifier.padding(innerPadding))
                val walletViewModel: WalletViewModel = viewModel(backStackEntry)
                SendScreen(paddingValues = innerPadding, walletViewModel = walletViewModel)

            }
            composable(BottomNavItem.Swap.route) {
                Text(text = "Swap Screen Content", modifier = Modifier.padding(innerPadding))
            }
            composable(BottomNavItem.Earn.route) {
                Text(text = "Earn Screen Content", modifier = Modifier.padding(innerPadding))
            }
            composable(BottomNavItem.Discover.route) {
                Text(text = "Discover Screen Content", modifier = Modifier.padding(innerPadding))
            }
            composable("send_screen") { backStackEntry ->
                val walletViewModel: WalletViewModel = viewModel(backStackEntry)
                SendScreen(paddingValues = innerPadding, walletViewModel = walletViewModel)
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
