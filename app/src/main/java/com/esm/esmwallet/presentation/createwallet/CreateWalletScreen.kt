package com.esm.esmwallet.presentation.createwallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.esm.esmwallet.navigation.Screen
import com.esm.esmwallet.presentation.viewmodel.WalletViewModel
import com.google.accompanist.flowlayout.FlowRow
import com.esm.esmwallet.R
import android.util.Log
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWalletScreen(
    navController: NavController,
    walletViewModel: WalletViewModel
) {
    val context = LocalContext.current
    val mnemonic by walletViewModel.mnemonicPhrase.collectAsState()
    val walletAddress by walletViewModel.walletAddress.collectAsState()
    val isLoading by walletViewModel.isLoading.collectAsState()
    val snackbarMessage by walletViewModel.snackbarMessage.collectAsState()

    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mnemonic, isLoading) {
        if (mnemonic.isNullOrEmpty() && !isLoading) {
            Log.d("CreateWalletScreen", "Mnemonic is null/empty and not loading, calling createNewWallet.")
            walletViewModel.createNewWallet()
        }
    }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            scope.launch {
                snackbarHostState.showSnackbar(message = snackbarMessage!!)
                walletViewModel.dismissSnackbar()
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Your Recovery Phrase",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Write down or save these 12 words in order and keep them in a safe place. Anyone with your phrase can take your assets.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val currentMnemonic = mnemonic
                if (!currentMnemonic.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            FlowRow(
                                mainAxisSpacing = 8.dp,
                                crossAxisSpacing = 8.dp,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                currentMnemonic.forEachIndexed { index, word ->
                                    SuggestionChip(
                                        onClick = {  },
                                        label = {
                                            Text(
                                                text = "${index + 1}. $word",
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 16.sp
                                            )
                                        },
                                        modifier = Modifier.wrapContentWidth()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val clipboard =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText(
                                        "Mnemonic Phrase",
                                        currentMnemonic.joinToString(" ")
                                    )
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Mnemonic Copied!", Toast.LENGTH_SHORT)
                                        .show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                enabled = !isLoading
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.content_copy),
                                    contentDescription = "Copy Mnemonic"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy to Clipboard")
                            }
                        }
                    }
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Text("Generating Mnemonic...", modifier = Modifier.padding(top = 16.dp))
                }

                walletAddress?.let { address ->
                    Text(
                        text = "Your Wallet Address:\n$address",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Do not share your recovery phrase with anyone! It gives full access to your wallet.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !mnemonic.isNullOrEmpty() && !isLoading
                ) {
                    Text("I've written it down")
                }
            }
        }
    }
}