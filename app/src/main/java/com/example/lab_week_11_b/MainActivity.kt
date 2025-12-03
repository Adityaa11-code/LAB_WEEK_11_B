package com.example.lab_week_11_b

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var imagePreview: ImageView
    private lateinit var btnTakePhoto: Button

    private var photoUri: Uri? = null

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK && photoUri != null) {
                val inputStream = contentResolver.openInputStream(photoUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                if (bitmap != null) {
                    imagePreview.setImageBitmap(bitmap)
                    saveBitmapToMediaStore(bitmap)
                    Toast.makeText(this, "Foto berhasil disimpan!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imagePreview = findViewById(R.id.imagePreview)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)

        btnTakePhoto.setOnClickListener {
            checkPermissionAndOpenCamera()
        }
    }


    private fun checkPermissionAndOpenCamera() {
        val cameraPermission = Manifest.permission.CAMERA

        if (ContextCompat.checkSelfPermission(this, cameraPermission) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(cameraPermission),
                101
            )
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val filename =
            "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        photoUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        cameraLauncher.launch(intent)
    }


    private fun saveBitmapToMediaStore(bitmap: Bitmap): Uri? {
        val filename =
            "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"

        return try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/LAB_WEEK_11_B"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = contentResolver
                val uri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                if (uri != null) {
                    var out: OutputStream? = null
                    try {
                        out = resolver.openOutputStream(uri)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out!!)
                    } finally {
                        out?.close()
                    }

                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)

                    return uri
                } else {
                    null
                }

            } else {

                val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                if (storageDir != null && !storageDir.exists()) storageDir.mkdirs()

                val file = File(storageDir, filename)
                var out: OutputStream? = null

                try {
                    out = file.outputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out!!)
                } finally {
                    out?.close()
                }

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.DATA, file.absolutePath)
                }

                contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
