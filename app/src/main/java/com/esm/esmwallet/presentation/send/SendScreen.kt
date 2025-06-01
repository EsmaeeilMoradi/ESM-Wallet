package com.esm.esmwallet.presentation.send

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.esm.esmwallet.presentation.viewmodel.WalletViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    walletViewModel: WalletViewModel = viewModel()
) {
    var recipientAddress by remember { mutableStateOf("") }
    var amountToSend by remember { mutableStateOf("") }

    val tokens by walletViewModel.tokens.collectAsState()
    val selectedToken by walletViewModel.selectedToken.collectAsState() // Collect the selected token
    val sendStatus by walletViewModel.sendStatus.collectAsState()

    var expanded by remember { mutableStateOf(false) } // For dropdown menu

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Text(
            text = "Send Tokens",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Token Selection Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedToken?.symbol ?: "Select Token", // Display selected token symbol
                onValueChange = {},
                readOnly = true,
                label = { Text("Token") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor() // Crucial for dropdown anchor
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                tokens.forEach { token ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                token.iconResId?.let {
                                    Icon(
                                        painter = painterResource(id = it),
                                        contentDescription = token.name,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(token.name)
                            }
                        },
                        onClick = {
                            walletViewModel.setSelectedToken(token) // Update selected token in ViewModel
                            expanded = false
                            amountToSend = "" // Clear amount when token changes
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))


        // Display current balance of the selected token
        selectedToken?.let { token ->
            Text(
                text = "Your current balance: ${token.balance}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } ?: run {
            Text(
                text = "Your current balance: Loading...",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        OutlinedTextField(
            value = recipientAddress,
            onValueChange = { recipientAddress = it },
            label = { Text("Recipient Address") },
            placeholder = { Text("0x...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amountToSend,
            onValueChange = { newValue ->
                // Allow only numeric input with optional decimal point
                if (newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                    amountToSend = newValue
                }
            },
            label = { Text("Amount (${selectedToken?.symbol ?: ""})") }, // Dynamic label
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Button(
                    onClick = {
                        selectedToken?.let { token ->
                            val balanceWithoutSymbol =
                                token.balance.replace(token.symbol, "").trim()
                            val balanceNumeric = balanceWithoutSymbol.toBigDecimalOrNull()
                            if (balanceNumeric != null && balanceNumeric > BigDecimal.ZERO) {
                                // For ERC-20, use its decimals for stripTrailingZeros
                                amountToSend = balanceNumeric.stripTrailingZeros().toPlainString()
                            }
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp),
                    // Disable Max button if no token is selected or balance is not valid
                    enabled = selectedToken != null &&
                            selectedToken?.balance?.replace(selectedToken?.symbol ?: "", "")?.trim()
                                ?.toBigDecimalOrNull() != null &&
                            selectedToken?.balance?.replace(selectedToken?.symbol ?: "", "")?.trim()
                                ?.toBigDecimalOrNull()!! > BigDecimal.ZERO
                ) {
                    Text("Max")
                }
            }
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                selectedToken?.let { token ->
                    if (token.symbol == "ETH") {
                        walletViewModel.sendEth(recipientAddress, amountToSend)
                    } else {
                        // For ERC-20 tokens
                        if (token.contractAddress.isNotBlank()) {
                            walletViewModel.sendErc20Token(
                                token.contractAddress,
                                recipientAddress,
                                amountToSend,
                                token.decimals // Pass decimals for conversion
                            )
                        } else {
                            Log.e("SendScreen", "Error: ERC-20 token has no contract address.")
                            walletViewModel.setSendStatus("Error: Token contract address missing.")
                        }
                    }
                } ?: run {
                    Log.e("SendScreen", "Error: No token selected to send.")
                    walletViewModel.setSendStatus("Error: No token selected.")
                }
                Log.d(
                    "SendScreen",
                    "Send Button Clicked - Token: ${selectedToken?.symbol ?: "None"}, Recipient: $recipientAddress, Amount: $amountToSend"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = recipientAddress.isNotBlank() && amountToSend.isNotBlank() && amountToSend.toBigDecimalOrNull() != null && selectedToken != null
        ) {
            Text("Send")
        }

        sendStatus?.let { status ->
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = if (status.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}