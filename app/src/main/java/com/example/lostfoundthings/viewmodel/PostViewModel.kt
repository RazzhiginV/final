package com.example.lostfoundthings.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostfoundthings.data.Post
import com.example.lostfoundthings.data.PostRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostsViewModel : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    var posts: StateFlow<List<Post>> = _posts.asStateFlow()
    var isLoading by mutableStateOf(false)
    var isRefreshing by mutableStateOf(false)
    var errorText by mutableStateOf<String?>(null)
    var isEndReached by mutableStateOf(false)
    private var lastDocument: DocumentSnapshot? = null

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        if (isLoading || isEndReached) return

        isLoading = true
        errorText = null

        viewModelScope.launch {
            try {
                val (newPosts, nextDocument) = PostRepository.getAllPosts(lastDocument)

                if (newPosts.isEmpty()) {
                    isEndReached = true
                } else {
                    val currentList = ArrayList(_posts.value)
                    currentList.addAll(newPosts)
                    _posts.value = currentList
                    lastDocument = nextDocument
                }
            } catch (e: Exception) {
                errorText = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun refreshPosts() {
        _posts.value = emptyList()
        lastDocument = null
        isEndReached = false
        loadNextPage()
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val isSuccess = PostRepository.deletePostById(postId)

            if (isSuccess) {
                _posts.value = _posts.value.filter { it.id != postId }
            }
        }
    }

}