package com.decklauncher.data

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.decklauncher.R

object SoundEffectManager {

    private var soundPool: SoundPool? = null
    private var isLoaded = false

    private var soundNavMove = 0
    private var soundTabSwitch = 0
    private var soundNavBoundary = 0
    private var soundConfirm = 0
    private var soundCancelBack = 0
    private var soundMenuOpen = 0
    private var soundMenuClose = 0
    private var soundGameLaunch = 0

    fun init(context: Context) {
        if (soundPool != null) return

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val pool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(attrs)
            .build()

        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                isLoaded = true
            }
        }

        soundNavMove = pool.load(context, R.raw.sfx_nav_move, 1)
        soundTabSwitch = pool.load(context, R.raw.sfx_tab_switch, 1)
        soundNavBoundary = pool.load(context, R.raw.sfx_nav_boundary, 1)
        soundConfirm = pool.load(context, R.raw.sfx_confirm, 1)
        soundCancelBack = pool.load(context, R.raw.sfx_cancel_back, 1)
        soundMenuOpen = pool.load(context, R.raw.sfx_menu_open, 1)
        soundMenuClose = pool.load(context, R.raw.sfx_menu_close, 1)
        soundGameLaunch = pool.load(context, R.raw.sfx_game_launch, 1)

        soundPool = pool
    }

    private fun play(soundId: Int, volume: Float = 0.95f) {
        val pool = soundPool ?: return
        if (soundId != 0) {
            pool.play(soundId, volume, volume, 1, 0, 1.0f)
        }
    }

    fun playNavMove() = play(soundNavMove, 0.75f)
    fun playTabSwitch() = play(soundTabSwitch, 0.85f)
    fun playBoundary() = play(soundNavBoundary, 0.75f)
    fun playConfirm() = play(soundConfirm, 0.9f)
    fun playCancelBack() = play(soundCancelBack, 0.85f)
    fun playMenuOpen() = play(soundMenuOpen, 0.85f)
    fun playMenuClose() = play(soundMenuClose, 0.85f)
    fun playGameLaunch() = play(soundGameLaunch, 1.0f)

    fun release() {
        soundPool?.release()
        soundPool = null
        isLoaded = false
    }
}
