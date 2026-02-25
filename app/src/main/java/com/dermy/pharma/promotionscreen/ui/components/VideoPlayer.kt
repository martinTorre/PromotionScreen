package com.dermy.pharma.promotionscreen.ui.components

import android.net.Uri
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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.dermy.pharma.promotionscreen.R

@Composable
fun VideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    isLocalFile: Boolean = false,
    fileId: String? = null,
    accessToken: String? = null,
    onEnded: () -> Unit = {}
) {
    val playbackKey = if (isLocalFile) "local:$url" else "url:$url"

    key(playbackKey) {
        val context = LocalContext.current
        val exoPlayer: ExoPlayer = remember(key1 = playbackKey) {
            val dataSourceFactory = DefaultDataSource.Factory(context)
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
            val mediaUri = if (isLocalFile) {
                Uri.fromFile(java.io.File(url)).toString()
            } else {
                url
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
                    (this as PlayerView).apply {
                        player = exoPlayer
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                }
            },
            modifier = modifier
        )
    }
}