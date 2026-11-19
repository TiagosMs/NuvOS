package com.decklauncher.data

import android.content.Context
import android.content.res.AssetFileDescriptor
import com.decklauncher.model.AppCategory
import com.decklauncher.model.AppItem
import com.decklauncher.model.ItemType
import java.io.File

data class ConsoleIntro(
    val id: String,
    val displayName: String,
    val assetFileName: String?
)

object IntroManager {

    private const val PREFS_NAME = "deck_intro_prefs"
    private const val KEY_INTRO_ENABLED = "intros_enabled"
    private const val KEY_CUSTOM_PREFIX = "custom_intro_"

    val ALL_INTROS = listOf(
        ConsoleIntro("nds", "Nintendo DS", "nds.mp4"),
        ConsoleIntro("3ds", "Nintendo 3DS", "3ds.mp4"),
        ConsoleIntro("switch", "Nintendo Switch", "switch.mp4"),
        ConsoleIntro("psp", "PlayStation Portable (PSP)", "psp.mp4"),
        ConsoleIntro("ps1", "PlayStation 1", "ps1.mp4"),
        ConsoleIntro("ps2", "PlayStation 2", "ps2.mp4"),
        ConsoleIntro("xbox_series", "Xbox Series X|S", "xbox_series.mp4"),
        ConsoleIntro("xbox_360", "Xbox 360", "xbox_360.mp4"),
        ConsoleIntro("xbox_classic", "Xbox Clássico", "xbox_classic.mp4"),
        ConsoleIntro("gamecube", "Nintendo GameCube", "gamecube.mp4"),
        ConsoleIntro("wii", "Nintendo Wii", "wii.mp4"),
        ConsoleIntro("wiiu", "Nintendo Wii U", "wiiu.mp4"),
        ConsoleIntro("dreamcast", "Sega Dreamcast", "dreamcast.mp4"),
        ConsoleIntro("none", "Nenhuma (Sem Intro)", null)
    )

    fun isIntroEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_INTRO_ENABLED, true)
    }

    fun setIntroEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_INTRO_ENABLED, enabled).apply()
    }

    fun getCustomIntroId(context: Context, gameId: String): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CUSTOM_PREFIX + gameId, null)
    }

    fun setCustomIntroId(context: Context, gameId: String, introId: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (introId == null) {
                remove(KEY_CUSTOM_PREFIX + gameId)
            } else {
                putString(KEY_CUSTOM_PREFIX + gameId, introId)
            }
            apply()
        }
    }

    fun detectIntroForApp(context: Context, app: AppItem): ConsoleIntro? {
        if (!isIntroEnabled(context)) return null

        // 1. Preferência manual específica salva pelo usuário para este jogo/app
        val customId = getCustomIntroId(context, app.id)
        if (customId != null) {
            if (customId == "none") return null
            return ALL_INTROS.firstOrNull { it.id == customId }
        }

        // 2. Apps normais utilitários (Aba Apps) não disparam intro por padrão
        if (app.category == AppCategory.APPS) {
            return null
        }

        // 3. Jogos em Nuvem (Better xCloud) -> Xbox Series
        if (app.itemType == ItemType.CLOUD) {
            return ALL_INTROS.firstOrNull { it.id == "xbox_series" }
        }

        // 4. Detecção por Emulador (quando abre o próprio aplicativo emulador na aba Emuladores)
        if (app.category == AppCategory.EMULADORES && app.itemType == ItemType.APP) {
            val pkg = app.packageName.lowercase()
            val label = app.label.lowercase()

            if (pkg.contains("ppsspp") || label.contains("ppsspp") || label.contains("psp")) {
                return ALL_INTROS.firstOrNull { it.id == "psp" }
            }
            if (pkg.contains("melonds") || pkg.contains("drastic") || label.contains("nds") || label.contains("ds")) {
                return ALL_INTROS.firstOrNull { it.id == "nds" }
            }
            if (pkg.contains("citra") || label.contains("3ds")) {
                return ALL_INTROS.firstOrNull { it.id == "3ds" }
            }
            if (pkg.contains("aethersx2") || pkg.contains("play!") || label.contains("ps2")) {
                return ALL_INTROS.firstOrNull { it.id == "ps2" }
            }
            if (pkg.contains("dolphin") || label.contains("dolphin") || label.contains("gamecube")) {
                return ALL_INTROS.firstOrNull { it.id == "gamecube" }
            }
            if (pkg.contains("haku_x") || label.contains("xbox")) {
                return ALL_INTROS.firstOrNull { it.id == "xbox_classic" }
            }
            if (pkg.contains("duckstation") || pkg.contains("epsxe") || label.contains("ps1") || label.contains("psx")) {
                return ALL_INTROS.firstOrNull { it.id == "ps1" }
            }
            if (pkg.contains("flycast") || pkg.contains("redream") || label.contains("dreamcast")) {
                return ALL_INTROS.firstOrNull { it.id == "dreamcast" }
            }
            if (pkg.contains("cemu") || label.contains("wii u") || label.contains("wiiu")) {
                return ALL_INTROS.firstOrNull { it.id == "wiiu" }
            }
            if (pkg.contains("yuzu") || pkg.contains("skyline") || label.contains("switch")) {
                return ALL_INTROS.firstOrNull { it.id == "switch" }
            }
            return ALL_INTROS.firstOrNull { it.id == "switch" }
        }

        // 5. Detecção por ROM (extensão de arquivo)
        if (app.itemType == ItemType.ROM) {
            val path = app.romPath?.lowercase() ?: ""
            val ext = path.substringAfterLast(".", "")

            when (ext) {
                "nds" -> return ALL_INTROS.firstOrNull { it.id == "nds" }
                "3ds" -> return ALL_INTROS.firstOrNull { it.id == "3ds" }
                "xbe" -> return ALL_INTROS.firstOrNull { it.id == "xbox_classic" }
                "cdi", "gdi", "chd" -> return ALL_INTROS.firstOrNull { it.id == "dreamcast" }
                "gcm" -> return ALL_INTROS.firstOrNull { it.id == "gamecube" }
                "wbfs" -> return ALL_INTROS.firstOrNull { it.id == "wii" }
                "iso", "cso" -> {
                    // Diferencia PSP de PS2 ou GameCube
                    val targetPkg = app.packageName.lowercase()
                    if (targetPkg.contains("aethersx2") || path.contains("ps2")) {
                        return ALL_INTROS.firstOrNull { it.id == "ps2" }
                    }
                    if (targetPkg.contains("dolphin") || path.contains("gamecube")) {
                        return ALL_INTROS.firstOrNull { it.id == "gamecube" }
                    }
                    return ALL_INTROS.firstOrNull { it.id == "psp" }
                }
                "bin", "cue", "pbp" -> return ALL_INTROS.firstOrNull { it.id == "ps1" }
                "gba", "gbc", "gb", "sfc", "smc", "nes", "n64", "z64" -> {
                    return ALL_INTROS.firstOrNull { it.id == "switch" }
                }
            }
        }

        // 6. Jogos Android nativos na aba Jogos -> Nintendo Switch
        if (app.category == AppCategory.JOGOS) {
            return ALL_INTROS.firstOrNull { it.id == "switch" }
        }

        return null
    }

    fun getAssetFd(context: Context, intro: ConsoleIntro): AssetFileDescriptor? {
        val fileName = intro.assetFileName ?: return null

        // 1. Permite override customizado pelo usuário no cartão se houver
        val customSd = File("/sdcard/Launcher/intros/$fileName")
        if (customSd.exists()) {
            return null // pode ser aberto via File
        }

        return try {
            context.assets.openFd("intros/$fileName")
        } catch (_: Exception) {
            null
        }
    }

    fun getCustomFile(intro: ConsoleIntro): File? {
        val fileName = intro.assetFileName ?: return null
        val customSd = File("/sdcard/Launcher/intros/$fileName")
        if (customSd.exists()) return customSd
        return null
    }
}
