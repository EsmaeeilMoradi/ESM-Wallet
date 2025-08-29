package com.esm.esmwallet.presentation.new_wallet


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.esm.esmwallet.R
import com.esm.esmwallet.ui.component.RadialBackground
import com.esm.esmwallet.ui.theme.ESMWalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewWalletScreen(
    navController: NavController,
    viewModel: NewWalletViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.new_wallet_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_desc)
                        )
                    }
                },
                actions = {
                    Text(
                        text = stringResource(R.string.create_button_text),
                        modifier = Modifier
                            .clickable(enabled = uiState.isCreateButtonEnabled) {
                                viewModel.onCreateClicked()
                            }
                            .padding(end = 16.dp),
                        color = if (uiState.isCreateButtonEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            RadialBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Wallet Icon
                Image(
                    painter = painterResource(R.drawable.ic_wallet),
                    contentDescription = stringResource(R.string.wallet_icon_desc),
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Wallet Name Field
                OutlinedTextField(
                    value = uiState.walletName,
                    onValueChange = viewModel::onWalletNameChanged,
                    label = { Text(text = stringResource(R.string.wallet_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = viewModel::onClearWalletNameClicked) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_button_desc)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Advanced Settings Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onAdvancedClicked() }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(text = stringResource(R.string.advanced_settings_label),
                        style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_right),
                        contentDescription = stringResource(R.string.advanced_settings_desc)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewWalletScreenPreview() {
    ESMWalletTheme {
        NewWalletScreen(navController = rememberNavController())
    }
}