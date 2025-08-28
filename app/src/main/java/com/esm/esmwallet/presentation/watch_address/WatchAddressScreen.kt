// File: app/src/main/java/com/esm/esmwallet/presentation/watch_address/WatchAddressScreen.kt

package com.esm.esmwallet.presentation.watch_address

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.esm.esmwallet.R
import com.esm.esmwallet.ui.theme.ESMWalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchAddressScreen(
    navController: NavController,
    viewModel: WatchAddressViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is WatchAddressEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is WatchAddressEvent.NavigateToHome -> {
                    // TODO: Navigate to the Home Screen
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.watch_address_screen_title)) },
                navigationIcon = {
                    // Back button functionality
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_desc)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Set the background color to white
                .background(Color.White)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text(text = stringResource(R.string.watch_address_field_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.address,
                    onValueChange = viewModel::onAddressChanged,
                    label = { Text(text = stringResource(R.string.watch_address_field_address_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Button(
                    onClick = viewModel::onWatchClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = uiState.isButtonEnabled
                ) {
                    Text(text = stringResource(R.string.watch_address_button_watch))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WatchAddressScreenPreview() {
    ESMWalletTheme {
        WatchAddressScreen(navController = rememberNavController())
    }
}