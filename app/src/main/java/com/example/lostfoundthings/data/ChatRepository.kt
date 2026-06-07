package com.example.lostfoundthings.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object ChatRepository {

    fun generateChatId(itemId: String, senderId: String, receiverId: String): String {
        val participants = listOf(senderId, receiverId).sorted()
        return "${itemId}_${participants[0]}_${participants[1]}"
    }

    suspend fun sendMessageAndWithPreview(message: ChatMessage, itemTitle: String, itemPhoto: String?, receiverPhoto: String?): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()

            val chatId = generateChatId(message.itemId, message.senderId, message.receiverId)
            val finalMessage = message.copy(chatId = chatId)

            db.collection("chats").add(finalMessage).await()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val preview = ChatPreview(
                        chatId = chatId,
                        itemId = message.itemId,
                        itemTitle = itemTitle,
                        itemPhoto = itemPhoto,
                        lastMessage = message.text.ifBlank { "Фотография" },
                        senderId = message.senderId,
                        senderPhoto = FirebaseAuth.getInstance().currentUser?.photoUrl?.toString(),
                        receiverId = message.receiverId,
                        receiverPhoto = receiverPhoto,
                        timestamp = message.timestamp
                    )
                    db.collection("chat_previews").document(chatId).set(preview).await()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun observeUserChats(): Flow<List<ChatPreview>> = callbackFlow {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val listener = FirebaseFirestore.getInstance()
            .collection("chat_previews")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val previews = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatPreview::class.java)
                    }.filter { it.senderId == currentUid || it.receiverId == currentUid } // 🎯 Чистая NoSQL фильтрация по полям!

                    trySend(previews)
                }
            }

        awaitClose { listener.remove() }
    }

    fun observeMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = FirebaseFirestore.getInstance()
            .collection("chats")
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messagesList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                    }
                    trySend(messagesList)
                }
            }
        awaitClose { listener.remove() }
    }
}