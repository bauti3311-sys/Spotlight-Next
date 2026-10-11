package dev.bautista.spotlight.index

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Locale

/** Read only metadata supplied by the selected image; no inferred creation dates. */
object ImageMetadata {
    data class Details(val orientation: Int = 1, val author: String = "", val created: Long = 0)

    fun read(context: Context, uri: Uri): Details =
        runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val exif = ExifInterface(input)
                    val timestamp = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    val created =
                        timestamp?.let {
                            runCatching {
                                    SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
                                        .apply { isLenient = false }
                                        .parse(it)
                                        ?.time ?: 0
                                }
                                .getOrDefault(0)
                        } ?: 0
                    Details(
                        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1),
                        exif.getAttribute(ExifInterface.TAG_ARTIST) ?: "",
                        created,
                    )
                } ?: Details()
            }
            .getOrDefault(Details())

    fun orient(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            2 -> matrix.setScale(-1f, 1f)
            3 -> matrix.setRotate(180f)
            4 -> matrix.setScale(1f, -1f)
            5 -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            6 -> matrix.setRotate(90f)
            7 -> {
                matrix.setRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            8 -> matrix.setRotate(270f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
