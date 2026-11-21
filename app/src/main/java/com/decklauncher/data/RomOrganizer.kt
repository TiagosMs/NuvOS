package com.decklauncher.data

import java.io.File

data class RomMoveAction(
    val sourceFile: File,
    val targetDirectory: String,
    val targetFileName: String,
    val reason: String,
    val isConflict: Boolean = false,
    val isSkipped: Boolean = false
)

data class RomOrganizationPlan(
    val actions: List<RomMoveAction>,
    val movedCount: Int,
    val reviewCount: Int,
    val skippedCount: Int,
    val conflictCount: Int
)

object RomOrganizer {

    private val TEMP_EXTENSIONS = setOf("part", "crdownload", "tmp", "download")

    fun plan(
        files: List<File>,
        installedSystems: Set<String>,
        currentTimeMs: Long = System.currentTimeMillis()
    ): RomOrganizationPlan {
        val actions = mutableListOf<RomMoveAction>()
        val existingNamesByDir = mutableMapOf<String, MutableSet<String>>()

        // Agrupa arquivos associados (.cue + .bin)
        val cueFiles = files.filter { it.extension.lowercase() == "cue" }
        val cueAssociatedBins = mutableSetOf<String>()

        for (cue in cueFiles) {
            try {
                cue.forEachLine { line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("FILE", ignoreCase = true)) {
                        val match = Regex("FILE\\s+\"([^\"]+)\"").find(trimmed)
                        if (match != null) {
                            cueAssociatedBins.add(match.groupValues[1].lowercase())
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        for (file in files) {
            val name = file.name
            val ext = file.extension.lowercase()

            // 1. Sanitização: Não seguir links, pastas ou nomes com ".." ou separadores inválidos
            if (file.isDirectory || name.contains("..") || name.contains("/") || name.contains("\\")) {
                actions.add(RomMoveAction(file, "", name, "Diretório ou caminho inválido", isSkipped = true))
                continue
            }

            // 2. Arquivos temporários ou modificados há menos de 60 segundos (ainda baixando)
            if (TEMP_EXTENSIONS.contains(ext) || (currentTimeMs - file.lastModified()) < 60_000L) {
                actions.add(RomMoveAction(file, "", name, "Arquivo em download ou temporário", isSkipped = true))
                continue
            }

            // 3. Detecção de Sistema via RomSystemDetector
            val detectedSystem = RomSystemDetector.detectSystem(file)

            val targetDir = if (detectedSystem == "unknown" || detectedSystem == "desconhecido" || !installedSystems.contains(detectedSystem)) {
                if (detectedSystem != "unknown" && detectedSystem != "desconhecido") detectedSystem else "_Revisar"
            } else {
                detectedSystem
            }

            // Verifica colisão de nomes no diretório de destino
            val targetNames = existingNamesByDir.getOrPut(targetDir) { mutableSetOf() }
            var finalName = name
            var isConflict = false

            if (targetNames.contains(name.lowercase())) {
                isConflict = true
                val base = name.substringBeforeLast(".")
                val extPart = if (name.contains(".")) ".${name.substringAfterLast(".")}" else ""
                var counter = 2
                while (targetNames.contains("${base} ($counter)$extPart".lowercase())) {
                    counter++
                }
                finalName = "${base} ($counter)$extPart"
            }
            targetNames.add(finalName.lowercase())

            actions.add(
                RomMoveAction(
                    sourceFile = file,
                    targetDirectory = targetDir,
                    targetFileName = finalName,
                    reason = if (targetDir == "_Revisar") "Sistema não identificado" else "Sistema: $targetDir",
                    isConflict = isConflict
                )
            )
        }

        return RomOrganizationPlan(
            actions = actions,
            movedCount = actions.count { !it.isSkipped && it.targetDirectory != "_Revisar" },
            reviewCount = actions.count { it.targetDirectory == "_Revisar" },
            skippedCount = actions.count { it.isSkipped },
            conflictCount = actions.count { it.isConflict }
        )
    }

    // Execução segura: apenas move dentro do volume. Operação destrutiva auditada.
    fun executePlan(
        baseDir: File,
        plan: RomOrganizationPlan,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): List<Pair<File, File>> {
        val journal = mutableListOf<Pair<File, File>>()
        var completed = 0

        for (action in plan.actions) {
            if (action.isSkipped || action.targetDirectory.isBlank()) continue

            val destDir = File(baseDir, action.targetDirectory)
            if (!destDir.exists()) {
                destDir.mkdirs()
            }

            val targetFile = File(destDir, action.targetFileName)
            val success = action.sourceFile.renameTo(targetFile)
            if (success) {
                journal.add(Pair(action.sourceFile, targetFile))
            }
            completed++
            onProgress(completed, plan.actions.size)
        }

        return journal
    }

    // Reverte operações com base no diário
    fun undoJournal(journal: List<Pair<File, File>>): Int {
        var restored = 0
        for ((orig, current) in journal.reversed()) {
            if (current.exists() && !orig.exists()) {
                if (current.renameTo(orig)) {
                    restored++
                }
            }
        }
        return restored
    }
}
