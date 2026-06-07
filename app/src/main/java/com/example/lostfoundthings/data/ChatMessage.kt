package com.example.lostfoundthings.data

import androidx.compose.runtime.Immutable

@Immutable
data class ChatMessage(
    val id: String = "",
    val itemId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderName: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = 0L
)