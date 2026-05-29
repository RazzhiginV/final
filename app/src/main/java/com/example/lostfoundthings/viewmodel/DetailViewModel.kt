package com.example.lostfoundthings.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostfoundthings.data.Post
import com.example.lostfoundthings.data.PostRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    var post by mutableStateOf<Post?>(null)
    var isLoading by mutableStateOf(true)

    init {
        val postId: String? = savedStateHandle["postId"]
        if (!postId.isNullOrBlank()) {
            loadPostDetails(postId)
        } else {
            isLoading = false
        }
    }

    fun loadPostDetails(postId: String) {
        if (post != null || postId.isBlank()) return

        isLoading = true
        viewModelScope.launch {
            val result = PostRepository.getPostById(postId)
            post = result
            isLoading = false
        }
    }

    fun formatTimestamp(timestamp: Number): String {
        val rawTime = timestamp.toLong()
        val timeInMillis = if (rawTime < 10000000000L) rawTime * 1000 else rawTime

        val date = Date(timeInMillis)
        val sdf = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
        return sdf.format(date)
    }

}