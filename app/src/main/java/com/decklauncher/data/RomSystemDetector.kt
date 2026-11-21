package com.decklauncher.data

import android.content.Context
import org.json.JSONArray
import java.io.File
import java.io.RandomAccessFile

data class EmulatorConfig(
    val packageName: String,
    val name: String,
    val systems: List<String>,
    val extensions: List<String>,
    val intro: String?,
    val action: String? = null,
    val component: String? = null,
    val mimeType: String? = null,
    val dataPattern: String? = null,
    val bootPathExtra: String? = null,
    val uriExtra: String? = null
)

object RomSystemDetector {

    private val REGEX_RELEASE = Regex("^[0-9]+\\s*-\\s*")
    private val REGEX_PARENS = Regex("\\([^)]*\\)")
    private val REGEX_BRACKETS = Regex("\\[[^]]*\\]")

    val ALL_SUPPORTED_EXTENSIONS = setOf(
        "nds", "3ds", "cia", "cxi",
        "iso", "cso", "pbp", "chd", "cue", "bin",
        "xbe", "gcm", "rvz", "wbfs", "ciso",
        "gba", "gb", "gbc", "sfc", "smc",
        "n64", "z64", "v64", "vpk", "nsp", "xci", "cdi", "gdi"
    )

    fun cleanRomTitle(fileName: String): String {
        var name = fileName.substringBeforeLast(".")
        name = name.replace(REGEX_RELEASE, "")
        name = name.replace(REGEX_PARENS, "")
        name = name.replace(REGEX_BRACKETS, "")
        return name.trim()
    }

    fun detectSystem(file: File): String {
        val pathLower = file.absolutePath.lowercase()
        val ext = file.extension.lowercase()

        // 1. Detecção por pasta
        val folderSystem = detectSystemFromFolder(pathLower)
        if (folderSystem != null) return folderSystem

        // 2. Detecção por extensão não ambígua
        when (ext) {
            "nds" -> return "nds"
            "3ds", "cia", "cxi" -> return "3ds"
            "xbe" -> return "xbox"
            "gcm", "rvz" -> return "gamecube"
            "wbfs" -> return "wii"
            "pbp" -> return "psp"
            "gba" -> return "gba"
            "gb", "gbc" -> return "gbc"
            "sfc", "smc" -> return "snes"
            "n64", "z64", "v64" -> return "n64"
            "vpk" -> return "psvita"
            "nsp", "xci" -> return "switch"
            "cdi", "gdi" -> return "dreamcast"
        }

        // 3. Inspeção de cabeçalho para .iso, .bin, .cso, .chd
        if (ext in setOf("iso", "bin", "cso")) {
            val headerSystem = inspectHeader(file)
            if (headerSystem != null) return headerSystem
        }

        return "unknown"
    }

    private fun detectSystemFromFolder(path: String): String? {
        val segments = path.split('/', '\\')
        for (seg in segments) {
            when (seg) {
                "psp" -> return "psp"
                "ps2", "playstation2" -> return "ps2"
                "ps1", "psx", "playstation" -> return "ps1"
                "gc", "gamecube", "dolphin-gc" -> return "gamecube"
                "wii", "dolphin-wii" -> return "wii"
                "nds", "ds" -> return "nds"
                "3ds", "citra" -> return "3ds"
                "gba" -> return "gba"
                "gbc", "gb" -> return "gbc"
                "snes", "sfc" -> return "snes"
                "n64" -> return "n64"
                "dc", "dreamcast" -> return "dreamcast"
                "xbox", "xbox_classic" -> return "xbox"
                "psvita", "vita" -> return "psvita"
                "switch", "yuzu" -> return "switch"
            }
        }
        return null
    }

    private fun inspectHeader(file: File): String? {
        if (!file.exists() || !file.canRead()) return null
        return try {
            RandomAccessFile(file, "r").use { raf ->
                val len = raf.length()
                if (len < 512) return null

                // Check GameCube magic at 0x1C: 0xC2339F3D
                if (len >= 0x20) {
                    raf.seek(0x1C)
                    val b0 = raf.read()
                    val b1 = raf.read()
                    val b2 = raf.read()
                    val b3 = raf.read()
                    if (b0 == 0xC2 && b1 == 0x33 && b2 == 0x9F && b3 == 0x3D) {
                        return "gamecube"
                    }
                }

                // Check Wii magic at 0x18: 0x5D1C9EA3
                if (len >= 0x1C) {
                    raf.seek(0x18)
                    val b0 = raf.read()
                    val b1 = raf.read()
                    val b2 = raf.read()
                    val b3 = raf.read()
                    if (b0 == 0x5D && b1 == 0x1C && b2 == 0x9E && b3 == 0xA3) {
                        return "wii"
                    }
                }

                // Search first 256KB for PSP_GAME or SYSTEM.CNF
                val readLimit = minOf(len, 256 * 1024L).toInt()
                val buf = ByteArray(readLimit)
                raf.seek(0)
                raf.readFully(buf)
                val str = String(buf, Charsets.ISO_8859_1)

                when {
                    str.contains("PSP_GAME") -> "psp"
                    str.contains("BOOT2") || str.contains("SYSTEM.CNF") -> "ps2"
                    str.contains("PLAYSTATION") -> "ps1"
                    else -> null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private var cachedConfigs: List<EmulatorConfig>? = null

    fun loadEmulatorConfigs(context: Context): List<EmulatorConfig> {
        cachedConfigs?.let { return it }

        val jsonStr = try {
            // Check SD card override first (/sdcard/Launcher/emulators.json)
            val externalFile = File("/sdcard/Launcher/emulators.json")
            if (externalFile.exists() && externalFile.canRead()) {
                externalFile.readText()
            } else {
                context.assets.open("emulators.json").bufferedReader().use { it.readText() }
            }
        } catch (_: Exception) {
            "[]"
        }

        val list = mutableListOf<EmulatorConfig>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val systemsArr = obj.optJSONArray("systems")
                val systems = mutableListOf<String>()
                if (systemsArr != null) {
                    for (s in 0 until systemsArr.length()) systems.add(systemsArr.getString(s))
                }
                val extsArr = obj.optJSONArray("extensions")
                val exts = mutableListOf<String>()
                if (extsArr != null) {
                    for (e in 0 until extsArr.length()) exts.add(extsArr.getString(e).lowercase())
                }
                val launchObj = obj.optJSONObject("launch")
                list.add(
                    EmulatorConfig(
                        packageName = obj.getString("package"),
                        name = obj.getString("name"),
                        systems = systems,
                        extensions = exts,
                        intro = obj.optString("intro").takeIf { it.isNotBlank() },
                        action = launchObj?.optString("action"),
                        component = launchObj?.optString("component"),
                        mimeType = launchObj?.optString("type"),
                        dataPattern = launchObj?.optString("data"),
                        bootPathExtra = launchObj?.optString("bootPath"),
                        uriExtra = launchObj?.optString("uriExtra")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        cachedConfigs = list
        return list
    }
}
