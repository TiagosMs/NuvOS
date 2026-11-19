package com.decklauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

data class InstalledIconPack(
    val packageName: String,
    val name: String,
    val isIconPackStudio: Boolean
)

object IconPackManager {

    private const val PREFS_NAME = "iconpack_prefs"
    private const val KEY_SELECTED_PACK = "selected_iconpack"
    const val DEFAULT_IPS_THEME_URL = "https://share.iconpackstudio.com/feed/d_dTcexQ9UlL5cpD"

    // Component or package -> drawable name
    private val iconCache = mutableMapOf<String, String>()
    private var loadedPackPackage: String? = null
    private var packResources: Resources? = null

    fun getSelectedPack(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SELECTED_PACK, null)
    }

    fun setSelectedPack(context: Context, packageName: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (packageName.isNullOrBlank()) {
                remove(KEY_SELECTED_PACK)
            } else {
                putString(KEY_SELECTED_PACK, packageName)
            }
            apply()
        }
        loadedPackPackage = null
        packResources = null
        iconCache.clear()
    }

    fun getInstalledIconPacks(context: Context): List<InstalledIconPack> {
        val pm = context.packageManager
        val actions = listOf(
            "org.adw.launcher.THEMES",
            "com.novalauncher.THEME",
            "com.gau.go.launcherex.theme",
            "com.fede.launcher.THEME_ICONPACK",
            "ginlemon.smartlauncher.iconpack"
        )

        val foundPackages = mutableMapOf<String, InstalledIconPack>()

        for (action in actions) {
            val intent = Intent(action)
            val list = pm.queryIntentActivities(intent, 0)
            for (res in list) {
                val pkg = res.activityInfo.packageName
                if (!foundPackages.containsKey(pkg)) {
                    val label = res.loadLabel(pm).toString()
                    val isIps = pkg.contains("ginlemon") || pkg.contains("iconpackstudio")
                    foundPackages[pkg] = InstalledIconPack(pkg, label, isIps)
                }
            }
        }

        // Também pesquisa pacotes instalados com nome característico de Icon Pack Studio
        val allPkgs = pm.getInstalledPackages(0)
        for (pkgInfo in allPkgs) {
            val pkg = pkgInfo.packageName
            if (pkg.contains("ginlemon.smartlauncher.iconpack") || pkg.contains("ginlemon.flowerpro")) {
                if (!foundPackages.containsKey(pkg)) {
                    val label = pkgInfo.applicationInfo?.loadLabel(pm)?.toString() ?: "iiSU style (Icon Pack)"
                    foundPackages[pkg] = InstalledIconPack(pkg, label, true)
                }
            }
        }

        return foundPackages.values.sortedByDescending { it.isIconPackStudio }
    }

    suspend fun loadIconPack(context: Context, forceReload: Boolean = false) = withContext(Dispatchers.IO) {
        var packPkg = getSelectedPack(context)

        // Se nenhum selecionado mas houver pacote do Icon Pack Studio instalado, seleciona-o automaticamente
        if (packPkg.isNullOrBlank()) {
            val installed = getInstalledIconPacks(context)
            val ipsPack = installed.firstOrNull { it.isIconPackStudio } ?: installed.firstOrNull()
            if (ipsPack != null) {
                packPkg = ipsPack.packageName
                setSelectedPack(context, packPkg)
            }
        }

        if (packPkg == null) {
            iconCache.clear()
            packResources = null
            loadedPackPackage = null
            return@withContext
        }

        if (!forceReload && packPkg == loadedPackPackage && packResources != null) {
            return@withContext
        }

        val pm = context.packageManager
        try {
            packResources = pm.getResourcesForApplication(packPkg)
            loadedPackPackage = packPkg
            iconCache.clear()

            // 1. Tenta carregar assets/appfilter.xml
            var inputStream: InputStream? = null
            try {
                val assetManager = context.createPackageContext(packPkg, Context.CONTEXT_IGNORE_SECURITY).assets
                inputStream = assetManager.open("appfilter.xml")
            } catch (_: Exception) {}

            // 2. Se não achou em assets, tenta res/xml/appfilter
            if (inputStream == null && packResources != null) {
                val xmlId = packResources!!.getIdentifier("appfilter", "xml", packPkg)
                if (xmlId != 0) {
                    val parser = packResources!!.getXml(xmlId)
                    parseXml(parser)
                    return@withContext
                }
            }

            if (inputStream != null) {
                inputStream.use { stream ->
                    val factory = XmlPullParserFactory.newInstance()
                    factory.isNamespaceAware = true
                    val parser = factory.newPullParser()
                    parser.setInput(stream, "utf-8")
                    parseXml(parser)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseXml(parser: XmlPullParser) {
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                val component = parser.getAttributeValue(null, "component")
                val drawable = parser.getAttributeValue(null, "drawable")
                if (!component.isNullOrBlank() && !drawable.isNullOrBlank()) {
                    // component geralmente no formato: ComponentInfo{com.whatsapp/com.whatsapp.Main}
                    val cleanComp = component
                        .replace("ComponentInfo{", "")
                        .replace("}", "")
                        .trim()
                    iconCache[cleanComp] = drawable

                    // Mapeia também apenas o pacote base caso a activity mude
                    val parts = cleanComp.split("/")
                    if (parts.isNotEmpty()) {
                        val pkgOnly = parts[0]
                        if (!iconCache.containsKey(pkgOnly)) {
                            iconCache[pkgOnly] = drawable
                        }
                    }
                }
            }
            eventType = parser.next()
        }
    }

    fun getIcon(context: Context, packageName: String, activityName: String?): Drawable? {
        val res = packResources ?: return null
        val packPkg = loadedPackPackage ?: return null

        val fullComp = if (!activityName.isNullOrBlank()) "$packageName/$activityName" else packageName
        val drawableName = iconCache[fullComp] ?: iconCache[packageName] ?: return null

        return try {
            val resId = res.getIdentifier(drawableName, "drawable", packPkg)
            if (resId != 0) {
                res.getDrawable(resId, null)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun openIconPackStudioLink(context: Context, themeUrl: String = DEFAULT_IPS_THEME_URL) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(themeUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
