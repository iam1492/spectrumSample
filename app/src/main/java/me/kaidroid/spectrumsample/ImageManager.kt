package me.kaidroid.spectrumsample

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.facebook.spectrum.DefaultPlugins
import com.facebook.spectrum.EncodedImageSink
import com.facebook.spectrum.EncodedImageSource
import com.facebook.spectrum.Spectrum
import com.facebook.spectrum.image.EncodedImageFormat
import com.facebook.spectrum.logging.SpectrumLogcatLogger
import com.facebook.spectrum.options.TranscodeOptions
import com.facebook.spectrum.requirements.EncodeRequirement
import com.facebook.spectrum.requirements.ResizeRequirement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ImageManager constructor(contentResolver: ContentResolver, fileUri: Uri) {

    private val spectrum: Spectrum = Spectrum.make(SpectrumLogcatLogger(Log.INFO), DefaultPlugins.get())
    private val inputStream: InputStream? = contentResolver.openInputStream(fileUri)

    suspend fun resize(targetFileName: String, resolution: Int, quality: Int): File? = withContext(Dispatchers.IO) {
        val transcodeOptions = TranscodeOptions.Builder((EncodeRequirement(EncodedImageFormat.JPEG, quality)))
            .resize(ResizeRequirement.Mode.EXACT_OR_SMALLER, resolution)
            .build()

        var outputStream: FileOutputStream? = null
        try {
            val outputFile = FileUtils.getOutputFile(targetFileName)
            outputStream = FileOutputStream(outputFile)

            val transcodeResult = spectrum.transcode(
                EncodedImageSource.from(inputStream),
                EncodedImageSink.from(outputStream),
                transcodeOptions,
                "spectrum"
            )
            Log.d(TAG, transcodeResult.toString())
            outputFile
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            null
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    companion object {
        const val TAG = "ImageManager"
    }
}