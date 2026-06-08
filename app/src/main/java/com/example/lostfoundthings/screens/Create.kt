package com.example.lostfoundthings.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.lostfoundthings.R
import com.example.lostfoundthings.viewmodel.CreatePostViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream

@Composable
fun CreatePostScreen(navController: NavController) {
    val activity = LocalContext.current as ComponentActivity

    val viewModel: CreatePostViewModel = viewModel(activity)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()
    val menu = remember { mutableStateOf(false) }

    viewModel.checkAndLoadData()

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.permission = 1
        } else {
            viewModel.permission = 0
            Toast.makeText(context, context.getString(R.string.camera_permission_needed), Toast.LENGTH_SHORT).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)

                    val tempFile =
                        File(context.cacheDir, "gallery_img_${System.currentTimeMillis()}.jpg")
                    val outputStream = FileOutputStream(tempFile)

                    inputStream?.copyTo(outputStream)

                    inputStream?.close()
                    outputStream.close()

                    viewModel.capturedImagePath = tempFile.absolutePath

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, context.getString(R.string.could_not_read_photo), Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    Scaffold { paddingValues ->

        Spacer(modifier = Modifier.statusBarsPadding())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    viewModel.clearFields()
                    navController.popBackStack()
                          },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (viewModel.isEditMode) stringResource(R.string.editing) else stringResource(R.string.new_post),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                label = { Text(stringResource(R.string.what_lost_or_found)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.address,
                onValueChange = { viewModel.address = it },
                label = { Text(stringResource(R.string.where)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                enabled = viewModel.address.isNotEmpty(),
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
                        Modifier.height(56.dp)
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
                                contentDescription = stringResource(R.string.preview),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.FillWidth
                            )
                            DropdownMenu(
                                expanded = menu.value,
                                onDismissRequest = { menu.value = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.make_another_photo)) },
                                    onClick = {
                                        menu.value = false
                                        when(viewModel.permission) {
                                            1 -> viewModel.cameraUsing = true
                                            0 -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.choose_another_photo)) },
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                        menu.value = false
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete_photo), color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        menu.value = false
                                        viewModel.capturedImagePath = null
                                    }
                                )
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.pin_a_photo), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(16.dp))
                            OutlinedIconButton(onClick = {
                                when(viewModel.permission) {
                                    1 -> viewModel.cameraUsing = true
                                    0 -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = stringResource(R.string.make_photo),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            OutlinedIconButton(onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = stringResource(R.string.choose_photo),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = stringResource(R.string.post_type))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = viewModel.isLost,
                    onClick = { viewModel.isLost = true }
                )
                Text(text = stringResource(R.string.lost))
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = !viewModel.isLost,
                    onClick = { viewModel.isLost = false }
                )
                Text(text = stringResource(R.string.found))
            }

            Spacer(modifier = Modifier.height(16.dp))

            YandexMapsSection(viewModel = viewModel)

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = {
                    viewModel.savePost(
                        activity,
                        onSuccess = { successMessage ->
                            Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
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
                if (viewModel.isLoading) CircularProgressIndicator() else Text(if (viewModel.isEditMode) stringResource(R.string.save_changes) else stringResource(R.string.publish))
            }

            Spacer(modifier = Modifier.height(80.dp))

        }

        if (viewModel.cameraUsing) {
            CameraScreen(
                context = context,
                lifecycleOwner = lifecycleOwner,
                cameraPermissionState = viewModel.permission,
                onBackClick = { viewModel.cameraUsing = false },
                reload = {
                    viewModel.cameraUsing = false
                    viewModel.capturedImagePath = it
                }
            )
        }
    }
}