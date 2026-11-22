package com.decklauncher.ui.components

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.decklauncher.data.ConsoleIntro
import com.decklauncher.data.IntroManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BootIntroPlayer(
    intro: ConsoleIntro,
    onIntroFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val isFinished = remember { java.util.concurrent.atomic.AtomicBoolean(false) }
    var isHoldingToSkip by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }

    // Fade geral da transição (1f = visível, 0f = tela preta pura antes do jogo)
    val fadeAnim = remember { Animatable(1f) }

    fun triggerFinishWithFade() {
        if (!isFinished.compareAndSet(false, true)) return
        coroutineScope.launch {
            // Fade suave de vídeo e áudio em 350ms (sem corte seco)
            launch {
                val mp = mediaPlayer
                if (mp != null) {
                    for (step in 10 downTo 0) {
                        val vol = step / 10f
                        try { mp.setVolume(vol, vol) } catch (_: Exception) {}
                        delay(25)
                    }
                }
            }
            fadeAnim.animateTo(0f, animationSpec = tween(350, easing = FastOutSlowInEasing))
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (_: Exception) {}
            onIntroFinished()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .alpha(fadeAnim.value)
            .pointerInput(Unit) {
                // Toque/clique contínuo na tela para pular com fade suave
                awaitEachGesture {
                    val down = awaitFirstDown()
                    isHoldingToSkip = true
                    val holdJob = coroutineScope.launch {
                        for (i in 1..20) {
                            holdProgress = i / 20f
                            delay(25) // ~500ms total para confirmação
                        }
                        triggerFinishWithFade()
                    }

                    // Aguarda soltar
                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { it.pressed })

                    isHoldingToSkip = false
                    holdProgress = 0f
                    holdJob.cancel()
                }
            }
    ) {
        // Preenche a tela toda conforme solicitado pelo usuário
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                TextureView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
                            try {
                                val surface = Surface(st)
                                val mp = MediaPlayer().apply {
                                    setSurface(surface)

                                    val customFile = IntroManager.getCustomFile(intro)
                                    if (customFile != null) {
                                        setDataSource(customFile.absolutePath)
                                    } else {
                                        val afd = IntroManager.getAssetFd(ctx, intro)
                                        if (afd != null) {
                                            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                            afd.close()
                                        }
                                    }

                                    setOnPreparedListener { player ->
                                        player.start()
                                    }
                                    setOnCompletionListener {
                                        triggerFinishWithFade()
                                    }
                                    setOnErrorListener { _, _, _ ->
                                        triggerFinishWithFade()
                                        true
                                    }
                                    prepareAsync()
                                }
                                mediaPlayer = mp
                            } catch (e: Exception) {
                                e.printStackTrace()
                                triggerFinishWithFade()
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) {}
                        override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                            try {
                                mediaPlayer?.stop()
                                mediaPlayer?.release()
                                mediaPlayer = null
                            } catch (_: Exception) {}
                            return true
                        }
                        override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
                    }
                }
            }
        )

        // Indicador discreto no canto inferior "Segure para Pular"
        AnimatedVisibility(
            visible = isHoldingToSkip,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x99000000))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                CircularProgressIndicator(
                    progress = { holdProgress },
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Segure para pular...",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
