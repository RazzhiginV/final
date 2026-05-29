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

class MyPostsViewModel : ViewModel() {
    private val _myPosts = MutableStateFlow<List<Post>>(emptyList())
    val myPosts: StateFlow<List<Post>> = _myPosts.asStateFlow()

    var isLoading by mutableStateOf(false)
    var errorText by mutableStateOf<String?>(null)

    init {
        loadMyPosts()
    }

    fun loadMyPosts() {
        isLoading = true
        errorText = null
        viewModelScope.launch {
            PostRepository.getMyPosts(
                onSuccess = { fetchedList ->
                    isLoading = false
                    _myPosts.value = fetchedList
                },
                onError = { message ->
                    isLoading = false
                    errorText = message
                }
            )
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val isSuccess = PostRepository.deletePostById(postId)

            if (isSuccess) {
                _myPosts.value = _myPosts.value.filter { it.id != postId }
            }
        }
    }

}