package com.esm.esmwallet.presentation.importtoken

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportTokenScreen(
    onBackClick: () -> Unit,
    onImportClick: (contractAddress: String, name: String, symbol: String, decimals: String) -> Unit,
    isLoading: Boolean,
    snackbarMessage: String?,
    onSnackbarDismiss: () -> Unit,
    contractAddress: String,
    tokenName: String,
    tokenSymbol: String,
    tokenDecimals: String,
    onContractAddressChange: (String) -> Unit,
    onTokenNameChange: (String) -> Unit,
    onTokenSymbolChange: (String) -> Unit,
    onTokenDecimalsChange: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(message = snackbarMessage)
            onSnackbarDismiss()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Crypto") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = contractAddress,
                onValueChange = onContractAddressChange,
                label = { Text("Contract Address") },
                placeholder = { Text("0x...") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = tokenName,
                onValueChange = onTokenNameChange,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = tokenSymbol,
                onValueChange = onTokenSymbolChange,
                label = { Text("Symbol") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = tokenDecimals,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        onTokenDecimalsChange(newValue)
                    }
                },
                label = { Text("Decimals") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onImportClick(contractAddress, tokenName, tokenSymbol, tokenDecimals)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading &&
                        contractAddress.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Import")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewImportTokenScreen() {
    MaterialTheme {
        ImportTokenScreen(
            onBackClick = {},
            onImportClick = { _, _, _, _ -> },
            isLoading = false,
            snackbarMessage = null,
            onSnackbarDismiss = {},
            contractAddress = "0x...",
            tokenName = "Test Token",
            tokenSymbol = "TST",
            tokenDecimals = "18",
            onContractAddressChange = {},
            onTokenNameChange = {},
            onTokenSymbolChange = {},
            onTokenDecimalsChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewImportTokenScreenLoading() {
    MaterialTheme {
        ImportTokenScreen(
            onBackClick = {},
            onImportClick = { _, _, _, _ -> },
            isLoading = true,
            snackbarMessage = "Fetching token details...",
            onSnackbarDismiss = {},
            contractAddress = "0x...",
            tokenName = "",
            tokenSymbol = "",
            tokenDecimals = "",
            onContractAddressChange = {},
            onTokenNameChange = {},
            onTokenSymbolChange = {},
            onTokenDecimalsChange = {}
        )
    }
}