package com.egiwon.example.myaudioplayer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.egiwon.example.myaudioplayer.audio.AudioScreen
import com.egiwon.example.myaudioplayer.audio.AudioViewModel
import com.egiwon.example.myaudioplayer.ui.theme.LightGray
import com.egiwon.example.myaudioplayer.ui.theme.MyAudioPlayerTheme

class MainActivity : ComponentActivity() {

    private val audioViewModel by viewModels<AudioViewModel>()

    private val pickAudioFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val filePath = getFilePathFromUri(this, it)
                if (filePath != null) {
                    audioViewModel.setAudioFilePath(filePath)
                    audioViewModel.playAudio(filePath)
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            MyAudioPlayerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AudioScreen(
                        modifier = Modifier.padding(innerPadding),
                        audioViewModel = audioViewModel,
                        onStopPlaying = audioViewModel::stopAudioAnalysis,
                        onPickAudioFile = pickAudioFile::launch
                    )
                }
            }
        }

        if (!hasAudioPermission()) {
            requestAudioPermission()
        }
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_AUDIO_PERMISSION
        )
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getFilePathFromUri(context: Context, uri: Uri): String? {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                filePath = it.getString(columnIndex)
            }
        }
        return filePath
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
                finish() // Close the app if permission is not granted
            }
        }
    }

    companion object {
        const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }
}
