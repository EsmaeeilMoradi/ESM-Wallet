package com.esm.esmwallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.esm.esmwallet.ui.theme.ESMWalletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dependencyGraph = (application as ESMWalletApp).dependencyGraph

            ESMWalletTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RootNavGraph(dependencyGraph = dependencyGraph)
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
    object History : BottomNavItem("History", Icons.Default.Info, "history_screen")
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ESMWalletTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
//            RootNavGraph(dependencyGraph = appState)
        }

    }
}
