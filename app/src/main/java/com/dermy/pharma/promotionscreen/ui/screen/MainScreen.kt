package com.dermy.pharma.promotionscreen.ui.screen

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.dermy.pharma.promotionscreen.ui.components.PortraitOnTvWrapper
import com.dermy.pharma.promotionscreen.ui.components.VideoPlayer
import com.dermy.pharma.promotionscreen.util.isTvDevice
import com.dermy.pharma.promotionscreen.ui.viewmodel.LoadingPhase
import com.dermy.pharma.promotionscreen.ui.viewmodel.MainViewModel
import com.dermy.pharma.promotionscreen.ui.viewmodel.MainViewModelFactory
import java.io.File

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
                val message = when (uiState.loadingPhase) {
                    LoadingPhase.DELETING -> "Borrando material viejo (${uiState.loadingProgress}%)..."
                    LoadingPhase.DOWNLOADING -> "Descargando material nuevo (${uiState.loadingProgress}%)..."
                    LoadingPhase.READY -> "Preparando..."
                }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
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
                val isTv = context.isTvDevice()
                when (currentItem.type) {
                    MediaType.IMAGE -> {
                        val imageData: Any = if (currentItem.localPath != null) {
                            File(currentItem.localPath)
                        } else {
                            currentItem.url
                        }
                        PortraitOnTvWrapper {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageData)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = if (isTv) ContentScale.FillBounds else ContentScale.Fit
                            )
                        }
                    }
                    MediaType.VIDEO -> {
                        PortraitOnTvWrapper {
                            VideoPlayer(
                                url = currentItem.localPath ?: currentItem.url,
                                isLocalFile = currentItem.localPath != null,
                                modifier = Modifier.fillMaxSize(),
                                onEnded = viewModel::onVideoEnded
                            )
                        }
                    }
                }
            }
        }
    }
}