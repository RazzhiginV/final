package com.example.lostfoundthings

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lostfoundthings.screens.ChatsScreen
import com.example.lostfoundthings.screens.CreateScreen
import com.example.lostfoundthings.screens.PostsScreen
import com.example.lostfoundthings.screens.ProfileScreen

@Composable
fun NavigationHost(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = ScreenClass.Posts.route
    ) {

        composable(
            route = ScreenClass.Posts.route
        ) {
            PostsScreen()
        }

        composable(
            route = ScreenClass.Create.route
        ) {
            CreateScreen()
        }

        composable(
            route = ScreenClass.Chats.route
        ) {
            ChatsScreen()
        }

        composable(
            route = ScreenClass.Profile.route
        ) {
            ProfileScreen()
        }

    }

}