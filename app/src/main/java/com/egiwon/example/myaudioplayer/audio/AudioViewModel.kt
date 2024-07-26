package com.egiwon.example.myaudioplayer.audio

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewModelScope
import com.egiwon.example.myaudioplayer.base.BaseAndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log10
import kotlin.math.sqrt

class AudioViewModel(
    application: Application
) : BaseAndroidViewModel(application) {

    private val _decibelLevels = MutableStateFlow(List(30) { 0f }) // 30개의 최신 값만 유지
    val decibelLevels: StateFlow<List<Float>> = _decibelLevels

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _fileName = MutableStateFlow<String?>(null)
    val fileName: StateFlow<String?> = _fileName

    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String? = null

    private var audioRecord: AudioRecord? = null
    private var audioRecordBufferSize: Int = 0

    val updatedLevels = mutableListOf<Float>()

    fun setAudioFilePath(filePath: String) {
        audioFilePath = filePath
        _fileName.value = filePath.substringAfterLast('/')
    }

    fun playAudio(filePath: String) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
            start()
            setOnCompletionListener {
                stopAudioAnalysis()
            }
        }
        initAudioRecord()
        startAudioAnalysis()
    }

    private fun initAudioRecord() {
        audioRecordBufferSize = AudioRecord.getMinBufferSize(
            44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        if (ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            audioRecordBufferSize
        )
    }

    private fun startAudioAnalysis() {
        _isPlaying.value = true
        audioRecord?.startRecording()
        viewModelScope.launch(coroutineExceptionHandler) {
            analyzeAudio()
        }
    }

    private suspend fun analyzeAudio() {
        withContext(Dispatchers.IO) {
            while (_isPlaying.value) {
                val buffer = ShortArray(audioRecordBufferSize)
                audioRecord?.read(buffer, 0, buffer.size)
                val rms = calculateRMS(buffer)
                val db = calculateDecibels(rms)

                // 최신 값으로 업데이트
                updatedLevels.add(db.toFloat())

                if (updatedLevels.size > 30) {
                    updatedLevels.removeAt(0)
                }
                _decibelLevels.value = updatedLevels.toList()
            }
        }
    }

    private fun calculateRMS(buffer: ShortArray): Double {
        var rms = 0.0
        for (i in buffer.indices) {
            rms += buffer[i] * buffer[i]
        }
        rms = sqrt(rms / buffer.size)
        return rms
    }

    private fun calculateDecibels(rms: Double): Double {
        return if (rms > 0) 20 * log10(rms) else 0.0
    }

    fun stopAudioAnalysis() {
        _isPlaying.value = false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}
