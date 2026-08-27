package com.milepicture.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.milepicture.app.data.model.UnifiedImage
import com.milepicture.app.ui.screens.DetailScreen
import com.milepicture.app.ui.screens.FavoritesScreen
import com.milepicture.app.ui.screens.HomeScreen
import com.milepicture.app.ui.screens.ProfileScreen
import com.milepicture.app.ui.theme.MiLePictureTheme
import com.milepicture.app.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 开启现代 Android 全面屏沉浸式 Edge-to-Edge (完美自适应挖孔屏、灵动岛、刘海屏与平板)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MiLePictureTheme {
                var selectedTab by remember { mutableIntStateOf(0) }
                var activeDetailImage by remember { mutableStateOf<UnifiedImage?>(null) }
                val isDownloading by viewModel.isDownloading.collectAsState()

                if (activeDetailImage != null) {
                    val img = activeDetailImage!!
                    DetailScreen(
                        image = img,
                        isFavorite = viewModel.isFavorite(img.id),
                        isDownloading = isDownloading,
                        onFavoriteToggle = { viewModel.toggleFavorite(img) },
                        onDownload = { viewModel.downloadImage(img) },
                        onBack = { activeDetailImage = null }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp,
                                modifier = Modifier.navigationBarsPadding()
                            ) {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = {
                                        Icon(
                                            if (selectedTab == 0) Icons.Filled.Explore else Icons.Outlined.Explore,
                                            contentDescription = "探索"
                                        )
                                    },
                                    label = { Text("探索") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = {
                                        Icon(
                                            if (selectedTab == 1) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "收藏"
                                        )
                                    },
                                    label = { Text("收藏") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    icon = {
                                        Icon(
                                            if (selectedTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = "设置"
                                        )
                                    },
                                    label = { Text("设置") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                    ) { innerPadding ->
                        when (selectedTab) {
                            0 -> HomeScreen(
                                viewModel = viewModel,
                                onImageClick = { activeDetailImage = it },
                                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                            )
                            1 -> FavoritesScreen(
                                viewModel = viewModel,
                                onImageClick = { activeDetailImage = it },
                                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                            )
                            2 -> ProfileScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                            )
                        }
                    }
                }
            }
        }
    }
}
