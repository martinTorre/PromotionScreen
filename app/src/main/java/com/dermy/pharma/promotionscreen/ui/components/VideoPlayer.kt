package com.dermy.pharma.promotionscreen.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.dermy.pharma.promotionscreen.R

private const val DRIVE_API_MEDIA_URL = "https://www.googleapis.com/drive/v3/files/"

@Composable
fun VideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    fileId: String? = null,
    accessToken: String? = null,
    onEnded: () -> Unit = {}
) {
    val playbackKey = when {
        fileId != null && accessToken != null -> "drive:$fileId"
        else -> "url:$url"
    }
    key(playbackKey) {
        val context = LocalContext.current
        val exoPlayer: ExoPlayer = remember(key1 = playbackKey) {
            val dataSourceFactory: DefaultDataSource.Factory = when {
                fileId != null && accessToken != null -> {
                    val httpFactory = DefaultHttpDataSource.Factory()
                        .setDefaultRequestProperties(mapOf("Authorization" to "Bearer $accessToken"))
                    DefaultDataSource.Factory(context, httpFactory)
                }
                else -> DefaultDataSource.Factory(context)
            }
            val mediaSourceFactory = DefaultMediaSourceFactory(context)
                .setDataSourceFactory(dataSourceFactory)
            ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
                .apply {
                    repeatMode = Player.REPEAT_MODE_OFF
                    playWhenReady = true
                }
        }
        DisposableEffect(playbackKey) {
            val mediaUri = when {
                fileId != null && accessToken != null -> "${DRIVE_API_MEDIA_URL}${fileId}?alt=media"
                else -> url
            }
            exoPlayer.setMediaItem(MediaItem.fromUri(mediaUri))
            exoPlayer.prepare()
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onEnded()
                    }
                }
            }
            exoPlayer.addListener(listener)
            onDispose {
                exoPlayer.removeListener(listener)
                exoPlayer.release()
            }
        }
        AndroidView(
            factory = { ctx ->
                LayoutInflater.from(ctx).inflate(R.layout.view_video_player, null).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    (this as PlayerView).player = exoPlayer
                }
            },
            modifier = modifier
        )
    }
}
