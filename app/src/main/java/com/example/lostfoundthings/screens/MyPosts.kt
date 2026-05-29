package com.example.lostfoundthings.screens

import androidx.compose.material3.Text
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lostfoundthings.navigation.ScreenClass
import com.example.lostfoundthings.viewmodel.CreatePostViewModel
import com.example.lostfoundthings.viewmodel.MyPostsViewModel

@Composable
fun MyPostsScreen(navController: NavController) {
    val viewModel: MyPostsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val context = LocalContext.current
    val activity = LocalContext.current as ComponentActivity
    val createPostViewModel: CreatePostViewModel = viewModel(activity)

    val myPosts by viewModel.myPosts.collectAsState()

    viewModel.errorText?.let {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        viewModel.errorText = null
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    createPostViewModel.postId = null
                    navController.navigate("create")
                          },
                modifier = Modifier.padding(bottom = 72.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Создать пост")
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (myPosts.isEmpty()) {
                Text(text = "Вы еще не создали ни одного поста", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(
                        items = myPosts,
                        key = { post -> post.id }
                    ) { post ->
                        PostCard(
                            post.title,
                            post.description,
                            post.photo,
                            post.address,
                            post.lat,
                            post.lon,
                            post.authorId,
                            post.authorName,
                            post.authorPhoto,
                            post.state,
                            post.timestamp,
                            {
                                createPostViewModel.postId = post.id
                                navController.navigate(ScreenClass.Create.route)
                            },
                            {navController.navigate("detail/${post.id}")},
                            { viewModel.deletePost(post.id) }
                        )
                    }
                }
            }
        }
    }
}