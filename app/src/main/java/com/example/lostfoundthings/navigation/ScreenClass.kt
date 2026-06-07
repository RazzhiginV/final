package com.example.lostfoundthings.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ScreenClass(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    data object Posts: ScreenClass("posts", "Posts", Icons.Default.Home, "Posts")
    data object Detail: ScreenClass("detail/{postId}", "Detail", Icons.Default.Details, "Details")
    data object MyPosts: ScreenClass("myPosts", "MyPosts", Icons.Default.Add, "MyPosts")
    data object Chats: ScreenClass("chat/{itemId}/{receiverId}", "Chats", Icons.AutoMirrored.Filled.Send, "Chats")
    data object Profile: ScreenClass("profile", "Profile", Icons.Default.AccountCircle, "Profile")
    data object Register: ScreenClass("register", "Register", Icons.Default.AccountCircle, "Registration")
    data object Login: ScreenClass("login", "Login", Icons.Default.AccountCircle, "Login")
    data object Create: ScreenClass("create", "Create", Icons.Default.Add, "Create")
    data object ChatsList: ScreenClass("chatsList", "ChatsList", Icons.AutoMirrored.Filled.Send, "ChatsList")

}