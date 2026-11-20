package com.decklauncher.data

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ThemeMusicManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingAppId: String? = null
    private var latestRequestedAppId: String? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var fetchJob: Job? = null

    fun playTheme(context: Context, appId: String, label: String = "") {
        latestRequestedAppId = appId
        if (!IidbManager.isThemeMusicEnabled(context)) {
            stopTheme()
            return
        }

        if (currentPlayingAppId == appId && mediaPlayer?.isPlaying == true) {
            return
        }

        fetchJob?.cancel()
        val file = IidbManager.getSoundbiteFile(context, appId)

        if (file.exists() && file.length() > 0) {
            startPlayerWithFile(file.absolutePath, appId)
        } else {
            stopTheme()
            // Se tiver iiDB configurado e o arquivo ainda não existir, baixa em background
            if (IidbManager.hasApiKey(context) && label.isNotBlank()) {
                fetchJob = scope.launch {
                    val games = IidbManager.searchGames(context, label)
                    val game = games.firstOrNull() ?: return@launch
                    val assets = IidbManager.getArtworkForParent(context, game.id) ?: return@launch
                    val url = assets.soundbiteUrl ?: return@launch
                    val downloaded = IidbManager.downloadSoundbite(context, appId, url)
                    if (downloaded != null && downloaded.exists()) {
                        withContext(Dispatchers.Main) {
                            if (latestRequestedAppId == appId) {
                                startPlayerWithFile(downloaded.absolutePath, appId)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startPlayerWithFile(path: String, appId: String) {
        try {
            stopTheme()
            currentPlayingAppId = appId

            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(path)
                isLooping = true
                setVolume(0.70f, 0.70f)
                setOnPreparedListener { player ->
                    if (currentPlayingAppId == appId) {
                        player.start()
                    } else {
                        player.release()
                    }
                }
                setOnErrorListener { player, _, _ ->
                    player.reset()
                    player.release()
                    if (mediaPlayer === player) mediaPlayer = null
                    true
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            e.printStackTrace()
            stopTheme()
        }
    }

    fun stopTheme() {
        fetchJob?.cancel()
        fetchJob = null
        currentPlayingAppId = null
        latestRequestedAppId = null
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) mp.stop()
                mp.reset()
                mp.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    fun pauseTheme() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (_: Exception) {}
    }

    fun resumeTheme() {
        try {
            if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                mediaPlayer?.start()
            }
        } catch (_: Exception) {}
    }
}
