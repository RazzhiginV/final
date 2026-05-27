package com.example.lostfoundthings.screens

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import com.example.lostfoundthings.data.takePhoto

@Composable
fun CameraScreen(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraPermissionState: Int,
    reload: (String) -> Unit = {}
) {
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    var isCameraReady by remember { mutableStateOf(false) }
    var capturedPhotoPath by remember { mutableStateOf("") }
    var isPhotoTaken by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (cameraPermissionState == 1) {

            if (!isPhotoTaken) {
                CameraPreview(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    onCameraReady = { provider, capture ->
                        cameraProvider = provider
                        imageCapture = capture
                        isCameraReady = true
                    }
                )
            } else {
                androidx.compose.foundation.Image(
                    painter = coil.compose.rememberAsyncImagePainter(model = java.io.File(capturedPhotoPath)),
                    contentDescription = "Превью сделанного фото",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

            }
            if (!isPhotoTaken && isCameraReady) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 70.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .size(75.dp)
                        .clickable {
                            takePhoto(
                                context = context,
                                imageCapture = imageCapture,
                                onPhotoTaken = { path ->
                                    capturedPhotoPath = path
                                    isPhotoTaken = true
                                }
                            )
                        }
                ) {}
            } else if (isPhotoTaken) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 50.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { reload(capturedPhotoPath) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Text("Ок")
                    }

                    Button(
                        onClick = {
                            isPhotoTaken = false
                            capturedPhotoPath = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Text("Сделать еще фото")
                    }
                }
            }
        }
    }
}