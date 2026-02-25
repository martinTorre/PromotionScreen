package com.dermy.pharma.promotionscreen.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import com.dermy.pharma.promotionscreen.util.isTvDevice

@Composable
fun PortraitOnTvWrapper(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    if (!context.isTvDevice()) {
        Box(modifier = modifier.fillMaxSize()) { content() }
        return
    }
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val portraitWidth: Dp = maxHeight
        val portraitHeight: Dp = maxWidth
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.Center)
                    .size(portraitWidth, portraitHeight)
                    .graphicsLayer {
                        rotationZ = 90f
                        transformOrigin = TransformOrigin.Center
                        clip = false
                    }
            ) {
                Box(
                    modifier = Modifier
                        .requiredSize(portraitWidth, portraitHeight)
                        .fillMaxSize()
                ) {
                    content()
                }
            }
        }
    }
}
