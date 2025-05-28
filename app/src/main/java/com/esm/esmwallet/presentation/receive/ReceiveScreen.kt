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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.esm.esmwallet.presentation.viewmodel.WalletViewModel
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

@Composable
fun ReceiveScreen(
    modifier: Modifier = Modifier,
    paddingValues:PaddingValues,
    walletViewModel: WalletViewModel = viewModel()
) {
    val clipboardManager = LocalClipboardManager.current
    val walletAddress = walletViewModel.testWalletAddress
    var qrBitmap: Bitmap? =
        remember(walletAddress) {
            try {
                val barcodeEncoder = BarcodeEncoder()
                barcodeEncoder.encodeBitmap(walletAddress, BarcodeFormat.QR_CODE, 500, 500)
            } catch (e: Exception) {
                Log.e("ReceiveScreen", "Error generating QR code", e)
                null
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

        if (qrBitmap != null) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR Code for wallet address",
                modifier = Modifier.size(250.dp)
            )
        } else {
            Text("Error generating QR code", color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = walletAddress,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                clipboardManager.setText(AnnotatedString(walletAddress))
                Log.d("ReceiveScreen", "Wallet address copied: $walletAddress")
            },
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
                 Log.d("ReceiveScreen", "Share Button Clicked")
             },
             modifier = Modifier.fillMaxWidth(0.8f)
         ) {
             Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(20.dp))
             Spacer(modifier = Modifier.width(8.dp))
             Text("Share")
         }
    }
}