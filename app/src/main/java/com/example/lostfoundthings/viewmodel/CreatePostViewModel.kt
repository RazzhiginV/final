package com.example.lostfoundthings.viewmodel

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostfoundthings.data.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class CreatePostViewModel : ViewModel() {
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var address by mutableStateOf("")
    var isLost by mutableStateOf(true)

    var capturedImagePath by mutableStateOf<String?>(null)
    var cameraUsing by mutableStateOf(false)
    var permission by mutableIntStateOf(0)
    var selectedLat by mutableStateOf(55.7558)
    var selectedLon by mutableStateOf(37.6173)

    var isLoading by mutableStateOf(false)



    fun publishPost(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (title.isBlank() || description.isBlank() || address.isBlank()) {
            onError("Пожалуйста, заполните все поля")
            return
        }

        isLoading = true

        viewModelScope.launch {
            try {
                var serverPhotoUrl: String? = null
                capturedImagePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        val imageBytes = withContext(Dispatchers.IO) {
                            file.readBytes()
                        }
                        serverPhotoUrl = PostRepository.uploadImageToSupabase(imageBytes)
                    }
                }

                PostRepository.createNewPost(
                    title = title.trim(),
                    description = description.trim(),
                    address = address.trim(),
                    lat = selectedLat,
                    lon = selectedLon,
                    photoUrl = serverPhotoUrl,
                    state = if (isLost) "lost" else "found",
                    onSuccess = {
                        isLoading = false
                        onSuccess()
                    },
                    onError = { error ->
                        isLoading = false
                        onError(error)
                    }
                )

            } catch (e: Exception) {
                isLoading = false
                onError("Ошибка при публикации: ${e.message}")
            }
        }
    }

    fun updateAddressFromCoordinates(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(selectedLat, selectedLon, 1)

                if (!addresses.isNullOrEmpty()) {
                    val addressObj = addresses[0]

                    val fullAddress = addressObj.getAddressLine(0) ?: ""

                    withContext(Dispatchers.Main) {
                        address = fullAddress
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}