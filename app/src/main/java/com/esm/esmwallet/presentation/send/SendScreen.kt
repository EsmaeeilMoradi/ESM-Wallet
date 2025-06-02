package com.esm.esmwallet.presentation.send

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.esm.esmwallet.R
import com.esm.esmwallet.data.model.Token
import com.esm.esmwallet.presentation.viewmodel.WalletViewModel
import com.esm.esmwallet.ui.theme.ESMWalletTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    walletViewModel: WalletViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedToken by walletViewModel.selectedToken.collectAsState()
    val sendStatus by walletViewModel.sendStatus.collectAsState()
    var toAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    LaunchedEffect(sendStatus) {
        sendStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            if (it.contains("Transaction sent!")) {
                toAddress = ""
                amount = ""
                walletViewModel.setSendStatus(null)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select Token",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navController.navigate("token_selection_screen")
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                selectedToken?.let { token ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        token.iconResId?.let {
                            Icon(
                                painter = painterResource(id = it),
                                contentDescription = token.name,
                                modifier = Modifier.size(32.dp)
                            )
                        } ?: run {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_token_placeholder),
                                contentDescription = "Token",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = token.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = token.symbol,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(text = token.balance, style = MaterialTheme.typography.bodyLarge)
                } ?: Text(text = "No token selected", style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Token")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = toAddress,
            onValueChange = { toAddress = it },
            label = { Text("Recipient Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = {
                // فقط اعداد و یک نقطه اعشار
                if (it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                    amount = it
                }
            },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (selectedToken == null) {
                    Toast.makeText(context, "Please select a token first", Toast.LENGTH_SHORT)
                        .show()
                    return@Button
                }
                if (toAddress.isBlank() || amount.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (selectedToken?.symbol == "ETH") {
                    walletViewModel.sendEth(toAddress, amount)
                } else if (selectedToken?.contractAddress != null) {
                    // برای ERC-20
                    selectedToken?.let { token ->
                        walletViewModel.sendErc20Token(
                            tokenContractAddress = token.contractAddress,
                            toAddress = toAddress,
                            amountDisplayValue = amount,
                            decimals = token.decimals
                        )
                    }
                } else {
                    Toast.makeText(context, "Unsupported token for sending", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedToken != null && toAddress.isNotBlank() && amount.isNotBlank()
        ) {
            Text("Send")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSendScreen() {
    ESMWalletTheme {
        val mockNavController = rememberNavController()
        val mockWalletViewModel = object : WalletViewModel() {
            override val selectedToken: StateFlow<Token?> = MutableStateFlow(
                Token(
                    name = "Ethereum",
                    symbol = "ETH",
                    balance = "1.234 ETH",
                    contractAddress = "",
                    iconResId = R.drawable.eth,
                    decimals = 18
                )
            ).asStateFlow()

            override val sendStatus: StateFlow<String?> = MutableStateFlow(null).asStateFlow()

        }
        SendScreen(
            navController = mockNavController,
            paddingValues = PaddingValues(0.dp),
            walletViewModel = mockWalletViewModel
        )
    }
}