package com.esm.esmwallet.ui.component

import com.esm.esmwallet.ui.theme.ESMWalletTheme


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

@Composable
fun RadialBackground() {
    var size by remember { mutableStateOf(Size.Zero) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                size = coordinates.size.toSize()
            }
            // Main background color is now #a7c6e6
            .background(Color(0xFFa7c6e6))
    ) {
        // 1st Radial Gradient - A light, complementary blue
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x8080a8d2), // Lighter blue
                            Color(0x0080a8d2)
                        ),
                        radius = 250.dp.dpToPx(),
                        center = Offset(
                            x = -50.dp.dpToPx(),
                            y = 300.dp.dpToPx()
                        )
                    )
                )
                .fillMaxSize()
        )

        // 2nd Radial Gradient - A soft orange/pink
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x40ffb080), // Soft orange/pink
                            Color(0x00ffb080)
                        ),
                        radius = 250.dp.dpToPx(),
                        center = Offset(
                            x = size.width / 2f,
                            y = 400.dp.dpToPx()
                        )
                    )
                )
                .fillMaxSize()
        )

        // 3rd Radial Gradient - A light teal
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x7380d2c7), // Light teal
                            Color(0x0080d2c7)
                        ),
                        radius = 250.dp.dpToPx(),
                        center = Offset(
                            x = size.width + 50.dp.dpToPx(),
                            y = 500.dp.dpToPx()
                        )
                    )
                )
                .fillMaxSize()
        )
    }
}

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Preview
@Composable
fun RadialBackgroundPreview() {
    ESMWalletTheme {
        RadialBackground()
    }
}