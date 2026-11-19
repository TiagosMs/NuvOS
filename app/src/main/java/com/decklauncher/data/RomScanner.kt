package com.decklauncher.data

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

    private val ROM_EXTENSIONS = setOf("nds", "iso", "cso", "xbe", "gba", "gb", "gbc", "sfc", "smc", "n64", "z64", "chd")

    fun cleanRomTitle(fileName: String): String {
        var name = fileName.substringBeforeLast(".")
        // Remove release numbers like "3588 - "
        name = name.replace(Regex("^[0-9]+\\s*-\\s*"), "")
        // Remove region / dump tags like (USA), (US), [!], (XenoPhobia)
        name = name.replace(Regex("\\([^)]*\\)"), "")
        name = name.replace(Regex("\\[[^]]*\\]"), "")
        return name.trim()
    }

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
                        if (file.isFile && ROM_EXTENSIONS.contains(file.extension.lowercase())) {
                            foundFiles.add(file)
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        for (file in foundFiles.distinctBy { it.absolutePath }) {
            val title = cleanRomTitle(file.name)
            val ext = file.extension.lowercase()
            val id = "rom_" + file.name.replace(Regex("[^a-zA-Z0-9._-]"), "_")

            val targetPkg = detectEmulatorForExtension(context.packageManager, ext)

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

    private fun detectEmulatorForExtension(pm: PackageManager, ext: String): String? {
        val candidates = when (ext) {
            "nds" -> listOf("me.magnum.melonds", "com.dsemu.drastic", "com.retroarch", "com.retroarch.aarch64")
            "xbe" -> listOf("com.rfandango.haku_x")
            "iso", "cso" -> listOf("org.ppsspp.ppsspp", "xyz.aethersx2.android", "com.rfandango.haku_x", "org.dolphinemu.dolphinemu")
            "gba", "gb", "gbc" -> listOf("com.fastemulator.gba", "com.retroarch", "com.retroarch.aarch64")
            "sfc", "smc" -> listOf("com.explusalpha.Snes9xPlus", "com.retroarch")
            else -> listOf("com.retroarch", "com.retroarch.aarch64")
        }

        for (pkg in candidates) {
            try {
                pm.getPackageInfo(pkg, 0)
                return pkg
            } catch (_: Exception) {}
        }
        return null
    }

    fun launchRom(context: Context, item: AppItem): Boolean {
        val path = item.romPath ?: return false
        val file = File(path)
        if (!file.exists()) return false

        val uri = Uri.fromFile(file)
        val ext = file.extension.lowercase()

        try {
            // Specialized intents for known emulators
            if (item.packageName == "com.rfandango.haku_x") {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setClassName("com.rfandango.haku_x", "com.rfandango.haku_x.LauncherActivity")
                    setDataAndType(uri, "*/*")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
                return true
            }

            if (item.packageName.contains("ppsspp")) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setPackage(item.packageName)
                    setDataAndType(uri, "application/octet-stream")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return true
            }

            if (item.packageName.contains("aethersx2")) {
                val intent = Intent().apply {
                    setPackage(item.packageName)
                    putExtra("bootPath", file.absolutePath)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return true
            }

            // General ACTION_VIEW with fallback
            val intent = Intent(Intent.ACTION_VIEW).apply {
                if (item.packageName.startsWith("com.") || item.packageName.startsWith("org.")) {
                    setPackage(item.packageName)
                }
                setDataAndType(uri, "*/*")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
