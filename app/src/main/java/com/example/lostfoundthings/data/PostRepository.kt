package com.example.lostfoundthings.data

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

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
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private const val SUPABASE_URL = "https://eklodbposzlperjomuwp.supabase.co/"
    private const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVrbG9kYnBvc3pscGVyam9tdXdwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzk4NTg4MDAsImV4cCI6MjA5NTQzNDgwMH0.vAX3_d6KGLcumbG0TdU6QrDH6IItnDPqSHrXFcws_q8"
    private const val BUCKET_NAME = "images"
    private val retrofit = Retrofit.Builder()
        .baseUrl(SUPABASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val supabaseStorageApi = retrofit.create(SupabaseStorageApi::class.java)

    fun getAllPosts(onSuccess: (List<Post>) -> Unit, onError: (String) -> Unit) {
        db.collection("items")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    return@addSnapshotListener onError(exception.message ?: "Ошибка")
                }

                val postsList = mutableListOf<Post>()
                snapshot?.documents?.forEach { document ->
                    val post = document.toObject(Post::class.java)
                    if (post != null) postsList.add(post)
                }
                onSuccess(postsList.sortedByDescending { it.timestamp })
            }
    }

    fun getMyPosts(onSuccess: (List<Post>) -> Unit, onError: (String) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return onError("Пользователь не авторизован")

        db.collection("items")
            .whereEqualTo("authorId", currentUid)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    onError(exception.message ?: "Ошибка загрузки ваших объявлений")
                    return@addSnapshotListener
                }

                val myPostsList = mutableListOf<Post>()
                snapshot?.documents?.forEach { document ->
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        myPostsList.add(post)
                    }
                }
                onSuccess(myPostsList.sortedByDescending { it.timestamp })
            }
    }


    fun createNewPost(
        title: String,
        description: String,
        address: String,
        lat: Double,
        lon: Double,
        photoUrl: String?,
        state: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser ?: return onError("Пользователь не авторизован")

        val postId = db.collection("items").document().id

        val newPost = Post(
            id = postId,
            title = title,
            description = description,
            photo = photoUrl,
            address = address,
            lat = lat,
            lon = lon,
            authorId = currentUser.uid,
            authorName = currentUser.displayName ?: "Аноним",
            authorPhoto = currentUser.photoUrl?.toString(),
            state = state,
            timestamp = System.currentTimeMillis()
        )

        db.collection("items").document(postId).set(newPost)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Ошибка создания объявления")
            }
    }

    suspend fun uploadImageToSupabase(imageBytes: ByteArray): String? {
        return try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

            val response = supabaseStorageApi.uploadFile(
                apiKey = "Bearer $ANON_KEY",
                apikeyHeader = ANON_KEY,
                bucket = BUCKET_NAME,
                path = fileName,
                file = requestBody
            )

            if (response.isSuccessful) {
                "${SUPABASE_URL}storage/v1/object/public/$BUCKET_NAME/$fileName"
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

}