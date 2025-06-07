package com.esm.esmwallet.presentation.importwallet

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.esm.esmwallet.navigation.Screen
import com.esm.esmwallet.presentation.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportWalletScreen(
    navController: NavController,
    walletViewModel: WalletViewModel
) {
    val context = LocalContext.current
    var mnemonicInput by remember { mutableStateOf("") }
    val isImporting by walletViewModel.isImportingWallet.collectAsState()
    val walletAddress by walletViewModel.walletAddress.collectAsState()

    LaunchedEffect(walletAddress) {
        if (walletAddress != null && !isImporting) {
            Toast.makeText(context, "Wallet imported successfully!", Toast.LENGTH_SHORT).show()
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Welcome.route) {
                    inclusive = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Import Existing Wallet") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Enter your 12 or 24-word recovery phrase",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = mnemonicInput,
                onValueChange = { newValue ->
                    mnemonicInput = newValue
                        .replace("\n", " ")
                        .replace("  ", " ")
                        .trim()
                },
                label = { Text("Recovery Phrase (e.g., word1 word2 ...)") },
                placeholder = { Text("Enter your mnemonic phrase here...") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .padding(bottom = 24.dp)
            )

            Button(
                onClick = {
                    val words = mnemonicInput.split(" ").filter { it.isNotBlank() }
                    if (words.size == 12 || words.size == 24) {
                        walletViewModel.importWalletFromMnemonic(words)
                    } else {
                        Toast.makeText(context, "Please enter a 12 or 24-word phrase.", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = mnemonicInput.isNotBlank() && !isImporting
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Import Wallet")
                }
            }
            walletAddress?.let { address ->
                Text(
                    text = "Imported Address: $address",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}