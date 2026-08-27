package com.milepicture.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.milepicture.app.data.model.UnifiedImage
import com.milepicture.app.ui.components.ImageCard
import com.milepicture.app.ui.components.SourceBadge
import com.milepicture.app.ui.viewmodel.MainViewModel

/**
 * 商业级「多源专属收藏夹」板块
 * 自动按图库网站分发归类为各个独立文件夹（Unsplash 收藏夹、Pixabay 收藏夹、The Met 收藏夹等）
 */
@Composable
fun FavoritesScreen(
    viewModel: MainViewModel,
    onImageClick: (UnifiedImage) -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favorites.collectAsState()
    var openedFolderKey by remember { mutableStateOf<String?>(null) }

    // 文件夹元数据配置
    val folderDefs = listOf(
        Triple("all", "全部收藏", "汇总所有已保存的灵感作品"),
        Triple("unsplash", "Unsplash 摄影收藏夹", "Unsplash 原创高质量摄影"),
        Triple("pixabay", "Pixabay 插画摄影夹", "Pixabay 免版税矢量与摄影"),
        Triple("pexels", "Pexels 灵感收藏夹", "Pexels 唯美视觉摄影"),
        Triple("met", "The Met 世界名作夹", "纽约大都会博物馆 CC0 经典馆藏"),
        Triple("wallhaven", "Wallhaven 壁纸收藏夹", "4K/8K 极致画质动漫与数字艺术"),
        Triple("wikimedia", "维基共享档案夹", "维基媒体公有领域纪实与艺术"),
        Triple("bing", "必应 4K 壁纸夹", "微软必应每日全球风光摄影")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        if (openedFolderKey == null) {
            // === 一级视图：各大图库专属收藏夹卡片网格 (Folder View) ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Folders",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "我的收藏夹",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "共 ${favorites.size} 个内容",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(folderDefs) { (sourceKey, folderName, _) ->
                    val folderItems = if (sourceKey == "all") {
                        favorites
                    } else {
                        favorites.filter { it.source.equals(sourceKey, ignoreCase = true) }
                    }
                    val coverImage = folderItems.firstOrNull()

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openedFolderKey = sourceKey }
                    ) {
                        Column {
                            // 封面图 (Cover)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.33f)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                if (coverImage != null) {
                                    AsyncImage(
                                        model = coverImage.renditions.thumbnail,
                                        contentDescription = folderName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = "Empty",
                                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }

                                // 来源徽标
                                if (sourceKey != "all") {
                                    Box(modifier = Modifier.padding(6.dp).align(Alignment.TopStart)) {
                                        SourceBadge(source = sourceKey)
                                    }
                                }

                                // 底部渐变透明阴影
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                            )
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${folderItems.size} 个内容",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // 文件夹名称与描述
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = folderName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (folderItems.isNotEmpty()) "点击查看该源收藏" else "暂无作品 · 自动归类",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // === 二级视图：进入具体文件夹后的作品瀑布流 (Folder Detail View) ===
            val currentDef = folderDefs.firstOrNull { it.first == openedFolderKey }
            val currentFolderItems = if (openedFolderKey == "all") {
                favorites
            } else {
                favorites.filter { it.source.equals(openedFolderKey, ignoreCase = true) }
            }

            // 顶部返回栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { openedFolderKey = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentDef?.second ?: "收藏夹",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "共 ${currentFolderItems.size} 件作品",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (currentFolderItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "此收藏夹暂无内容",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "浏览探索页时收藏来自此网站的作品，会自动归类到这里",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentFolderItems, key = { it.id }) { img ->
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
