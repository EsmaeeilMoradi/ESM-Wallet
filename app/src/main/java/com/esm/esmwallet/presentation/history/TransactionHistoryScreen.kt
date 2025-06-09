package com.esm.esmwallet.presentation.history

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.esm.esmwallet.data.model.Transaction
import com.esm.esmwallet.presentation.viewmodel.WalletViewModel
import com.esm.esmwallet.util.Resource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionHistoryScreen(
    paddingValues: PaddingValues,
    walletViewModel: WalletViewModel
) {
    val transactionHistory by walletViewModel.transactionHistory.collectAsState()
//    val currentWalletAddress = walletViewModel.testWalletAddress // یا آدرس ولت جاری کاربر را از ViewModel بگیرید
    val currentWalletAddress by walletViewModel.walletAddress.collectAsState()
    LaunchedEffect(key1 = currentWalletAddress) {
        // اگر currentWalletAddress نال نباشد، وارد بلاک .let می شود.
        // در غیر این صورت، else بلوک .let اجرا می شود.
        currentWalletAddress?.let { address ->
            if (address.isNotBlank()) {
                Log.d("TransactionHistoryScreen", "Fetching transactions for address: $address")
                walletViewModel.fetchTransactionHistory(address)
            } else {
                Log.w("TransactionHistoryScreen", "Wallet address is blank, not fetching transactions.")
                // TODO: اینجا میتونی UI رو برای حالت آدرس خالی به روز کنی.
                // مثلاً: walletViewModel.updateTransactionHistoryState(Resource.Error("Wallet address is blank."))
            }
        } ?: run { // این '?: run {}' معادل 'else' برای .let است
            Log.w("TransactionHistoryScreen", "Wallet address is null, not fetching transactions.")
            // TODO: اینجا میتونی UI رو برای حالت آدرس نال به روز کنی.
            // مثلاً: walletViewModel.updateTransactionHistoryState(Resource.Error("Please create or import a wallet."))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Transaction History",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (transactionHistory) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val transactions = transactionHistory.data
                if (transactions.isNullOrEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No transactions found for this address.", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        val validAddress = currentWalletAddress ?: "" // Fallback to empty string if somehow null (less likely with the LaunchedEffect check)
                        items(transactions) { transaction ->
                            TransactionItem(transaction = transaction, myAddress = validAddress)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error: ${transactionHistory.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            is Resource.Empty -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading transaction history...", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, myAddress: String) {
    val amountColor = if (transaction.isReceived) Color.Green else Color.Red
    val sign = if (transaction.isReceived) "+" else "-"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // TODO: Implement click to view full transaction details (e.g., on Etherscan)
                // val url = "https://sepolia.etherscan.io/tx/${transaction.hash}"
                // val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                // context.startActivity(intent)
            }
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (transaction.type) {
                    Transaction.TransactionType.SENT -> "Sent ${transaction.tokenSymbol ?: "ETH"}"
                    Transaction.TransactionType.RECEIVED -> "Received ${transaction.tokenSymbol ?: "ETH"}"
                    Transaction.TransactionType.UNKNOWN -> "Unknown Transaction"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$sign ${transaction.getFormattedValue()}",
                style = MaterialTheme.typography.titleMedium,
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "To: ${if (transaction.isSent) transaction.to.take(8) + "..." + transaction.to.takeLast(6) else transaction.from.take(8) + "..." + transaction.from.takeLast(6)}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Hash: ${transaction.hash.take(10)}...",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = formatDate(transaction.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        if (transaction.isError) {
            Text(
                text = "Status: Failed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = "Status: Success",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp * 1000)
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}