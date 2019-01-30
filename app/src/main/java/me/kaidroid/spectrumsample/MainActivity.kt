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

    lateinit var imageManager: ImageManager

    lateinit var viewBinder: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinder = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            if (needStoragePermission()) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE)
            } else {
                startImagePicker()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fileUri = data?.data
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && fileUri != null) {
            imageManager = ImageManager(contentResolver, fileUri)
        }

        launch {
            val outFile = imageManager.resize("resized1.jpg", 1024, 100)
            GlideApp.with(this@MainActivity)
                .load(outFile)
                .centerCrop()
                .into(viewBinder.mainContainer.image)
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

    private fun needStoragePermission() = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

    companion object {
        const val PICK_IMAGE_REQUEST = 1000
        const val MIME_TYPE_IMAGE = "image/*"
        const val REQUEST_STORAGE = 1
    }
}
