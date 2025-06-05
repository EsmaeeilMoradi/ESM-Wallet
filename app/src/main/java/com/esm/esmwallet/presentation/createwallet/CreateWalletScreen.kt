package com.esm.esmwallet.presentation.createwallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.esm.esmwallet.navigation.Screen
import com.esm.esmwallet.presentation.viewmodel.WalletViewModel

@Composable
fun CreateWalletScreen(
    navController: NavController,
    walletViewModel: WalletViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create New Wallet Screen (Coming Soon!)")
        Button(onClick = {
            navController.popBackStack()
        }) {
            Text("Back to Welcome")
        }
    }
}