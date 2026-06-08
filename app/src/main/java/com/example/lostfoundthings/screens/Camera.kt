package com.example.lostfoundthings.screens

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.example.lostfoundthings.R
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import coil.compose.rememberAsyncImagePainter
import com.example.lostfoundthings.data.takePhoto
import java.io.File

@Composable
fun CameraScreen(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraPermissionState: Int,
    onBackClick: () -> Unit,
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
                    lifecycleOwner = lifecycleOwner,
                    onCameraReady = { provider, capture ->
                        cameraProvider = provider
                        imageCapture = capture
                        isCameraReady = true
                    }
                )
            } else {
                Image(
                    painter = rememberAsyncImagePainter(model = File(capturedPhotoPath)),
                    contentDescription = stringResource(R.string.photo_preview),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(top = 24.dp, start = 16.dp)
                    .align(Alignment.TopStart)
                    .size(48.dp)
                    .shadow(8.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (!isPhotoTaken && isCameraReady) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0x80000000))
                ) {}

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 50.dp)
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
                    OutlinedButton(
                        onClick = { reload(capturedPhotoPath) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.5f),
                            contentColor = colorResource(R.color.grey)
                        )
                    ) {
                        Text(stringResource(R.string.ok))
                    }

                    OutlinedButton(
                        onClick = {
                            isPhotoTaken = false
                            capturedPhotoPath = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.5f),
                            contentColor = colorResource(R.color.grey)
                        )
                    ) {
                        Text(stringResource(R.string.make_a_photo))
                    }
                }
            }
        }
    }
}