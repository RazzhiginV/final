package com.example.lostfoundthings.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.lostfoundthings.data.AuthManager

@Composable
fun ProfileScreen(navController: NavController) {
    Button(
        onClick = {
        AuthManager.logout(
            onSuccess = {
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        ) }
    ) {
        Text("Выйти из аккаунта")
    }

}