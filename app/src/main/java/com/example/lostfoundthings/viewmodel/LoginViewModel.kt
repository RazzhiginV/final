package com.example.lostfoundthings.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.lostfoundthings.data.AuthManager
import kotlinx.coroutines.CoroutineScope

class LoginViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    fun loginWithEmail(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onError("Заполните все поля")
            return
        }
        isLoading = true

        AuthManager.signInWithEmail(
            email = email.trim(),
            password = password.trim(),
            onSuccess = {
                isLoading = false
                onSuccess()
            },
            onError = { error ->
                isLoading = false
                onError(error)
            }
        )
    }

    fun loginWithGoogle(context: Context, scope: CoroutineScope, onSuccess: () -> Unit, onError: (String) -> Unit) {
        isLoading = true

        AuthManager.signInWithGoogle(
            context = context,
            scope = scope,
            onSuccess = {
                isLoading = false
                onSuccess()
            },
            onError = { error ->
                isLoading = false
                onError(error)
            }
        )
    }
}