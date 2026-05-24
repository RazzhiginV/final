package com.example.lostfoundthings.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.lostfoundthings.data.AuthManager
import kotlinx.coroutines.CoroutineScope

class RegisterViewModel : ViewModel() {
    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    fun registerWithEmail(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            onError("Заполните все поля")
            return
        }
        isLoading = true

        AuthManager.signUpWithEmail(
            email = email.trim(),
            password = password.trim(),
            name = name.trim(),
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

    fun registerWithGoogle(context: Context, scope: CoroutineScope, onSuccess: () -> Unit, onError: (String) -> Unit) {
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