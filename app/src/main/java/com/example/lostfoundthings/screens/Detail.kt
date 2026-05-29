package com.example.lostfoundthings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lostfoundthings.R
import com.example.lostfoundthings.viewmodel.PostDetailViewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

@Composable
fun PostDetailScreen(
    navController: NavController,
    viewModel: PostDetailViewModel = viewModel()
) {

    Scaffold { innerPadding ->
        val currentPost = viewModel.post

        Spacer(modifier = Modifier.statusBarsPadding())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Детали объявления",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (currentPost != null) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {

                if (!currentPost.photo.isNullOrEmpty()) {
                    AsyncImage(
                        model = currentPost.photo,
                        contentDescription = "Фото вещи",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = currentPost.authorPhoto,
                        contentDescription = "Аватар автора",
                        fallback = painterResource(id = R.drawable.outline_account_circle_24),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = currentPost.authorName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                }

                Text(
                    text = "Опубликовано: ${viewModel.formatTimestamp(currentPost.timestamp)}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = if (currentPost.state == "lost") "Потеряно" else "Найдено",
                    color = Color.White,
                    modifier = Modifier
                        .background(if (currentPost.state == "lost") Color(0xFFEF5350) else Color(0xFF66BB6A),
                            RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = currentPost.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = currentPost.description, fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Адрес: ${currentPost.address}", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(16.dp))

                if (currentPost.address.isNotEmpty()) {

                    Text(text = "Место на карте:", modifier = Modifier.padding(bottom = 8.dp))

                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        factory = { ctx ->
                            MapView(ctx).apply {
                                val map = this.mapWindow.map

                                map.isScrollGesturesEnabled = false
                                map.isZoomGesturesEnabled = false

                                val itemPoint = Point(currentPost.lat, currentPost.lon)
                                map.move(
                                    CameraPosition(itemPoint, 15.0f, 0.0f, 0.0f),
                                    Animation(Animation.Type.SMOOTH, 0f),
                                    null
                                )

                                val placemark = map.mapObjects.addPlacemark()
                                placemark.geometry = itemPoint
                                val iconStyle = IconStyle().apply {
                                    scale = 0.1f
                                    anchor = android.graphics.PointF(0.5f, 1.0f)
                                }
                                placemark.setIcon(ImageProvider.fromResource(ctx, R.drawable.placemark), iconStyle)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))

            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Не удалось загрузить данные объявления")
            }
        }
    }
}