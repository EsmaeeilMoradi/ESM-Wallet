package com.esm.esmwallet.presentation.send

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.esm.esmwallet.presentation.viewmodel.WalletViewModel
import java.math.BigDecimal

@Composable
fun SendScreen(
    modifier: Modifier = Modifier,
    paddingValues:PaddingValues,
    walletViewModel: WalletViewModel = viewModel()
) {
    var recipientAddress by remember { mutableStateOf("") }
    var amountToSend by remember { mutableStateOf("") }

    //  Now we collect the list of tokens
    val tokens by walletViewModel.tokens.collectAsState()

    //  Find the ETH token from the list
    val ethToken = tokens.find { it.symbol == "ETH" }
    val currentEthBalance = ethToken?.balance ?: "Loading..." // Default to "Loading..." or "N/A"

    val sendStatus by walletViewModel.sendStatus.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Text(
            text = "Send Ethereum (ETH)",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Your current balance: $currentEthBalance",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
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
                if (newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                    amountToSend = newValue
                }
            },
            label = { Text("Amount (ETH)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Button(
                    onClick = {
                        // Use currentEthBalance for Max button logic
                        val balanceNumeric = currentEthBalance.replace(" ETH", "").toBigDecimalOrNull()
                        if (balanceNumeric != null && balanceNumeric > BigDecimal.ZERO) {
                            amountToSend = balanceNumeric.stripTrailingZeros().toPlainString()
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Max")
                }
            }
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                walletViewModel.sendEth(recipientAddress, amountToSend)
                Log.d("SendScreen", "Send Button Clicked")
                Log.d("SendScreen", "Recipient: $recipientAddress, Amount: $amountToSend")
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = recipientAddress.isNotBlank() && amountToSend.isNotBlank() && amountToSend.toDoubleOrNull() != null
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