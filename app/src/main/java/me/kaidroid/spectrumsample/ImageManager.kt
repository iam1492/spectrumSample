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

class ImageManager constructor(private val contentResolver: ContentResolver,
                               private val fileUri: Uri) {

    private val spectrum: Spectrum = Spectrum.make(SpectrumLogcatLogger(Log.INFO), DefaultPlugins.get())

    suspend fun resizeWithSpectrum(targetFileName: String, resolution: Int, quality: Int, result: (File, ResizeResult) -> Unit) = withContext(Dispatchers.IO) {
        val transcodeOptions = TranscodeOptions.Builder((EncodeRequirement(EncodedImageFormat.JPEG, quality)))
            .resize(ResizeRequirement.Mode.EXACT_OR_SMALLER, resolution)
            .build()
        val inputStream = contentResolver.openInputStream(fileUri)
        var outputStream: FileOutputStream? = null
        try {
            val outputFile = Utils.getOutputFile(targetFileName)
            outputStream = FileOutputStream(outputFile)

            val transcodeResult = spectrum.transcode(
                EncodedImageSource.from(inputStream),
                EncodedImageSink.from(outputStream),
                transcodeOptions,
                "spectrum"
            )
            Log.d(TAG, transcodeResult.toString())
            withContext(Dispatchers.Main) {
                result.invoke(outputFile, ResizeResult(transcodeResult.inputImageSpecification?.size?.width,
                    transcodeResult.inputImageSpecification?.size?.height,
                    transcodeResult.outputImageSpecification?.size?.width,
                    transcodeResult.outputImageSpecification?.size?.height,
                    transcodeResult.totalBytesRead,
                    transcodeResult.totalBytesWritten))
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    suspend fun resizeWithAndroidDefault(targetFileName: String, resolution: Int, quality: Int, result: (File?, ResizeResult) -> Unit) = withContext(Dispatchers.IO) {
        val afd = contentResolver.openAssetFileDescriptor(fileUri,"r")
        val originalFileSize = afd?.length
        afd?.close()

        val source = Utils.fromUri(contentResolver, fileUri)
        val bitmap = Utils.resizeBitmap(source, resolution)

        val outputFile = bitmap?.let { Utils.bitmapToJpeg(it, targetFileName, quality) }

        withContext(Dispatchers.Main) {
            result.invoke(outputFile, ResizeResult(source?.width, source?.height, bitmap?.width, bitmap?.height, originalFileSize, outputFile?.length()))
        }
    }

    data class ResizeResult(val originalWidth: Int? = 0,
                            val originalHeight: Int? = 0,
                            val resultWidth: Int? = 0,
                            val resultHeight: Int? = 0,
                            val originalFileSize: Long? = 0,
                            val resultFileSize: Long? = 0) {
        override fun toString(): String {
            return "originalSize[$originalWidth:$originalHeight]\n" +
                    "resultSize[$resultWidth:$resultHeight]\n" +
                    "original fileSize[$originalFileSize]\n" +
                    "result fileSize[$resultFileSize]\n" +
                    "compress ratio[${((resultFileSize?.div(originalFileSize!!.toFloat()))?.times(100))}]%"
        }
    }

    companion object {
        const val TAG = "ImageManager"
    }
}
