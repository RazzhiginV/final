package com.example.lostfoundthings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.lostfoundthings.R
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.lostfoundthings.viewmodel.CreatePostViewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.PlacemarkMapObject

@Composable
fun YandexMapsSection(viewModel: CreatePostViewModel) {
    var showMapDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var placemark by remember { mutableStateOf<PlacemarkMapObject?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = if (viewModel.address.isBlank()) "Адрес не указан (выберите на карте)" else "Место: ${viewModel.address}",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE0E0E0))
                .clickable { showMapDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Нажмите, чтобы выбрать место на карте", color = Color.DarkGray)
        }
    }

    if (showMapDialog) {
        Dialog(
            onDismissRequest = { showMapDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val map = mapView.mapWindow.map
                        map.mapObjects.clear()

                        val startPoint = Point(viewModel.selectedLat, viewModel.selectedLon)
                        map.move(
                            CameraPosition(startPoint, 15.0f, 0.0f, 0.0f),
                            Animation(Animation.Type.SMOOTH, 0f),
                            null
                        )

                        val newPlacemark = map.mapObjects.addPlacemark().apply {
                            geometry = startPoint
                            val iconStyle = IconStyle().apply {
                                scale = 0.1f
                                anchor = android.graphics.PointF(0.5f, 1.0f)
                            }
                            setIcon(ImageProvider.fromResource(ctx, R.drawable.placemark), iconStyle)
                        }
                        placemark = newPlacemark

                        map.addInputListener(object : com.yandex.mapkit.map.InputListener {
                            override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {
                                viewModel.selectedLat = point.latitude
                                viewModel.selectedLon = point.longitude

                                newPlacemark.geometry = point
                            }

                            override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {}
                        })

                        mapView
                    },
                    update = {}
                )

                IconButton(
                    onClick = { showMapDialog = false },
                    modifier = Modifier
                        .padding(top = 48.dp, start = 16.dp)
                        .align(Alignment.TopStart)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.Black
                    )
                }

                Button(
                    onClick = {
                        showMapDialog = false
                        viewModel.updateAddressFromCoordinates(context)},
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .fillMaxWidth(0.8f)
                ) {
                    Text("Готово, сохранить место")
                }
            }
        }
    }
}