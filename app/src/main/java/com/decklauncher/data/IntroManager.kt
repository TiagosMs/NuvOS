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
    private const val KEY_DOLPHIN_TOGGLE = "dolphin_toggle_wii"

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

    fun isEmulator(packageName: String, label: String): Boolean {
        val p = packageName.lowercase()
        val l = label.lowercase()
        return p.contains("retroarch") || p.contains("dolphin") || p.contains("ppsspp")
            || p.contains("aether") || p.contains("nether") || p.contains("yuzu")
            || p.contains("citra") || p.contains("vita3k") || p.contains("duckstation")
            || p.contains("mupen") || p.contains("snes9x") || p.contains("myboy")
            || p.contains("drastic") || p.contains("lemuroid") || p.contains("skyline")
            || p.contains("redream") || p.contains("flycast") || p.contains("melonds")
            || p.contains("hakux") || p.contains("haku_x") || p.contains("gamehub")
            || p.contains("eden") || p.contains("cemu") || p.contains("azahar")
            || p.contains("rpcsx") || p.contains("rpcs3") || p.contains("winlator")
            || p.contains("mobox") || p.contains("cassia") || p.contains("horizon")
            || p.contains("suyu") || p.contains("sudachi") || p.contains("lime3ds")
            || p.contains("folium") || p.contains("mandarine") || p.contains("strato")
            || p.contains("ryujinx") || p.contains("xemu") || p.contains("play!")
            || p.contains("epsxe") || p.contains("fpse") || p.contains("reicast")
            || p.contains("scummvm") || p.contains("dosbox") || p.contains("m64plus")
            || p.contains("superretro") || p.contains("classicboy")
            || l.contains("emulator") || l.contains("emulador")
            || l.contains("hakux") || l.contains("haku_x") || l.contains("dolphin")
            || l.contains("duckstation") || l.contains("ppsspp") || l.contains("melonds")
            || l.contains("drastic") || l.contains("aethersx2") || l.contains("nethersx2")
            || l.contains("citra") || l.contains("azahar") || l.contains("cemu")
            || l.contains("winlator") || l.contains("rpcsx") || l.contains("retroarch")
            || l.contains("eden") || l.contains("vita3k")
    }

    private fun getDolphinIntro(context: Context, isLaunching: Boolean): ConsoleIntro? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastWasWii = prefs.getBoolean(KEY_DOLPHIN_TOGGLE, false)
        val nextIsWii = if (isLaunching) {
            val toggled = !lastWasWii
            prefs.edit().putBoolean(KEY_DOLPHIN_TOGGLE, toggled).apply()
            toggled
        } else {
            !lastWasWii
        }
        return if (nextIsWii) {
            ALL_INTROS.firstOrNull { it.id == "wii" }
        } else {
            ALL_INTROS.firstOrNull { it.id == "gamecube" }
        }
    }

    fun detectEmulatorIntro(context: Context, app: AppItem, isLaunching: Boolean = false): ConsoleIntro? {
        val pkg = app.packageName.lowercase()
        val label = app.label.lowercase()

        // 1. Dolphin: Alterna 1 vez Wii, 1 vez GameCube
        if (pkg.contains("dolphin") || label.contains("dolphin")) {
            return getDolphinIntro(context, isLaunching)
        }

        // 2. Nintendo 3DS: AzaharPlus, Citra, Lime3DS, Mandarine, Folium
        if (pkg.contains("azahar") || pkg.contains("citra") || pkg.contains("lime3ds")
            || pkg.contains("mandarine") || pkg.contains("folium")
            || label.contains("3ds") || label.contains("azahar") || label.contains("citra")
        ) {
            return ALL_INTROS.firstOrNull { it.id == "3ds" }
        }

        // 3. Nintendo Wii U: Cemu
        if (pkg.contains("cemu") || label.contains("cemu") || label.contains("wii u") || label.contains("wiiu")) {
            return ALL_INTROS.firstOrNull { it.id == "wiiu" }
        }

        // 4. PSP: PPSSPP, Vita3K
        if (pkg.contains("ppsspp") || label.contains("ppsspp") || label.contains("psp") || pkg.contains("vita3k") || label.contains("vita")) {
            return ALL_INTROS.firstOrNull { it.id == "psp" }
        }

        // 5. Nintendo DS: melonDS, DraStic, DeSmuME
        if (pkg.contains("melonds") || pkg.contains("drastic") || pkg.contains("desmume") || label.contains("nds") || label.contains("ds")) {
            return ALL_INTROS.firstOrNull { it.id == "nds" }
        }

        // 6. PlayStation 2: NetherSX2, AetherSX2, Play!
        if (pkg.contains("aether") || pkg.contains("nether") || pkg.contains("play!") || label.contains("ps2") || label.contains("aethersx2") || label.contains("nethersx2")) {
            return ALL_INTROS.firstOrNull { it.id == "ps2" }
        }

        // 7. PlayStation 1: DuckStation, ePSXe, FPse, PCSX
        if (pkg.contains("duckstation") || pkg.contains("epsxe") || pkg.contains("fpse") || label.contains("ps1") || label.contains("psx")) {
            return ALL_INTROS.firstOrNull { it.id == "ps1" }
        }

        // 8. Xbox Clássico: haku_x, xemu
        if (pkg.contains("haku_x") || pkg.contains("hakux") || pkg.contains("xemu") || label.contains("xbox") || label.contains("hakux") || label.contains("haku_x")) {
            return ALL_INTROS.firstOrNull { it.id == "xbox_classic" }
        }

        // 9. Sega Dreamcast: Flycast, Redream, Reicast
        if (pkg.contains("flycast") || pkg.contains("redream") || pkg.contains("reicast") || label.contains("dreamcast")) {
            return ALL_INTROS.firstOrNull { it.id == "dreamcast" }
        }

        // 10. PC / Windows: Winlator, Mobox, Cassia, Horizon -> Xbox 360
        if (pkg.contains("winlator") || pkg.contains("mobox") || pkg.contains("cassia") || pkg.contains("horizon") || label.contains("winlator")) {
            return ALL_INTROS.firstOrNull { it.id == "xbox_360" }
        }

        // 11. PS3: RPCSX
        if (pkg.contains("rpcsx") || pkg.contains("rpcs3") || label.contains("rpcsx") || label.contains("ps3")) {
            return ALL_INTROS.firstOrNull { it.id == "ps2" }
        }

        // 12. Nintendo Switch: Eden, Yuzu, Suyu, Sudachi, Skyline, Ryujinx, Strato
        if (pkg.contains("eden") || pkg.contains("yuzu") || pkg.contains("suyu") || pkg.contains("sudachi")
            || pkg.contains("skyline") || pkg.contains("ryujinx") || pkg.contains("strato")
            || label.contains("switch") || label.contains("eden")
        ) {
            return ALL_INTROS.firstOrNull { it.id == "switch" }
        }

        // 13. RetroArch / Lemuroid
        if (pkg.contains("retroarch") || pkg.contains("lemuroid") || label.contains("retroarch")) {
            return ALL_INTROS.firstOrNull { it.id == "switch" }
        }

        return ALL_INTROS.firstOrNull { it.id == "switch" }
    }

    fun detectIntroForApp(context: Context, app: AppItem, isLaunching: Boolean = false): ConsoleIntro? {
        if (!isIntroEnabled(context)) return null

        // 1. Preferência manual específica salva pelo usuário para este jogo/app
        val customId = getCustomIntroId(context, app.id)
        if (customId != null) {
            if (customId == "none") return null
            return ALL_INTROS.firstOrNull { it.id == customId }
        }

        // 2. Detecção de Aplicativo Emulador (independente da aba em que estiver)
        if (app.itemType == ItemType.APP && isEmulator(app.packageName, app.label)) {
            return detectEmulatorIntro(context, app, isLaunching)
        }

        // 3. Apps normais utilitários (Aba Apps) não disparam intro por padrão
        if (app.category == AppCategory.APPS) {
            return null
        }

        // 4. Jogos em Nuvem (Better xCloud) -> Xbox Series
        if (app.itemType == ItemType.CLOUD) {
            return ALL_INTROS.firstOrNull { it.id == "xbox_series" }
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
                "wua", "rpx" -> return ALL_INTROS.firstOrNull { it.id == "wiiu" }
                "nsp", "xci" -> return ALL_INTROS.firstOrNull { it.id == "switch" }
                "iso", "cso" -> {
                    // Diferencia PSP, PS2, GameCube ou Wii
                    val targetPkg = app.packageName.lowercase()
                    if (targetPkg.contains("aethersx2") || targetPkg.contains("nether") || path.contains("ps2")) {
                        return ALL_INTROS.firstOrNull { it.id == "ps2" }
                    }
                    if (targetPkg.contains("dolphin") || path.contains("gamecube")) {
                        return ALL_INTROS.firstOrNull { it.id == "gamecube" }
                    }
                    if (path.contains("wii")) {
                        return ALL_INTROS.firstOrNull { it.id == "wii" }
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
