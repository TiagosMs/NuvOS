package com.decklauncher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
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

        // 2. Diretórios padrão de busca (normalizados por caminho canônico para evitar duplicações em sistemas case-insensitive)
        val defaultDirs = listOf(
            File(Environment.getExternalStorageDirectory(), "Rom"),
            File(Environment.getExternalStorageDirectory(), "ROM"),
            File(Environment.getExternalStorageDirectory(), "ROMs"),
            File(Environment.getExternalStorageDirectory(), "Roms"),
            File(Environment.getExternalStorageDirectory(), "roms"),
            File(Environment.getExternalStorageDirectory(), "Games"),
            File(Environment.getExternalStorageDirectory(), "games"),
            File(Environment.getExternalStorageDirectory(), "ISOs"),
            File(Environment.getExternalStorageDirectory(), "isos"),
            File(Environment.getExternalStorageDirectory(), "Download"),
            File(Environment.getExternalStorageDirectory(), "Download/MDS"),
            File(Environment.getExternalStorageDirectory(), "RetroArch/downloads")
        )
        searchDirs.addAll(defaultDirs)

        val uniqueSearchDirs = searchDirs.mapNotNull { dir ->
            try {
                if (dir.exists() && dir.isDirectory) dir.canonicalFile else null
            } catch (_: Exception) {
                if (dir.exists() && dir.isDirectory) dir else null
            }
        }.distinctBy { it.path }

        val foundFiles = mutableListOf<File>()
        for (dir in uniqueSearchDirs) {
            try {
                dir.walkTopDown().maxDepth(10).forEach { file ->
                    if (file.isFile && RomSystemDetector.ALL_SUPPORTED_EXTENSIONS.contains(file.extension.lowercase())) {
                        foundFiles.add(file)
                    }
                }
            } catch (_: Exception) {}
        }

        val emuConfigs = RomSystemDetector.loadEmulatorConfigs(context)

        // Deduplica arquivos pelo caminho canônico (ou path absoluto se canonical falhar)
        val uniqueFiles = foundFiles.mapNotNull { file ->
            try {
                file.canonicalFile
            } catch (_: Exception) {
                file
            }
        }.distinctBy { it.path }

        val processedIds = mutableSetOf<String>()

        for (file in uniqueFiles) {
            val title = cleanRomTitle(file.name)
            val ext = file.extension.lowercase()
            val system = RomSystemDetector.detectSystem(file)
            var id = "rom_" + file.name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            if (processedIds.contains(id)) {
                id = "${id}_${file.absolutePath.hashCode().toString(16)}"
            }
            processedIds.add(id)

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

    private fun buildSafUriForPackage(context: Context, targetPackage: String, file: File): Uri? {
        val relPath = file.absolutePath.removePrefix("/storage/emulated/0/").removePrefix("storage/emulated/0/")
        val docId = "primary:$relPath"
        val folderRel = file.parentFile?.absolutePath?.removePrefix("/storage/emulated/0/")?.removePrefix("storage/emulated/0/") ?: ""
        val treeId = if (folderRel.isNotBlank()) "primary:$folderRel" else "primary:Rom"
        return DocumentsContract.buildDocumentUriUsingTree(
            DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", treeId),
            docId
        )
    }

    fun launchRom(context: Context, item: AppItem, options: android.os.Bundle? = null): Boolean {
        android.util.Log.i("NuvOSLaunch", "launchRom: label=${item.label}, pkg=${item.packageName}, path=${item.romPath}")
        val path = item.romPath ?: return false
        val file = File(path)
        if (!file.exists()) {
            android.util.Log.e("NuvOSLaunch", "launchRom: file does not exist: $path")
            return false
        }

        val uri = Uri.fromFile(file) // rule-ok: excecao T2.4 retrocompatibilidade com emuladores sem suporte a SAF content URI
        val ext = file.extension.lowercase()
        val configs = RomSystemDetector.loadEmulatorConfigs(context)
        val config = configs.firstOrNull { it.packageName == item.packageName }
        android.util.Log.i("NuvOSLaunch", "launchRom: matched config=${config?.name}, uriExtra=${config?.uriExtra}")

        try {
            // Se houver configuração declarativa em emulators.json, constrói a Intent estritamente baseada nela
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
                    if (config.bootPathExtra != null) {
                        putExtra("bootPath", file.absolutePath)
                    }
                    if (config.uriExtra != null) {
                        // melonDS e emuladores SAF: constrói a tree URI canônica correspondente à permissão persistida do emulador
                        val safeUri = buildSafUriForPackage(context, config.packageName, file) ?: uri
                        android.util.Log.i("NuvOSLaunch", "launchRom: putting ${config.uriExtra} = $safeUri")
                        putExtra(config.uriExtra, safeUri.toString())
                    } else if (config.mimeType != null) {
                        setDataAndType(uri, config.mimeType)
                    } else if (config.dataPattern != null || act == Intent.ACTION_VIEW || (config.action != null && config.action != "me.magnum.melonds.LAUNCH_ROM")) {
                        data = uri
                    }
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                android.util.Log.i("NuvOSLaunch", "launchRom: intent=$intent, extras=${intent.extras}")
                if (options != null) context.startActivity(intent, options) else context.startActivity(intent)
                return true
            }

            // Fallback genérico com intent explícita
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setPackage(item.packageName)
                data = uri
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            android.util.Log.i("NuvOSLaunch", "launchRom fallback: intent=$intent")
            if (options != null) context.startActivity(intent, options) else context.startActivity(intent)
            return true
        } catch (e: Exception) {
            android.util.Log.e("NuvOSLaunch", "launchRom exception", e)
            e.printStackTrace()
            return false
        }
    }
}

