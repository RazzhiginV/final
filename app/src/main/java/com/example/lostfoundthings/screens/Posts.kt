package com.example.lostfoundthings.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lostfoundthings.R
import com.example.lostfoundthings.viewmodel.PostsViewModel
import com.google.firebase.auth.FirebaseAuth


@Composable
fun PostsScreen(navController: NavController, viewModel: PostsViewModel = viewModel()) {
    val posts by viewModel.posts.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val filteredPosts = remember(posts, selectedTab) {
        when (selectedTab) {
            1 -> posts.filter { it.state == "lost" }
            2 -> posts.filter { it.state == "found" }
            else -> posts
        }
    }

    val onEdit: (String) -> Unit = { id -> navController.navigate("create/$id") }
    val onClick: (String) -> Unit = { id -> navController.navigate("detail/$id") }
    val onDeleteClick: (String) -> Unit = { id -> viewModel.deletePost(id) }

    val listState = rememberLazyListState()

    val shouldLoadNextPage by remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            lastVisibleItemIndex >= totalItemsCount - 2 && totalItemsCount > 0
        }
    }

    LaunchedEffect(shouldLoadNextPage) {
        if (shouldLoadNextPage && !viewModel.isEndReached && !viewModel.isLoading) {
            viewModel.loadNextPage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.isLoading && posts.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            PrimaryTabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth()) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Все") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Потеряно") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Найдено") })
            }
            PullToRefreshBox(
                isRefreshing = viewModel.isRefreshing,
                onRefresh = { viewModel.refreshPosts() },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.padding(top = 50.dp, bottom = 4.dp, start = 8.dp, end = 8.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    itemsIndexed(
                        items = filteredPosts,
                        key = { _, post -> post.id }
                    ) { index, post ->

                        PostCard(
                            post.title,
                            post.description,
                            post.photo,
                            post.address,
                            post.lat,
                            post.lon,
                            post.authorName,
                            post.authorId,
                            post.authorPhoto,
                            post.state,
                            post.timestamp,
                            { onEdit(post.id) },
                            { onClick(post.id) },
                            { onDeleteClick(post.id) }
                        )
                    }

                    if (viewModel.isLoading && posts.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(
    title: String,
    desc: String,
    url: String?,
    address: String,
    lat: Double,
    lon: Double,
    authorId: String,
    authorName: String,
    authorPhoto: String?,
    state: String,
    timestamp: Long,
    onEdit: () -> Unit,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.padding(8.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (!url.isNullOrBlank()) {
                AsyncImage(
                    model = url,
                    contentDescription = desc,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                AsyncImage(
                    model = authorPhoto,
                    fallback = painterResource(id = R.drawable.outline_account_circle_24),
                    contentDescription = desc,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(25.dp, 25.dp)
                        .clip(CircleShape)
                )

                Text(
                    title,
                    fontFamily = FontFamily.Default,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                Card(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state == "found") colorResource(R.color.found) else colorResource(R.color.lost)
                    ),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            painter = painterResource(if (state == "found") R.drawable.outline_check_small_24 else R.drawable.outline_close_small_24),
                            contentDescription = stringResource(R.string.check_icon)
                        )
                        Text(if (state == "found") stringResource(R.string.found) else stringResource(R.string.lost))
                    }
                }

                Card(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .width(30.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.grey)
                    )
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_forward_24),
                            contentDescription = stringResource(R.string.arrow),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (authorId == currentUserId) {
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedIconButton(
                        onClick = {
                            onEdit()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            ),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_post),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedIconButton(
                        onClick = {
                            onDeleteClick()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            ),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = stringResource(R.string.delete_post),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                }
            }
        }
    }
}