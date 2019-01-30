package me.kaidroid.spectrumsample

import android.os.Environment
import java.io.File
import java.util.*

object FileUtils {
    private val externalDir: File
        get() {
            val dir = File(String.format(Locale.US, "%s/Android/data/%s", Environment.getExternalStorageDirectory(), BuildConfig.APPLICATION_ID))
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    private val externalCacheDir: File
        get() {
            val dir = File(String.format(Locale.US, "%s/%s", externalDir, "cache"))
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
}