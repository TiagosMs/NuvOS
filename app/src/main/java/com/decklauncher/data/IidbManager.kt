package com.decklauncher.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.collection.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class IidbParentCandidate(
    val id: Long,
    val name: String,
    val year: Int? = null,
    val developer: String? = null,
    val publisher: String? = null
)

data class IidbArtwork(
    val id: Long,
    val type: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val mimeType: String? = null,
    val durationMs: Int? = null,
    val displayName: String? = null
)

data class IidbGameAssets(
    val parentId: Long,
    val title: String,
    val boxartUrl: String? = null,
    val bannerUrl: String? = null,
    val logoUrl: String? = null,
    val iconUrl: String? = null,
    val soundbiteUrl: String? = null
)

object IidbManager {

    private const val PREFS_NAME = "iidb_prefs"
    private const val KEY_API = "iidb_api_key"
    private const val KEY_THEME_MUSIC_ENABLED = "theme_music_enabled"
    private const val BASE_URL = "https://iidb-api.iisu.network/api/v1"

    private val netGate = Semaphore(2)
    private val negativeCache = LruCache<String, Long>(256)
    private val NEGATIVE_CACHE_TTL_MS = 3L * 24 * 60 * 60 * 1000

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun getApiKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_API, "")?.trim() ?: ""
    }

    fun setApiKey(context: Context, apiKey: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (apiKey.isNullOrBlank()) {
                remove(KEY_API)
            } else {
                putString(KEY_API, apiKey.trim())
            }
            apply()
        }
    }

    fun hasApiKey(context: Context): Boolean = getApiKey(context).isNotBlank()

    fun isThemeMusicEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_THEME_MUSIC_ENABLED, true)
    }

    fun setThemeMusicEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_THEME_MUSIC_ENABLED, enabled).apply()
    }

    fun getSoundbiteDir(context: Context): File {
        val dir = File(context.filesDir, "soundbites")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getSoundbiteFile(context: Context, appId: String): File {
        return File(getSoundbiteDir(context), "${appId}.ogg")
    }

    suspend fun searchGames(context: Context, query: String): List<IidbParentCandidate> = withContext(Dispatchers.IO) {
        val key = getApiKey(context)
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank() || key.isBlank()) return@withContext emptyList()

        val cacheKey = "search_$cleanQuery"
        val lastFailed = negativeCache.get(cacheKey)
        if (lastFailed != null && System.currentTimeMillis() - lastFailed < NEGATIVE_CACHE_TTL_MS) {
            return@withContext emptyList()
        }

        netGate.withPermit {
            try {
                val enc = URLEncoder.encode(cleanQuery, "UTF-8").replace("+", "%20")
                val url = "$BASE_URL/search/parents?q=$enc&limit=15"
                val request = Request.Builder()
                    .url(url)
                    .header("X-API-Key", key)
                    .header("Accept", "application/json")
                    .header("User-Agent", "NuvOS-Launcher/1.0 (Android)")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: return@withContext emptyList()
                        val root = JSONObject(body)
                        val candidates = root.optJSONArray("candidates") ?: return@withContext emptyList()
                        val result = mutableListOf<IidbParentCandidate>()
                        for (i in 0 until candidates.length()) {
                            val obj = candidates.getJSONObject(i)
                            result.add(
                                IidbParentCandidate(
                                    id = obj.getLong("id"),
                                    name = obj.getString("name"),
                                    year = if (obj.has("year") && !obj.isNull("year")) obj.getInt("year") else null,
                                    developer = obj.optString("developer").takeIf { it.isNotBlank() },
                                    publisher = obj.optString("publisher").takeIf { it.isNotBlank() }
                                )
                            )
                        }
                        if (result.isEmpty()) negativeCache.put(cacheKey, System.currentTimeMillis())
                        return@withContext result
                    } else if (response.code == 404) {
                        negativeCache.put(cacheKey, System.currentTimeMillis())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            emptyList()
        }
    }

    suspend fun getArtworkForParent(context: Context, parentId: Long): IidbGameAssets? = withContext(Dispatchers.IO) {
        val key = getApiKey(context)
        if (key.isBlank()) return@withContext null

        val cacheKey = "parent_$parentId"
        val lastFailed = negativeCache.get(cacheKey)
        if (lastFailed != null && System.currentTimeMillis() - lastFailed < NEGATIVE_CACHE_TTL_MS) {
            return@withContext null
        }

        netGate.withPermit {
            try {
                val slots = "boxart,background,banner,hero,logo,icon,soundbite"
                val url = "$BASE_URL/assets/parents/$parentId?slots=$slots&candidates_per_slot=2&preferred_region=USA"
                val request = Request.Builder()
                    .url(url)
                    .header("X-API-Key", key)
                    .header("Accept", "application/json")
                    .header("User-Agent", "NuvOS-Launcher/1.0 (Android)")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: return@withContext null
                        val root = JSONObject(body)
                        val matchObj = root.optJSONObject("match")
                        val parentObj = matchObj?.optJSONObject("parent")
                        val name = parentObj?.optString("name") ?: root.optString("name", "Unknown")

                        val artwork = root.optJSONObject("artwork") ?: JSONObject()

                        val boxartArr = artwork.optJSONArray("boxart")
                        val bannerArr = artwork.optJSONArray("banner") ?: artwork.optJSONArray("background")
                        val heroArr = artwork.optJSONArray("hero")
                        val logoArr = artwork.optJSONArray("logo")
                        val iconArr = artwork.optJSONArray("icon")
                        val soundbiteArr = artwork.optJSONArray("soundbite")

                        val boxartUrl = boxartArr?.optJSONObject(0)?.optString("url")
                        val bannerUrl = bannerArr?.optJSONObject(0)?.optString("url") ?: heroArr?.optJSONObject(0)?.optString("url")
                        val logoUrl = logoArr?.optJSONObject(0)?.optString("url")
                        val iconUrl = iconArr?.optJSONObject(0)?.optString("url")
                        val soundbiteUrl = soundbiteArr?.optJSONObject(0)?.optString("url")

                        return@withContext IidbGameAssets(
                            parentId = parentId,
                            title = name,
                            boxartUrl = boxartUrl,
                            bannerUrl = bannerUrl,
                            logoUrl = logoUrl,
                            iconUrl = iconUrl,
                            soundbiteUrl = soundbiteUrl
                        )
                    } else if (response.code == 404) {
                        negativeCache.put(cacheKey, System.currentTimeMillis())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            null
        }
    }

    suspend fun getGalleryForParent(context: Context, parentId: Long, slot: String): List<IidbArtwork> = withContext(Dispatchers.IO) {
        val key = getApiKey(context)
        if (key.isBlank()) return@withContext emptyList()

        netGate.withPermit {
            try {
                val url = "$BASE_URL/assets/parents/$parentId/gallery?slot=$slot"
                val request = Request.Builder()
                    .url(url)
                    .header("X-API-Key", key)
                    .header("Accept", "application/json")
                    .header("User-Agent", "NuvOS-Launcher/1.0 (Android)")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string() ?: return@withContext emptyList()
                        val root = JSONObject(body)
                        val items = root.optJSONArray("items") ?: return@withContext emptyList()
                        val list = mutableListOf<IidbArtwork>()
                        for (i in 0 until items.length()) {
                            val obj = items.getJSONObject(i)
                            list.add(
                                IidbArtwork(
                                    id = obj.getLong("id"),
                                    type = obj.getString("type"),
                                    url = obj.getString("url"),
                                    thumbnailUrl = obj.optString("thumbnail_url").takeIf { it.isNotBlank() },
                                    mimeType = obj.optString("mime_type").takeIf { it.isNotBlank() },
                                    durationMs = if (obj.has("duration_ms") && !obj.isNull("duration_ms")) obj.getInt("duration_ms") else null,
                                    displayName = obj.optString("display_name").takeIf { it.isNotBlank() }
                                )
                            )
                        }
                        return@withContext list
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            emptyList()
        }
    }

    suspend fun downloadSoundbite(context: Context, appId: String, url: String): File? = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext null
        val target = getSoundbiteFile(context, appId)
        if (target.exists() && target.length() > 0) return@withContext target

        netGate.withPermit {
            try {
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", "NuvOS-Launcher/1.0 (Android)")
                    .build()

                client.newCall(req).execute().use { res ->
                    if (res.isSuccessful) {
                        val bytes = res.body?.bytes() ?: return@withContext null
                        FileOutputStream(target).use { it.write(bytes) }
                        return@withContext target
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            null
        }
    }

    suspend fun fetchAndApplyAllAssets(context: Context, appId: String, title: String): Boolean = withContext(Dispatchers.IO) {
        if (!hasApiKey(context) || title.isBlank()) return@withContext false

        val games = searchGames(context, title)
        val game = games.firstOrNull() ?: return@withContext false
        val assets = getArtworkForParent(context, game.id) ?: return@withContext false

        // 1. Box Art para Capa
        if (!assets.boxartUrl.isNullOrBlank()) {
            CoverManager.downloadCover(context, appId, assets.boxartUrl)
        }

        // 2. Banner para Imagem de Fundo (Hero + HeroBlur)
        if (!assets.bannerUrl.isNullOrBlank()) {
            downloadHeroInternal(context, appId, assets.bannerUrl)
        }

        // 3. Logo para Item 3D / Logo
        val logoToDownload = assets.logoUrl ?: assets.iconUrl
        if (!logoToDownload.isNullOrBlank()) {
            downloadLogoInternal(context, appId, logoToDownload)
        }

        // 4. Música tema / Soundbite
        if (!assets.soundbiteUrl.isNullOrBlank()) {
            downloadSoundbite(context, appId, assets.soundbiteUrl)
        }

        true
    }

    private fun downloadHeroInternal(context: Context, appId: String, url: String) {
        try {
            val req = Request.Builder().url(url).build()
            client.newCall(req).execute().use { res ->
                if (res.isSuccessful) {
                    val bytes = res.body?.bytes() ?: return
                    val raw = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return

                    val maxDim = 1280
                    val (w, h) = if (raw.width > maxDim || raw.height > maxDim) {
                        if (raw.width >= raw.height) {
                            maxDim to (raw.height * maxDim / raw.width)
                        } else {
                            (raw.width * maxDim / raw.height) to maxDim
                        }
                    } else raw.width to raw.height

                    val scaled = Bitmap.createScaledBitmap(raw, w, h, true)
                    val heroFile = HeroManager.getHeroFile(context, appId)
                    val fmt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Bitmap.CompressFormat.WEBP_LOSSY
                    } else {
                        @Suppress("DEPRECATION")
                        Bitmap.CompressFormat.JPEG
                    }
                    FileOutputStream(heroFile).use { scaled.compress(fmt, 82, it) }

                    // Gera hero_blur pré-renderizado em 1/16 do tamanho
                    val blurW = (w / 16).coerceAtLeast(32)
                    val blurH = (h / 16).coerceAtLeast(18)
                    val blurBmp = Bitmap.createScaledBitmap(scaled, blurW, blurH, true)
                    val blurFile = HeroManager.getHeroBlurFile(context, appId)
                    FileOutputStream(blurFile).use { blurBmp.compress(fmt, 70, it) }
                    blurBmp.recycle()
                    if (scaled != raw) scaled.recycle()
                    raw.recycle()
                }
            }
        } catch (_: Exception) {}
    }

    private fun downloadLogoInternal(context: Context, appId: String, url: String) {
        try {
            val req = Request.Builder().url(url).build()
            client.newCall(req).execute().use { res ->
                if (res.isSuccessful) {
                    val bytes = res.body?.bytes() ?: return
                    val raw = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return

                    val maxW = 880
                    val (w, h) = if (raw.width > maxW) {
                        maxW to (raw.height * maxW / raw.width)
                    } else raw.width to raw.height

                    val scaled = Bitmap.createScaledBitmap(raw, w, h, true)
                    val file = LogoManager.getLogoFile(context, appId)
                    val fmt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Bitmap.CompressFormat.WEBP_LOSSLESS
                    } else {
                        Bitmap.CompressFormat.PNG
                    }
                    FileOutputStream(file).use { scaled.compress(fmt, 100, it) }
                    if (scaled != raw) scaled.recycle()
                    raw.recycle()
                }
            }
        } catch (_: Exception) {}
    }
}
