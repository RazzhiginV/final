package com.example.lostfoundthings.data

import androidx.compose.runtime.Immutable

@Immutable
data class ChatPreview(
    val chatId: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val itemPhoto: String? = null,
    val lastMessage: String = "",
    val senderId: String = "",
    val senderPhoto: String? = null,
    val receiverId: String = "",
    val receiverPhoto: String? = null,
    val timestamp: Long = 0L
)