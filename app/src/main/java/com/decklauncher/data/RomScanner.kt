package com.decklauncher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import com.decklauncher.model.AppCategory
import com.decklauncher.model.AppItem
import com.decklauncher.model.ItemType
import java.io.File

object RomScanner {

    fun cleanRomTitle(fileName: String): String = RomSystemDetector.cleanRomTitle(fileName)

    fun scanRoms(context: Context): List<AppItem> {
        val results = mutableListOf<AppItem>()

        val searchDirs = mutableListOf<File>()

        // 1. Pasta personalizada configurada pelo usuário no Setup inicial
        val customUri = SetupManager.getRomFolderUri(context)
        if (customUri != null) {
            val customFile = SetupManager.getPhysicalFileForTreeUri(customUri)
            if (customFile != null && customFile.exists() && customFile.isDirectory) {
                searchDirs.add(customFile)
            }
        }

        // 2. Diretórios padrão de busca
        searchDirs.addAll(
            listOf(
                File(Environment.getExternalStorageDirectory(), "Download/MDS"),
                File(Environment.getExternalStorageDirectory(), "Download"),
                File(Environment.getExternalStorageDirectory(), "ROMs"),
                File(Environment.getExternalStorageDirectory(), "Roms"),
                File(Environment.getExternalStorageDirectory(), "Games"),
                File(Environment.getExternalStorageDirectory(), "ISOs"),
                File(Environment.getExternalStorageDirectory(), "RetroArch/downloads")
            )
        )

        val foundFiles = mutableListOf<File>()
        for (dir in searchDirs.distinctBy { it.absolutePath }) {
            if (dir.exists() && dir.isDirectory) {
                try {
                    dir.walkTopDown().maxDepth(3).forEach { file ->
                        if (file.isFile && RomSystemDetector.ALL_SUPPORTED_EXTENSIONS.contains(file.extension.lowercase())) {
                            foundFiles.add(file)
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        val emuConfigs = RomSystemDetector.loadEmulatorConfigs(context)

        for (file in foundFiles.distinctBy { it.absolutePath }) {
            val title = cleanRomTitle(file.name)
            val ext = file.extension.lowercase()
            val system = RomSystemDetector.detectSystem(file)
            val id = "rom_" + file.name.replace(Regex("[^a-zA-Z0-9._-]"), "_")

            val targetPkg = detectEmulatorForRom(context.packageManager, system, ext, emuConfigs)

            results.add(
                AppItem(
                    id = id,
                    label = title,
                    packageName = targetPkg ?: "emulator.$ext",
                    category = AppCategory.EMULADORES,
                    itemType = ItemType.ROM,
                    romPath = file.absolutePath
                )
            )
        }

        return results
    }

    private fun detectEmulatorForRom(
        pm: PackageManager,
        system: String,
        ext: String,
        configs: List<EmulatorConfig>
    ): String? {
        // 1. Prioriza por sistema detectado
        for (cfg in configs) {
            if (cfg.systems.contains(system)) {
                try {
                    pm.getPackageInfo(cfg.packageName, 0)
                    return cfg.packageName
                } catch (_: Exception) {}
            }
        }

        // 2. Fallback por extensão
        for (cfg in configs) {
            if (cfg.extensions.contains(ext)) {
                try {
                    pm.getPackageInfo(cfg.packageName, 0)
                    return cfg.packageName
                } catch (_: Exception) {}
            }
        }
        return null
    }

    fun getRomsForEmulator(emulatorPkg: String, allRoms: List<AppItem>): List<AppItem> {
        val pkgLower = emulatorPkg.lowercase()
        return allRoms.filter { rom ->
            val romPkg = rom.packageName.lowercase()
            val ext = rom.romPath?.substringAfterLast('.', "")?.lowercase() ?: ""
            when {
                romPkg == pkgLower -> true
                (pkgLower.contains("melonds") || pkgLower.contains("drastic") || pkgLower.contains("dsemu")) &&
                    (ext == "nds" || romPkg.contains("melonds") || romPkg.contains("drastic")) -> true
                pkgLower.contains("ppsspp") && (ext in listOf("iso", "cso", "pbp") || romPkg.contains("ppsspp")) -> true
                pkgLower.contains("dolphin") && (ext in listOf("iso", "rvz", "gcm", "wbfs", "ciso") || romPkg.contains("dolphin")) -> true
                (pkgLower.contains("aethersx2") || pkgLower.contains("nethersx2")) &&
                    (ext in listOf("iso", "chd", "bin", "cso") || romPkg.contains("aether") || romPkg.contains("nether")) -> true
                pkgLower.contains("citra") && (ext in listOf("3ds", "cia", "cxi") || romPkg.contains("citra")) -> true
                pkgLower.contains("snes") && (ext in listOf("sfc", "smc")) -> true
                (pkgLower.contains("gba") || pkgLower.contains("myboy")) && (ext in listOf("gba", "gb", "gbc")) -> true
                pkgLower.contains("retroarch") -> true
                else -> false
            }
        }
    }

    fun launchRom(context: Context, item: AppItem, options: android.os.Bundle? = null): Boolean {
        val path = item.romPath ?: return false
        val file = File(path)
        if (!file.exists()) return false

        val uri = Uri.fromFile(file)
        val ext = file.extension.lowercase()
        val configs = RomSystemDetector.loadEmulatorConfigs(context)
        val config = configs.firstOrNull { it.packageName == item.packageName }

        try {
            // Specialized intents for known emulators
            if (item.packageName == "com.rfandango.haku_x") {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setClassName("com.rfandango.haku_x", "com.rfandango.haku_x.LauncherActivity")
                    setDataAndType(uri, "*/*")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                if (options != null) context.startActivity(intent, options) else context.startActivity(intent)
                return true
            }

            if (item.packageName.contains("ppsspp")) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setPackage(item.packageName)
                    setDataAndType(uri, "application/octet-stream")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (options != null) context.startActivity(intent, options) else context.startActivity(intent)
                return true
            }

            if (item.packageName.contains("aethersx2") || item.packageName.contains("nether")) {
                val intent = Intent().apply {
                    setPackage(item.packageName)
                    putExtra("bootPath", file.absolutePath)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (options != null) context.startActivity(intent, options) else context.startActivity(intent)
                return true
            }

            // Usando configuração declarativa se disponível
            if (config != null) {
                val act = config.action ?: Intent.ACTION_VIEW
                val intent = Intent(act).apply {
                    setPackage(config.packageName)
                    if (config.component != null) {
                        val parts = config.component.split('/')
                        if (parts.size == 2) {
                            component = ComponentName(parts[0], parts[1])
                        }
                    }
                    val mime = config.mimeType ?: "*/*"
                    setDataAndType(uri, mime)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                if (options != null) context.startActivity(intent, options) else context.startActivity(intent)
                return true
            }

            // General ACTION_VIEW with fallback
            val intent = Intent(Intent.ACTION_VIEW).apply {
                if (item.packageName.startsWith("com.") || item.packageName.startsWith("org.") || item.packageName.startsWith("info.") || item.packageName.startsWith("io.") || item.packageName.startsWith("net.")) {
                    setPackage(item.packageName)
                }
                setDataAndType(uri, "*/*")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            if (options != null) context.startActivity(intent, options) else context.startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}

