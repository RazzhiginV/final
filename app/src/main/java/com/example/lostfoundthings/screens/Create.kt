package com.example.lostfoundthings.screens

import android.Manifest
import android.graphics.BitmapFactory.decodeFile
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lostfoundthings.viewmodel.CreatePostViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@Composable
fun CreatePostScreen(navController: NavController) {
    val activity = LocalContext.current as ComponentActivity

    val viewModel: CreatePostViewModel = viewModel(activity)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()
    var menu = remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.permission = 1
        } else {
            viewModel.permission = 0
            Toast.makeText(context, "Нужно разрешение камеры", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold { paddingValues ->

        Spacer(modifier = Modifier.statusBarsPadding())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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
                text = "Новое объявление",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                label = { Text("Что потеряно / найдено") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                label = { Text("Описание вещи и обстоятельств") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.address,
                onValueChange = { viewModel.address = it },
                label = { Text("Где это произошло (Адрес/Место)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                    if (viewModel.capturedImagePath != null) {
                        Modifier.wrapContentHeight()
                    } else {
                        Modifier.height(150.dp)
                    }
                ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (viewModel.capturedImagePath != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { menu.value = true }
                                )
                        ) {
                            AsyncImage(
                                model = viewModel.capturedImagePath,
                                contentDescription = "preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.FillWidth
                            )
                            DropdownMenu(
                                expanded = menu.value,
                                onDismissRequest = { menu.value = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Переделать фото") },
                                    onClick = {
                                        menu.value = false
                                        viewModel.cameraUsing = true
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Удалить фото", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        menu.value = false
                                        viewModel.capturedImagePath = null
                                    }
                                )
                            }
                        }
                    } else {
                        Button(onClick = {
                            when(viewModel.permission) {
                                1 -> viewModel.cameraUsing = true
                                0 -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }) {
                            Text("Сделать фото")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Тип объявления: ")

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = viewModel.isLost,
                    onClick = { viewModel.isLost = true }
                )
                Text(text = "Потерял")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = !viewModel.isLost,
                    onClick = { viewModel.isLost = false }
                )
                Text(text = "Нашел")
            }

            Spacer(modifier = Modifier.height(16.dp))

            YandexMapsSection(viewModel = viewModel)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.publishPost(
                        onSuccess = {
                            Toast.makeText(context, "Объявление успешно создано!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                enabled = !viewModel.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (viewModel.isLoading) CircularProgressIndicator() else Text("Опубликовать")
            }

            Spacer(modifier = Modifier.height(80.dp))

        }

        if (viewModel.cameraUsing) {
            CameraScreen(
                context = context,
                lifecycleOwner = lifecycleOwner,
                cameraPermissionState = viewModel.permission,
                reload = {
                    viewModel.cameraUsing = false
                    viewModel.capturedImagePath = it
                }
            )
        }
    }
}