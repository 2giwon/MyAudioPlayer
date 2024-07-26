package com.egiwon.example.myaudioplayer.audio

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.egiwon.example.myaudioplayer.MainActivity
import com.egiwon.example.myaudioplayer.ui.theme.LightGray
import com.egiwon.example.myaudioplayer.ui.theme.Pink40
import com.egiwon.example.myaudioplayer.ui.theme.Pink80
import com.egiwon.example.myaudioplayer.ui.theme.Purple40
import com.egiwon.example.myaudioplayer.ui.theme.Purple80
import com.egiwon.example.myaudioplayer.ui.theme.PurpleGrey40
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

@Composable
fun AudioScreen(
    modifier: Modifier = Modifier,
    audioViewModel: AudioViewModel,
    onStopPlaying: () -> Unit = {},
    onPickAudioFile: (String) -> Unit = {}
) {
    val decibelLevels by audioViewModel.decibelLevels.collectAsState()
    val isPlaying by audioViewModel.isPlaying.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as MainActivity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MainActivity.REQUEST_RECORD_AUDIO_PERMISSION
            )
        }
    }

    Column(
        modifier = modifier.fillMaxSize().background(LightGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isPlaying) {
            Button(
                onClick = onStopPlaying,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple40,
                    contentColor = Color.White
                )
            ) {
                Text("Stop")
            }
        } else {
            Button(
                onClick = { onPickAudioFile("audio/*") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple40,
                    contentColor = Color.White
                )
            ) {
                Text("Pick Audio")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (decibelLevels.isNotEmpty()) {
            Log.e("Audio", "AudioScreen: decibel : $decibelLevels")
            DecibelChart(decibelLevels)
        }
    }
}

@Composable
fun DecibelChart(decibelLevels: List<Float>) {
    // Use MPAndroidChart or another charting library to render the decibel levels
    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setMaxVisibleValueCount(60)
                setPinchZoom(false)
                setDrawGridBackground(false)

                axisLeft.isEnabled = false
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.setDrawAxisLine(false)
                xAxis.setDrawLabels(false)
                legend.isEnabled = false
            }
        },
        update = { barChart ->
            val entries = decibelLevels.mapIndexed { index, value ->
                BarEntry(
                    index.toFloat(),
                    value.toFloat()
                )
            }
            val dataSet = BarDataSet(entries, "Decibel Levels").apply {
                colors = listOf(
                    Purple40.toArgb(),
                    PurpleGrey40.toArgb(),
                    Purple80.toArgb(),
                    Pink80.toArgb(),
                    Pink40.toArgb()
                )
                setDrawValues(false)
            }
            val barData = BarData(dataSet)
            barChart.data = barData
            barChart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AudioScreen(
        audioViewModel = AudioViewModel(
            application = Application()
        )
    )
}
