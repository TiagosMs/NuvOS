package com.decklauncher.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.decklauncher.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object IconCache {
    private val cache = object : LruCache<String, ImageBitmap>(8 * 1024 * 1024) {
        override fun sizeOf(key: String, v: ImageBitmap): Int = v.width * v.height * 4
    }

    fun get(id: String): ImageBitmap? = cache.get(id)

    fun put(id: String, v: ImageBitmap) {
        cache.put(id, v)
    }

    fun clear() {
        cache.evictAll()
    }
}

fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        val b = drawable.bitmap
        if (b.width > 0 && b.height > 0) return b
    }
    val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun loadIcon(context: Context, app: AppItem): ImageBitmap? {
    val pm = context.packageManager
    val customIconDrawable = IconPackManager.getIcon(context, app.packageName, app.activityName)
    val drawable = customIconDrawable ?: try {
        pm.getApplicationIcon(app.packageName)
    } catch (_: Exception) {
        null
    }
    return drawable?.let { drawableToBitmap(it) }?.asImageBitmap()
}

@Composable
fun rememberAppIcon(app: AppItem?): ImageBitmap? {
    if (app == null) return null
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = IconCache.get(app.id), key1 = app.id) {
        if (value == null) {
            value = withContext(Dispatchers.IO) {
                loadIcon(context, app)?.also { IconCache.put(app.id, it) }
            }
        }
    }.value
}
