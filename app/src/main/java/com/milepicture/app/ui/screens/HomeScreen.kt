package com.milepicture.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.milepicture.app.data.api.ApiClient
import com.milepicture.app.data.model.UnifiedImage
import com.milepicture.app.ui.components.ImageCard
import com.milepicture.app.ui.components.SearchBarWithChips
import com.milepicture.app.ui.components.ShimmerGrid
import com.milepicture.app.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onImageClick: (UnifiedImage) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val selectedTagId by viewModel.selectedTagId.collectAsState()
    val images by viewModel.images.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showServerDialog by remember { mutableStateOf(false) }
    var serverUrlInput by remember { mutableStateOf(ApiClient.BASE_URL) }

    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = { Text("配置后端服务地址") },
            text = {
                Column {
                    Text(
                        "USB 直连填 http://127.0.0.1:3000/\n局域网填电脑 IP，如 http://192.168.1.5:3000/",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = serverUrlInput,
                        onValueChange = { serverUrlInput = it },
                        label = { Text("后端服务 URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showServerDialog = false
                    viewModel.updateServerUrl(serverUrlInput)
                }) {
                    Text("保存并连接")
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        SearchBarWithChips(
            query = query,
            onQueryChange = viewModel::onQueryChange,
            onSearch = viewModel::onSearchTriggered,
            tags = tags,
            selectedTagId = selectedTagId,
            onTagSelect = viewModel::onTagSelect,
            onFilterClick = onFilterClick
        )

        Box(modifier = Modifier.weight(1f)) {
            when {
                // 加载中 + 无数据 → 骨架屏动画
                isLoading && images.isEmpty() -> {
                    ShimmerGrid()
                }

                // 错误 + 无数据 → 错误重试
                errorMessage != null && images.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: "请求发生异常",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    serverUrlInput = ApiClient.BASE_URL
                                    showServerDialog = true
                                },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("修改服务器 IP")
                            }
                            Button(
                                onClick = { viewModel.loadInitialData() },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("重试连接")
                            }
                        }
                    }
                }

                // 正常显示瀑布流
                else -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(images, key = { it.id }) { img ->
                            ImageCard(
                                image = img,
                                onClick = onImageClick
                            )
                        }
                    }
                }
            }
        }
    }
}
