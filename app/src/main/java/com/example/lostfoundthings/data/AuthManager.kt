package com.example.lostfoundthings.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object AuthManager {
    val auth: FirebaseAuth get() = Firebase.auth
    val isUserLoggedIn: Boolean get() = auth.currentUser != null


    fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(translateFirebaseError(task.exception?.message))
                }
            }
    }

    fun signUpWithEmail(
        email: String,
        password: String,
        name: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }
                    auth.currentUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            onSuccess()
                        }
                } else {
                    onError(translateFirebaseError(task.exception?.message))
                }
            }
    }

    fun signInWithGoogle(
        context: Context,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val activityContext = context as? androidx.activity.ComponentActivity
            ?: return onError("Неверный контекст экрана")

        val credentialManager = CredentialManager.create(activityContext)

        val webClientId = "112702711436-cuopgbdvn98gt3qgtba0h45fi78957hm.apps.googleusercontent.com"

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context = activityContext, request = request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnSuccessListener { authResult ->
                            val user = authResult.user
                            val uid = user?.uid ?: ""

                            val newUser = authResult.additionalUserInfo?.isNewUser ?: false

                            if (newUser) {
                                createUserProfile(
                                    uid = uid,
                                    name = user?.displayName ?: "Пользователь Google",
                                    email = user?.email ?: ""
                                )
                            }
                            onSuccess()
                        }
                        .addOnFailureListener {
                            onError(translateFirebaseError(it.message))
                        }
                }
            } catch (e: Exception) {
                onError(translateFirebaseError(e.message))
            }
        }
    }

    fun translateFirebaseError(errorMessage: String?): String {
        if (errorMessage == null) return "Произошла неизвестная ошибка"
        return when {
            "badly formatted" in errorMessage ->
                "Неверный формат Email"
            ("user-not-found" in errorMessage || "no user record" in errorMessage) ->
                "Пользователь с таким Email не найден"
            ("wrong-password" in errorMessage || "invalid-credential" in errorMessage) ->
                "Неверный пароль или Email"
            ("email-already-in-use" in errorMessage || "already exists" in errorMessage || "email address is already in use" in errorMessage) ->
                "Этот Email уже зарегистрирован другим пользователем"
            "weak-password" in errorMessage ->
                "Слишком слабый пароль (нужно минимум 6 символов)"
            "network-request-failed" in errorMessage ->
                "Проблема с интернетом. Проверьте подключение"
            "cancelled" in errorMessage ->
                "Вход через Google отменен"
            else -> "Ошибка сервера"
        }
    }

    fun createUserProfile(uid: String, name: String, email: String) {
        val db = Firebase.firestore
        val profile = UserProfile(uid = uid, name = name, email = email)

        db.collection("users").document(uid).set(profile)
    }

    fun logout(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }
}