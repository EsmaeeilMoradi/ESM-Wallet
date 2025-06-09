package com.esm.esmwallet.presentation.receive

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.esm.esmwallet.presentation.viewmodel.WalletViewModel
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import android.content.Intent // Required for sharing
import android.net.Uri // Required for sharing
import androidx.compose.runtime.getValue // این رو اضافه کن
import androidx.compose.runtime.setValue // (اگر متغیر قابل تغییر و از var استفاده می‌کنید)

@Composable
fun ReceiveScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    walletViewModel: WalletViewModel = viewModel()
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current // Get context for Intents
    val walletAddress by walletViewModel.walletAddress.collectAsState() // Use 'by' for direct access

    // QR code generation, now correctly handles null/blank walletAddress
    val qrBitmap: Bitmap? =
        remember(walletAddress) {
            if (!walletAddress.isNullOrBlank()) { // Check for valid address
                try {
                    val barcodeEncoder = BarcodeEncoder()
                    // Pass walletAddress directly, as it's a String now due to !isNullOrBlank() check
                    barcodeEncoder.encodeBitmap(walletAddress, BarcodeFormat.QR_CODE, 500, 500)
                } catch (e: Exception) {
                    Log.e("ReceiveScreen", "Error generating QR code", e)
                    null
                }
            } else {
                null // If address is null or blank, no QR code
            }
        }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Receive Ethereum (ETH)",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Conditional display for QR code
        if (qrBitmap != null) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR Code for wallet address",
                modifier = Modifier.size(250.dp)
            )
        } else {
            // Display a message while waiting for address or if QR generation failed
            Text(
                text = if (walletAddress.isNullOrBlank()) "Loading wallet address..." else "Error generating QR code",
                color = if (walletAddress.isNullOrBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Display wallet address, showing loading text if null
        Text(
            text = walletAddress ?: "Loading Address...",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Only copy if walletAddress is not null or blank
                walletAddress?.let { address ->
                    if (address.isNotBlank()) {
                        clipboardManager.setText(AnnotatedString(address))
                        Log.d("ReceiveScreen", "Wallet address copied: $address")
                    } else {
                        Log.w("ReceiveScreen", "Cannot copy address: Wallet address is blank.")
                    }
                } ?: run {
                    Log.w("ReceiveScreen", "Cannot copy address: Wallet address is null.")
                }
            },
            // Button is enabled only if a valid address is available
            enabled = !walletAddress.isNullOrBlank(),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Copy Address",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Copy Address")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // Only share if walletAddress is not null or blank
                walletAddress?.let { address ->
                    if (address.isNotBlank()) {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "My ESM-Wallet Address: $address")
                            putExtra(Intent.EXTRA_SUBJECT, "My Wallet Address")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Wallet Address"))
                        Log.d("ReceiveScreen", "Share Button Clicked for address: $address")
                    } else {
                        Log.w("ReceiveScreen", "Cannot share address: Wallet address is blank.")
                    }
                } ?: run {
                    Log.w("ReceiveScreen", "Cannot share address: Wallet address is null.")
                }
            },
            // Button is enabled only if a valid address is available
            enabled = !walletAddress.isNullOrBlank(),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share")
        }
    }
}