package com.example.lab_week_11_b

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.VideoView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnRecord: Button
    private lateinit var videoView: VideoView

    private val REQUEST_VIDEO_CAPTURE = 200
    private val REQUEST_PERMISSION = 201

    private var videoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRecord = findViewById(R.id.btnRecordVideo)
        videoView = findViewById(R.id.videoView)

        btnRecord.setOnClickListener {
            checkPermissionsAndRecord()
        }
    }

    private fun checkPermissionsAndRecord() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missing.toTypedArray(),
                REQUEST_PERMISSION
            )
        } else {
            openCameraForVideo()
        }
    }

    private fun openCameraForVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, REQUEST_VIDEO_CAPTURE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSION && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            openCameraForVideo()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == Activity.RESULT_OK) {
            videoUri = data?.data

            videoView.setVideoURI(videoUri)
            videoView.setOnPreparedListener { mp: MediaPlayer ->
                mp.isLooping = true
            }
            videoView.start()
        }
    }
}
