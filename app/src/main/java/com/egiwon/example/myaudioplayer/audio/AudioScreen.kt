package com.egiwon.example.myaudioplayer.audio

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.egiwon.example.myaudioplayer.MainActivity
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
import kotlin.math.abs

@Composable
fun AudioScreen(
    modifier: Modifier = Modifier,
    audioViewModel: AudioViewModel,
    onStopPlaying: () -> Unit = {},
    onPickAudioFile: (String) -> Unit = {}
) {
    val decibelLevels by audioViewModel.decibelLevels.collectAsState()
    val isPlaying by audioViewModel.isPlaying.collectAsState()
    val fileName by audioViewModel.fileName.collectAsState()
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

    Box(
        modifier = Modifier
            .background(Color(0xFFF9F9E3))
            .wrapContentSize(Alignment.Center)
    ) {
        Wave(
            color = Color(0xFF00AAFF),
            durationMillis = 3000,
            size = 700.dp,
            opacity = 0.4f,
            initialRotation = 0f
        )
        Wave(
            color = Color.Yellow,
            durationMillis = 7000,
            size = 750.dp,
            opacity = 0.1f,
            initialRotation = 0f
        )
        Wave(
            color = Color(0xFF00AAFF),
            durationMillis = 5000,
            size = 700.dp,
            opacity = 0.4f,
            initialRotation = 0f
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = verticalGradient(
                        colors = listOf(
                            Color(0xFFEE88aa),
                            Color(0xFF00defF),
                            Color.White.copy(alpha = 0.2f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            fileName?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
            }
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
                DecibelChart(decibelLevels)
            }
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
                    abs(value)
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

@Composable
fun Wave(color: Color, durationMillis: Int, size: Dp, opacity: Float, initialRotation: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val rotation by infiniteTransition.animateFloat(
        initialValue = initialRotation,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis, easing = LinearEasing)
        ), label = ""
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(300.dp))
        Box(
            modifier = Modifier
                .size(width = size, height = 400.dp)
                .graphicsLayer {
                    rotationZ = rotation
                    alpha = opacity
                }
                .background(color = color, shape = RoundedCornerShape(46)),
        )
    }

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
