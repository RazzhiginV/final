package com.example.lostfoundthings.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostfoundthings.data.ChatPreview
import com.example.lostfoundthings.data.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatsListViewModel : ViewModel() {

    private val _chats = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chats: StateFlow<List<ChatPreview>> = _chats.asStateFlow()

    var isLoading by mutableStateOf(false)

    init {
        loadUserChats()
    }

    private fun loadUserChats() {
        isLoading = true
        viewModelScope.launch {
            ChatRepository.observeUserChats().collect { fetchedChats ->
                _chats.value = fetchedChats
                isLoading = false
            }
        }
    }
}