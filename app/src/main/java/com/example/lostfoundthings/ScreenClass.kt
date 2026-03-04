package com.example.lostfoundthings


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ScreenClass(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    data object Posts: ScreenClass("posts", "Posts", Icons.Default.Home, "Posts")
    data object Create: ScreenClass("create", "Create", Icons.Default.Edit, "Create")
    data object Chats: ScreenClass("chats", "Chats", Icons.AutoMirrored.Filled.Send, "Chats")
    data object Profile: ScreenClass("profile", "Profile", Icons.Default.AccountCircle, "Profile")
}