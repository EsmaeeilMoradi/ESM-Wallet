package com.esm.esmwallet.presentation.security_terms


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.esm.esmwallet.R
import com.esm.esmwallet.navigation.Screen
import com.esm.esmwallet.ui.theme.ESMWalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityTermsScreen(
    navController: NavController,
    nextRoute: String?,
    viewModel: SecurityTermsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Listen for navigation events from the ViewModel
    LaunchedEffect(key1 = Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is SecurityTermsEvent.NavigateToNextScreen -> {
                    // Navigate to the correct destination using the passed-in route
                    if (nextRoute != null) {
                        navController.navigate(nextRoute) {
                            popUpTo(Screen.SecurityTerms.route) { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.security_terms_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close_button_desc)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Section 1: Secure Backup
                TermsAndConditionsItem(
                    text = stringResource(R.string.security_terms_desc_1),
                    isChecked = uiState.hasAcceptedFirstTerm,
                    onCheckedChange = viewModel::onFirstTermAccepted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Disabling PIN
                TermsAndConditionsItem(
                    text = stringResource(R.string.security_terms_desc_2),
                    isChecked = uiState.hasAcceptedSecondTerm,
                    onCheckedChange = viewModel::onSecondTermAccepted
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = viewModel::onNextClicked,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.isButtonEnabled
                ) {
                    Text(text = stringResource(R.string.next_button_text))
                }
            }
        }
    }
}

@Composable
fun TermsAndConditionsItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SecurityTermsScreenPreview() {
    ESMWalletTheme {
        SecurityTermsScreen(navController = rememberNavController(),nextRoute = "test_route")
    }
}
