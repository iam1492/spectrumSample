package me.kaidroid.spectrumsample

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.kaidroid.spectrumsample.databinding.ActivityMainBinding
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    var imageManager: ImageManager? = null

    lateinit var viewBinder: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinder = DataBindingUtil.setContentView(this, R.layout.activity_main)

        viewBinder.mainContainer.resolution.setText(DEFAULT_RESOLUTION.toString())
        viewBinder.mainContainer.quality.setText(DEFAULT_QUALITY.toString())

        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            if (needStoragePermission()) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE)
            } else {
                startImagePicker()
            }
        }

        viewBinder.mainContainer.button.setOnClickListener {
            if (useSpectrum()) {
                resizeWithSpectrum()
            } else {
                resizeWithDefault()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fileUri = data?.data
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && fileUri != null) {

            GlideApp.with(this@MainActivity)
                .load(fileUri)
                .centerCrop()
                .into(viewBinder.mainContainer.image)

            imageManager = ImageManager(contentResolver, fileUri)
        }
    }

    private fun resizeWithSpectrum() = launch {
        imageManager?.resizeWithSpectrum(System.currentTimeMillis().toString(), getResolution(), getQuality()) { outFile, result ->
            GlideApp.with(this@MainActivity)
                .load(outFile)
                .centerCrop()
                .into(viewBinder.mainContainer.image)

            printResult(result)
        }
    }

    private fun resizeWithDefault() = launch {
        imageManager?.resizeWithAndroidDefault(System.currentTimeMillis().toString(), getResolution(), getQuality()) { outFile, result ->
            GlideApp.with(this@MainActivity)
                .load(outFile)
                .centerCrop()
                .into(viewBinder.mainContainer.image)

            printResult(result)
        }
    }

    private fun startImagePicker() {
        startActivityForResult(Intent.createChooser(Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = MIME_TYPE_IMAGE
        }, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            startImagePicker()
        }
    }

    private fun getResolution(): Int {
        val resolution = viewBinder.mainContainer.resolution.text.toString()
        return if (resolution.isNotEmpty()) resolution.toInt() else DEFAULT_RESOLUTION
    }

    private fun getQuality(): Int {
        val quality = viewBinder.mainContainer.quality.text.toString()
        return if (quality.isNotEmpty()) quality.toInt() else DEFAULT_QUALITY
    }

    private fun useSpectrum(): Boolean = viewBinder.mainContainer.toggleSwitch.isChecked

    private fun needStoragePermission() = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

    private fun printResult(result: ImageManager.ResizeResult) {
        viewBinder.mainContainer.result_resolution.text =
            "${result.originalWidth}x${result.originalHeight} ==> ${result.resultWidth}x${result.resultHeight}"
        viewBinder.mainContainer.result_size.text = "${result.originalFileSize} ==> ${result.resultFileSize}"
        viewBinder.mainContainer.result_ratio.text =
            "${((result.resultFileSize?.div(result.originalFileSize!!.toFloat()))?.times(100))}%"
    }

    companion object {
        const val PICK_IMAGE_REQUEST = 1000
        const val MIME_TYPE_IMAGE = "image/*"
        const val REQUEST_STORAGE = 1
        const val DEFAULT_RESOLUTION = 1024
        const val DEFAULT_QUALITY = 100
    }
}
