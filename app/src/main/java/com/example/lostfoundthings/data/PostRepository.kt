package com.example.lostfoundthings.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


data class Post(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val photo: String? = null,
    val address: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val authorId: String = "",
    val authorName: String = "",
    val authorPhoto: String? = null,
    val state: String = "lost",
    val timestamp: Long = 0
)

object PostRepository {

    fun getAllPosts(onSuccess: (List<Post>) -> Unit, onError: (String) -> Unit) {
        val db = Firebase.firestore

        db.collection("items")
            .get()
            .addOnSuccessListener { snapshot ->
                val postsList = mutableListOf<Post>()
                for (document in snapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        postsList.add(post)
                    }
                }
                onSuccess(postsList.sortedByDescending { it.timestamp })
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Ошибка загрузки глобальной ленты")
            }
    }

}