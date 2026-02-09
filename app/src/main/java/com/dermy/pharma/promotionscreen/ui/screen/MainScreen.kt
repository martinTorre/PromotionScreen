package com.dermy.pharma.promotionscreen.ui.screen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dermy.pharma.promotionscreen.data.model.MediaType
import com.dermy.pharma.promotionscreen.ui.components.VideoPlayer
import com.dermy.pharma.promotionscreen.ui.viewmodel.MainViewModel
import com.dermy.pharma.promotionscreen.ui.viewmodel.MainViewModelFactory

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentIndex = uiState.currentIndex
    val mediaItems = uiState.mediaItems
    val currentItem = mediaItems.getOrNull(currentIndex)
    LaunchedEffect(currentIndex, mediaItems.size) {
        if (currentItem != null && currentItem.type == MediaType.IMAGE) {
            viewModel.startSlideTimer()
        }
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.stopSlideTimer() }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        when {
            uiState.isLoading -> {
                Text(
                    text = "Cargando…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.errorMessage != null && mediaItems.isEmpty() -> {
                Text(
                    text = uiState.errorMessage ?: "Error",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            currentItem != null -> {
                when (currentItem.type) {
                    MediaType.IMAGE -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(currentItem.url)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    MediaType.VIDEO -> {
                        VideoPlayer(
                            url = currentItem.url,
                            modifier = Modifier.fillMaxSize(),
                            fileId = null,
                            accessToken = null,
                            onEnded = viewModel::onVideoEnded
                        )
                    }
                }
            }
        }
    }
}
