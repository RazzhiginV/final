package com.example.lostfoundthings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lostfoundthings.ui.theme.LostFoundThingsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LostFoundThingsTheme {
                val navController = rememberNavController()
                val screens = listOf(ScreenClass.Posts, ScreenClass.Create, ScreenClass.Chats, ScreenClass.Profile)

                Scaffold(
                    modifier = Modifier.padding(top = 20.dp),
                    bottomBar = {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
                        ) {
                            NavigationBar(
                                Modifier.clip(shape = RoundedCornerShape(24.dp))
                                    .height(70.dp)
                            ) {

                                val backStackEntry by navController.currentBackStackEntryAsState()
                                val currentRoute = backStackEntry?.destination?.route
                                screens.forEach { screen ->
                                    NavigationBarItem(
                                        selected = screen.route == currentRoute,
                                        onClick = {
                                            navController.navigate(route = screen.route)
                                        },
                                        icon = {
                                            Icon(
                                                screen.icon,
                                                contentDescription = screen.contentDescription
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = screen.label,
                                                fontSize = 9.sp
                                            )
                                        }
                                    )
                                }

                            }
                        }
                    }
                ) { _ ->
                    NavigationHost(navController)
                }
            }
        }
    }
}