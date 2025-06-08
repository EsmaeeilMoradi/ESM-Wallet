package com.esm.esmwallet.presentation.token

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
fun TokenSelectionScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    walletViewModel: WalletViewModel = viewModel()
) {
    val tokens by walletViewModel.tokens.collectAsState()
    val selectedToken by walletViewModel.selectedToken.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Token") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (tokens.isEmpty()) {
                Text(
                    text = "No tokens available or loading...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                LazyColumn {
                    items(tokens) { token ->
                        TokenSelectionItem(
                            token = token,
                            isSelected = token == selectedToken,
                            onTokenSelected = {
                                walletViewModel.setSelectedToken(it)
                                navController.popBackStack()
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun TokenSelectionItem(
    token: Token,
    isSelected: Boolean,
    onTokenSelected: (Token) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTokenSelected(token) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = token.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = token.symbol,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isSelected) {
            Icon(
                painter = painterResource(id = R.drawable.ic_checked),
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewTokenSelectionScreen() {
//    ESMWalletTheme {
//        val mockNavController = rememberNavController()
//        val mockWalletViewModel = object : WalletViewModel() {
//            private val _mockTokens = MutableStateFlow(
//
//                listOf(
//                    Token(
//                        name = "Ethereum",
//                        symbol = "ETH",
//                        balance = "1.234 ETH",
//                        contractAddress = "",
//                        iconResId = R.drawable.eth,
//                        decimals = 18
//                    ),
//                    Token(
//                        name = "My Test Token",
//                        symbol = "MTT",
//                        balance = "50000.00 MTT",
//                        contractAddress = "0xe4CB9f751Fe035B6365d233b780cd8c637D80cBe",
//                        iconResId = R.drawable.ic_launcher_foreground,
//                        decimals = 18
//                    ),
//                    Token(
//                        name = "Another Token",
//                        symbol = "ATK",
//                        balance = "10.5 ATK",
//                        contractAddress = "0x...",
//                        iconResId = R.drawable.ic_token_placeholder,
//                        decimals = 6
//                    )
//                )
//            )
//            override val tokens: StateFlow<List<Token>> = _mockTokens.asStateFlow()
//            private lateinit var _mockSelectedToken: MutableStateFlow<Token?>
//
//            init {
//                _mockSelectedToken = MutableStateFlow(_mockTokens.value.firstOrNull())
//            }
//
//            override val selectedToken: StateFlow<Token?>
//                get() = _mockSelectedToken.asStateFlow()
//
//
//            override fun setSelectedToken(token: Token) {
//                _mockSelectedToken.value = token
//            }
//        }
//        TokenSelectionScreen(
//            navController = mockNavController,
//            paddingValues = PaddingValues(0.dp),
//            walletViewModel = mockWalletViewModel
//        )
//    }
//}