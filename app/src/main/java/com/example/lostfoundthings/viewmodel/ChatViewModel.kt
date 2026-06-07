package com.example.lostfoundthings.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostfoundthings.data.ChatMessage
import com.example.lostfoundthings.data.ChatRepository
import com.example.lostfoundthings.data.PostRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    var messageText by mutableStateOf("")
    var selectedChatImagePath by mutableStateOf<String?>(null)
    var isSending by mutableStateOf(false)
    private var currentItemId: String? = null

    fun initChat(itemId: String, senderId: String, receiverId: String) {
        val cleanChatId = ChatRepository.generateChatId(itemId, senderId, receiverId)

        if (currentItemId == cleanChatId) return
        currentItemId = cleanChatId

        viewModelScope.launch {
            ChatRepository.observeMessages(cleanChatId).collect { fetchedMessages ->
                _messages.value = fetchedMessages
            }
        }
    }

    fun sendMessage(
        context: Context,
        itemId: String,
        receiverId: String,
        itemTitle: String,
        itemPhoto: String?,
        authorPhoto: String?,
        onError: (String) -> Unit
    ) {
        val textToSend = messageText.trim()
        val imagePath = selectedChatImagePath

        if (textToSend.isBlank() && imagePath == null) return

        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        isSending = true

        viewModelScope.launch {
            try {
                var serverImageUrl: String? = null

                if (imagePath != null) {
                    val imageBytes: ByteArray? = withContext(Dispatchers.IO) {
                        try {
                            if (imagePath.startsWith("content://")) {
                                val uri = Uri.parse(imagePath)
                                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            } else {
                                val cleanPath = if (imagePath.startsWith("file:")) imagePath.substringAfter("file:") else imagePath
                                val file = File(cleanPath)
                                if (file.exists()) file.readBytes() else null
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }

                    if (imageBytes != null) {
                        serverImageUrl = PostRepository.uploadImageToSupabase(imageBytes)
                    }
                }

                val newMessage = ChatMessage(
                    itemId = itemId,
                    senderId = currentUser.uid,
                    receiverId = receiverId,
                    senderName = currentUser.displayName ?: "Пользователь",
                    text = textToSend,
                    imageUrl = serverImageUrl,
                    timestamp = System.currentTimeMillis()
                )

                messageText = ""
                selectedChatImagePath = null

                val success = ChatRepository.sendMessageAndWithPreview(
                    message = newMessage,
                    itemTitle = itemTitle,
                    itemPhoto = itemPhoto,
                    receiverPhoto = authorPhoto
                )
                if (!success) onError("Не удалось отправить сообщение")

            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Ошибка отправки")
            } finally {
                isSending = false
            }
        }
    }

}