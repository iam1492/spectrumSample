package me.kaidroid.spectrumsample

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Point
import android.net.Uri
import android.os.Environment
import java.io.*

object Utils {
    private val externalCacheDir: File
        get() {
            val dir = File("${Environment.getExternalStorageDirectory()}/Android/data/${BuildConfig.APPLICATION_ID}/cache")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    private val externalTmpDir: File
        get() {
            val dir = File(externalCacheDir, "tmp")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    fun getOutputFile(fileName: String): File {
        return File(externalTmpDir, fileName)
    }

    fun fromUri(contentResolver: ContentResolver, uri: Uri): Bitmap? {
        var inputStream: InputStream? = null
        return try {
            inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, BitmapFactory.Options())
        } catch (e: FileNotFoundException) {
            null
        } catch (e: NullPointerException) {
            null
        } finally {
            inputStream?.close()
        }
    }

    internal fun resizeBitmap(orgImage: Bitmap?, maxResolution: Int): Bitmap? {
        if (orgImage == null) {
            return null
        }

        val matrix = Matrix()

        val resized = calculateFit(orgImage.width, orgImage.height, maxResolution)

        val w = resized.x.toFloat() / orgImage.width.toFloat()
        val h = resized.y.toFloat() / orgImage.height.toFloat()

        matrix.postScale(w, h)
        return Bitmap.createBitmap(orgImage, 0, 0, orgImage.width, orgImage.height, matrix, true)
    }

    private fun calculateFit(originWidth: Int, originHeight: Int, maxResolution: Int): Point {
        var dw = if (maxResolution > 0) maxResolution else originWidth
        var dh = if (maxResolution > 0) maxResolution else originHeight

        val wAspect = dw.toDouble() / originWidth
        val hAspect = dh.toDouble() / originHeight
        if (wAspect < hAspect) {
            dh = (originHeight * wAspect).toInt()
        } else {
            dw = (originWidth * hAspect).toInt()
        }

        if (dw > originWidth || dh > originHeight) {
            dw = originWidth
            dh = originHeight
        }
        return Point().apply {
            x = if (dw > 0) dw else 1
            y = if (dh > 0) dh else 1
        }
    }

    fun bitmapToJpeg(bitmap: Bitmap, fileName: String, quality: Int = 100): File? {
        var file: File? = null
        try {
            file = File(externalTmpDir, fileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.close()
        } catch (e: IOException) {
        }

        return file
    }
}