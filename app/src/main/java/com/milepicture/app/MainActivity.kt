package com.milepicture.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.milepicture.app.data.model.UnifiedImage
import com.milepicture.app.ui.screens.DetailScreen
import com.milepicture.app.ui.screens.HomeScreen
import com.milepicture.app.ui.screens.ProfileScreen
import com.milepicture.app.ui.theme.MiLePictureTheme
import com.milepicture.app.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiLePictureTheme {
                var selectedTab by remember { mutableIntStateOf(0) }
                var activeDetailImage by remember { mutableStateOf<UnifiedImage?>(null) }
                var showFilterDialog by remember { mutableStateOf(false) }
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
                                tonalElevation = 0.dp
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
                                            if (selectedTab == 1) Icons.Filled.Person else Icons.Outlined.Person,
                                            contentDescription = "我的"
                                        )
                                    },
                                    label = { Text("我的") },
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
                                onFilterClick = { showFilterDialog = true },
                                modifier = Modifier.padding(innerPadding)
                            )
                            1 -> ProfileScreen(
                                viewModel = viewModel,
                                onImageClick = { activeDetailImage = it },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        if (showFilterDialog) {
                            AlertDialog(
                                onDismissRequest = { showFilterDialog = false },
                                title = { Text("筛选设置") },
                                text = {
                                    val onlyPd by viewModel.onlyPublicDomain.collectAsState()
                                    Column {
                                        Text("仅展示公有领域 (CC0 / Public Domain):")
                                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = onlyPd,
                                                onCheckedChange = { viewModel.togglePublicDomainOnly() }
                                            )
                                            Text(if (onlyPd) "已开启" else "未开启")
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showFilterDialog = false }) {
                                        Text("确定")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
