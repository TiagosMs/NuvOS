package com.decklauncher.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import java.io.File

object SetupManager {

    private const val PREFS_NAME = "deck_setup_prefs"
    private const val KEY_SETUP_COMPLETED = "is_setup_completed"
    private const val KEY_SETUP_VERSION = "setup_version"
    private const val KEY_SETUP_STEP = "setup_current_step"
    private const val KEY_ROM_FOLDER_URI = "rom_folder_uri"
    private const val KEY_ROM_FOLDER_PATH = "rom_folder_path"
    private const val KEY_DOWNLOAD_FOLDER_URI = "download_folder_uri"
    private const val KEY_DOWNLOAD_FOLDER_PATH = "download_folder_path"

    const val CURRENT_SETUP_VERSION = 1

    fun isSetupCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SETUP_COMPLETED, false)
    }

    fun setSetupCompleted(context: Context, completed: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_SETUP_COMPLETED, completed)
            .putInt(KEY_SETUP_VERSION, CURRENT_SETUP_VERSION)
            .putInt(KEY_SETUP_STEP, 0)
            .apply()
    }

    fun getSavedSetupStep(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_SETUP_STEP, 0)
    }

    fun saveSetupStep(context: Context, step: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_SETUP_STEP, step).apply()
    }

    fun getRomFolderUri(context: Context): Uri? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val str = prefs.getString(KEY_ROM_FOLDER_URI, null) ?: return null
        return try { Uri.parse(str) } catch (_: Exception) { null }
    }

    fun getRomFolderName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ROM_FOLDER_PATH, null) ?: "Nenhuma selecionada (toque para escolher)"
    }

    fun setRomFolder(context: Context, uri: Uri) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (_: Exception) {}

        val readable = getHumanReadablePath(uri)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ROM_FOLDER_URI, uri.toString())
            .putString(KEY_ROM_FOLDER_PATH, readable)
            .apply()
    }

    fun getDownloadFolderUri(context: Context): Uri? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val str = prefs.getString(KEY_DOWNLOAD_FOLDER_URI, null) ?: return null
        return try { Uri.parse(str) } catch (_: Exception) { null }
    }

    fun getDownloadFolderName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_DOWNLOAD_FOLDER_PATH, null) ?: "Padrão (/sdcard/Download/Games)"
    }

    fun setDownloadFolder(context: Context, uri: Uri) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (_: Exception) {}

        val readable = getHumanReadablePath(uri)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_DOWNLOAD_FOLDER_URI, uri.toString())
            .putString(KEY_DOWNLOAD_FOLDER_PATH, readable)
            .apply()
    }

    fun getHumanReadablePath(uri: Uri): String {
        try {
            val docId = DocumentsContract.getTreeDocumentId(uri)
            if (docId != null && docId.contains(":")) {
                val parts = docId.split(":", limit = 2)
                val type = parts[0]
                val subPath = parts.getOrNull(1) ?: ""
                return if ("primary".equals(type, ignoreCase = true)) {
                    "/sdcard/$subPath".trimEnd('/')
                } else {
                    "/storage/$type/$subPath".trimEnd('/')
                }
            }
        } catch (_: Exception) {}
        return uri.lastPathSegment ?: uri.toString()
    }

    fun getPhysicalFileForTreeUri(uri: Uri): File? {
        val path = getHumanReadablePath(uri)
        if (path.startsWith("/sdcard/")) {
            val rel = path.removePrefix("/sdcard/")
            val f = File(Environment.getExternalStorageDirectory(), rel)
            if (f.exists()) return f
        }
        val direct = File(path)
        if (direct.exists()) return direct
        return null
    }
}
