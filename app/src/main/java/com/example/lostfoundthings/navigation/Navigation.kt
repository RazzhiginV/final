package com.example.lostfoundthings.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.lostfoundthings.data.AuthManager
import com.example.lostfoundthings.screens.ChatScreen
import com.example.lostfoundthings.screens.ChatsListScreen
import com.example.lostfoundthings.screens.CreatePostScreen
import com.example.lostfoundthings.screens.MyPostsScreen
import com.example.lostfoundthings.screens.LoginScreen
import com.example.lostfoundthings.screens.PostDetailScreen
import com.example.lostfoundthings.screens.PostsScreen
import com.example.lostfoundthings.screens.ProfileScreen
import com.example.lostfoundthings.screens.RegisterScreen

@Composable
fun NavigationHost(navController: NavHostController, modifier: Modifier = Modifier) {

    NavHost(
        navController = navController,
        startDestination = if (AuthManager.isUserLoggedIn) "posts" else "login",
        modifier = Modifier
    ) {

        composable(
            route = ScreenClass.Login.route
        ) {
            LoginScreen(navController)
        }

        composable(
            route = ScreenClass.Register.route
        ) {
            RegisterScreen(navController)
        }

        composable(
            route = ScreenClass.Posts.route
        ) {
            PostsScreen(navController)
        }

        composable(
            route = ScreenClass.Create.route
        ) {
            CreatePostScreen(navController = navController)
        }

        composable(
            route = ScreenClass.Detail.route,
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType }
            )
        ) {
            PostDetailScreen(navController)
        }

        composable(
            route = ScreenClass.MyPosts.route
        ) {
            MyPostsScreen(navController)
        }

        composable(
            route = ScreenClass.Profile.route
        ) {
            ProfileScreen(navController)
        }

        composable(route = ScreenClass.ChatsList.route) {
            ChatsListScreen(navController = navController)
        }

        composable(route = ScreenClass.Chats.route) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""

            ChatScreen(
                navController = navController,
                itemId = itemId,
                receiverId = receiverId
            )
        }

    }

}