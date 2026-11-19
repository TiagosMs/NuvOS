package com.decklauncher.data

import android.content.pm.ApplicationInfo
import android.os.Build
import com.decklauncher.model.AppCategory
import com.decklauncher.model.AppItem

enum class FolderCategory(val id: String, val title: String, val iconEmoji: String) {
    SYSTEM("system", "Sistema & Google", "⚙️"),
    MEDIA("media", "Mídia & Streaming", "🎬"),
    SOCIAL("social", "Comunicação & Social", "💬"),
    PRODUCTIVITY("productivity", "Produtividade & Notas", "📝"),
    TOOLS("tools", "Ferramentas & Web", "🛠️"),
    CREATIVE("creative", "Criatividade & Arte", "🎨"),
    OTHERS("others", "Outros Aplicativos", "📦")
}

data class AppFolder(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val apps: List<AppItem>
)

object CategoryManager {

    fun categorizeApp(app: AppItem, appInfo: ApplicationInfo?): FolderCategory {
        val pkg = app.packageName.lowercase()
        val label = app.label.lowercase()

        // 1. Mídia & Streaming
        if (pkg.contains("spotify") || pkg.contains("stremio") || pkg.contains("youtube")
            || pkg.contains("netflix") || pkg.contains("twitch") || pkg.contains("music")
            || pkg.contains("audio") || pkg.contains("wavelet") || pkg.contains("vlc")
            || pkg.contains("mxplayer") || pkg.contains("primevideo") || label.contains("spotify")
            || label.contains("stremio") || label.contains("youtube") || label.contains("música")
        ) {
            return FolderCategory.MEDIA
        }

        // 2. Social & Comunicação
        if (pkg.contains("whatsapp") || pkg.contains("discord") || pkg.contains("reddit")
            || pkg.contains("instagram") || pkg.contains("telegram") || pkg.contains("facebook")
            || pkg.contains("twitter") || pkg.contains("tiktok") || pkg.contains("pinterest")
            || pkg.contains("wemesh") || label.contains("whatsapp") || label.contains("discord")
            || label.contains("reddit") || label.contains("instagram")
        ) {
            return FolderCategory.SOCIAL
        }

        // 3. Criatividade & Arte
        if (pkg.contains("ibispaint") || pkg.contains("picsart") || pkg.contains("canva")
            || pkg.contains("sketch") || pkg.contains("draw") || pkg.contains("photo")
            || label.contains("paint") || label.contains("desenho") || label.contains("fotos")
        ) {
            return FolderCategory.CREATIVE
        }

        // 4. Produtividade & Notas
        if (pkg.contains("docs") || pkg.contains("notion") || pkg.contains("moffice")
            || pkg.contains("notes") || pkg.contains("sheets") || pkg.contains("deepseek")
            || pkg.contains("chatgpt") || pkg.contains("drive") || label.contains("bloco de notas")
            || label.contains("documentos") || label.contains("notion") || label.contains("deepseek")
        ) {
            return FolderCategory.PRODUCTIVITY
        }

        // 5. Sistema & Samsung & Google
        val isSamsungCustom = pkg.contains("samsung") || pkg.contains("homestar") || pkg.contains("lockstar")
            || pkg.contains("goodlock") || pkg.contains("clockface")
        val isGoogleSys = pkg.contains("inputmethod.latin") || pkg.contains("contactkeys") || pkg.contains("ar.lens")
            || pkg.contains("android.apps.messaging") || pkg.contains("google.android.dialer")
        val isSysSettings = pkg.contains("settings") || pkg.contains("systemui")

        if (isSamsungCustom || isGoogleSys || isSysSettings) {
            return FolderCategory.SYSTEM
        }

        // 6. Ferramentas & Web
        if (pkg.contains("brave") || pkg.contains("chrome") || pkg.contains("firefox")
            || pkg.contains("browser") || pkg.contains("calculator") || pkg.contains("clock")
            || pkg.contains("tailscale") || pkg.contains("cloudflare") || pkg.contains("etchdroid")
            || pkg.contains("kdeconnect") || pkg.contains("universalcopy") || pkg.contains("muambator")
            || pkg.contains("moodle") || pkg.contains("aliexpress") || pkg.contains("mercadolibre")
            || pkg.contains("schibsted") || pkg.contains("airbnb") || pkg.contains("jmbus")
            || pkg.contains("cooljazz") || pkg.contains("networkstorage") || pkg.contains("readera")
        ) {
            return FolderCategory.TOOLS
        }

        // Fallback por categoria Android oficial se disponível
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appInfo != null) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_AUDIO, ApplicationInfo.CATEGORY_VIDEO -> return FolderCategory.MEDIA
                ApplicationInfo.CATEGORY_SOCIAL -> return FolderCategory.SOCIAL
                ApplicationInfo.CATEGORY_IMAGE -> return FolderCategory.CREATIVE
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return FolderCategory.PRODUCTIVITY
                ApplicationInfo.CATEGORY_MAPS, ApplicationInfo.CATEGORY_NEWS -> return FolderCategory.TOOLS
            }
        }

        return FolderCategory.OTHERS
    }

    fun groupIntoFolders(apps: List<AppItem>): List<AppFolder> {
        val appList = apps.filter { it.category == AppCategory.APPS }
        if (appList.isEmpty()) return emptyList()

        val grouped = appList.groupBy { it.folderCategory ?: FolderCategory.OTHERS }

        val folders = mutableListOf<AppFolder>()

        for (cat in FolderCategory.values()) {
            val items = grouped[cat] ?: emptyList()
            if (items.isNotEmpty()) {
                folders.add(
                    AppFolder(
                        id = cat.id,
                        title = cat.title,
                        iconEmoji = cat.iconEmoji,
                        apps = items
                    )
                )
            }
        }

        return folders
    }
}
