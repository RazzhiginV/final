package com.example.lostfoundthings.viewmodel

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.activity.ComponentActivity
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

class CreatePostViewModel() : ViewModel() {
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var address by mutableStateOf("")
    var isLost by mutableStateOf(true)
    var capturedImagePath by mutableStateOf<String?>(null)
    var cameraUsing by mutableStateOf(false)
    var permission by mutableIntStateOf(0)
    var selectedLat by mutableStateOf(55.7558)
    var selectedLon by mutableStateOf(37.6173)
    var isEditMode by mutableStateOf(false)
    var postId: String? by mutableStateOf(null)
    var isLoading by mutableStateOf(false)
    private var loadedPostId: String? = null

    fun checkAndLoadData() {
        Log.d("MyLog", "ViewModel: Проверка перехвата. ID = $postId")

        if (!postId.isNullOrBlank()) {
            if (postId != loadedPostId) {
                loadedPostId = postId
                loadPostDataForEditing(postId!!)
            }
        } else {
            if (isEditMode || loadedPostId != null) {
                clearFields()
            }
        }
    }

    private fun loadPostDataForEditing(postId: String) {
        isLoading = true
        viewModelScope.launch {
            val post = PostRepository.getPostById(postId)
            if (post != null) {
                isEditMode = true
                title = post.title
                description = post.description
                address = post.address
                capturedImagePath = post.photo
                selectedLat = post.lat
                selectedLon = post.lon
                isLost = post.state == "lost"

            } else {
                isLoading = false
            }
            isLoading = false
        }
    }

    fun savePost(activity: ComponentActivity, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true

            if (isEditMode && postId != null) {
                var finalServerPhotoUrl = capturedImagePath

                if (capturedImagePath != null && (capturedImagePath!!.startsWith("file:/") || capturedImagePath!!.contains("cache"))) {
                    try {
                        val cleanPath = capturedImagePath!!.substringAfter("file:")
                        val file = File(cleanPath)
                        val fileBytes: ByteArray = file.readBytes()

                        val uploadedUrl = PostRepository.uploadImageToSupabase(fileBytes)

                        if (uploadedUrl != null) {
                            finalServerPhotoUrl = uploadedUrl
                        } else {
                            isLoading = false
                            onError("Не удалось загрузить новое фото в облако")
                            return@launch
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        isLoading = false
                        onError("Ошибка при чтении файла изображения")
                        return@launch
                    }
                }

                val updateData = mapOf(
                    "title" to title,
                    "description" to description,
                    "address" to address,
                    "photo" to finalServerPhotoUrl,
                    "lat" to selectedLat,
                    "lon" to selectedLon
                )

                val success = PostRepository.updatePost(postId!!, updateData)
                if (success) {
                    try {
                        val viewModelProvider = androidx.lifecycle.ViewModelProvider(activity)
                        val postsViewModel = viewModelProvider[PostsViewModel::class.java]

                        postsViewModel.loadAllPosts()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    clearFields()
                    isLoading = false
                    onSuccess("Изменения сохранены")
                } else {
                    isLoading = false
                    onError("Не удалось обновить пост на сервере")
                }

            } else {
                if (title.isBlank() || description.isBlank() || address.isBlank()) {
                    isLoading = false
                    onError("Пожалуйста, заполните все поля")
                    return@launch
                }

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
                            clearFields()
                            isLoading = false
                            onSuccess("Объявление успешно создано")
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

    fun clearFields() {
        title = ""
        description = ""
        address = ""
        cameraUsing = false
        capturedImagePath = ""
        isLost = true
        selectedLat = 55.7558
        selectedLon = 37.6173
        isEditMode = false
        postId = null
        loadedPostId = null
    }

}