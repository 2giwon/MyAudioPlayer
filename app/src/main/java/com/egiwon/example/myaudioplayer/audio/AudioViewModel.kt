package com.egiwon.example.myaudioplayer.audio

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.egiwon.example.myaudioplayer.base.BaseAndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.log10
import kotlin.math.sqrt

class AudioViewModel(
    application: Application
) : BaseAndroidViewModel(application) {

    private val _decibelLevels = MutableStateFlow(List(30) { 0f }) // 30개의 최신 값만 유지
    val decibelLevels: StateFlow<List<Float>> = _decibelLevels

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 100 // ms

    private var audioRecord: AudioRecord? = null
    private var audioRecordBufferSize: Int = 0

    fun setAudioFilePath(filePath: String) {
        audioFilePath = filePath
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
            MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, audioRecordBufferSize
        )
    }

    private fun startAudioAnalysis() {
        _isPlaying.value = true
        audioRecord?.startRecording()
        handler.post(updateTask)
    }

    private val updateTask = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                val buffer = ShortArray(audioRecordBufferSize)
                audioRecord?.read(buffer, 0, buffer.size)
                val rms = calculateRMS(buffer)
                val db = if (rms > 0) 20 * log10(rms) else 0.0

                // 최신 값으로 업데이트
                val updatedLevels = _decibelLevels.value.toMutableList()
                updatedLevels.removeAt(0)
                updatedLevels.add(db.toFloat())

                _decibelLevels.value = updatedLevels.toList()
                handler.postDelayed(this, updateInterval)
            }
        }
    }

    private fun calculateRMS(buffer: ShortArray): Double {
        var sum = 0.0
        for (sample in buffer) {
            val normalizedSample = sample / 32768.0 // 16비트 오디오 샘플을 [-1, 1] 범위로 정규화
            sum += normalizedSample * normalizedSample
        }
        return sqrt(sum / buffer.size)
    }

    fun stopAudioAnalysis() {
        _isPlaying.value = false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(updateTask)
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}
