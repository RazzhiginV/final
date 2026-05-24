package com.example.lostfoundthings.viewmodel

import androidx.lifecycle.ViewModel
import com.example.lostfoundthings.data.Post
import com.example.lostfoundthings.data.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PostsViewModel : ViewModel() {
    private val repository = PostRepository()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    init {
        loadPosts()
    }

    private fun loadPosts() {
        _posts.value = repository.getFakePosts()
    }
}