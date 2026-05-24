package com.example.lostfoundthings.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostfoundthings.data.Post
import com.example.lostfoundthings.data.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostsViewModel : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())

    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    var isLoading by mutableStateOf(false)
    var errorText by mutableStateOf<String?>(null)

    init {
        loadAllPosts()
    }

    private fun loadAllPosts() {
        isLoading = true
        errorText = null

        viewModelScope.launch {
            PostRepository.getAllPosts(
                onSuccess = { fetchedList ->
                    isLoading = false
                    _posts.value = fetchedList
                },
                onError = { message ->
                    isLoading = false
                    errorText = message
                }
            )
        }
    }
}