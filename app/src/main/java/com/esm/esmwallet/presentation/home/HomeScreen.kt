package com.esm.esmwallet.presentation.home


import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.esm.esmwallet.presentation.components.TokenItem
import com.esm.esmwallet.presentation.viewmodel.WalletViewModel


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    walletViewModel: WalletViewModel = viewModel()
) {
    val ethBalance by walletViewModel.ethBalance.collectAsState()
    val tokens by walletViewModel.tokens.collectAsState()

    walletViewModel.loadDaiBalance(tokenContractAddress = "0x82fb927676b53b6ee07904780c7be9b4b50db80b", walletAddress = "0x2c6497d4492cdBAbB38D226353d5C656d4D71eB8")
    val daiBalance by walletViewModel.daiBalance.collectAsState()

    Column(modifier = modifier.padding(paddingValues)) {
        Text(
            text = "Total Balance",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp)
        )
        Text(
            text = ethBalance,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
        )
        Text(
            text = "DAIBalance",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp)
        )
        Text(
            text = daiBalance,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { Log.d("HomeScreen", "Send Button Clicked") },
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Send", modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Send")
                }
            }

            Spacer(Modifier.width(16.dp))

            Button(
                onClick = { Log.d("HomeScreen", "Receive Button Clicked") },
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Receive", modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Receive")
                }
            }
        }


        Text(
            text = "Your Tokens",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        LazyColumn {
            items(tokens) { token ->
                TokenItem(token = token)
            }
        }
    }
}