package com.milepicture.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milepicture.app.data.model.UnifiedImage
import com.milepicture.app.ui.components.ImageCard
import com.milepicture.app.ui.viewmodel.MainViewModel

/**
 * 独立的收藏夹板块 — 支持按图库网站自动分类管理
 */
@Composable
fun FavoritesScreen(
    viewModel: MainViewModel,
    onImageClick: (UnifiedImage) -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favorites.collectAsState()
    var selectedSourceCategory by remember { mutableStateOf("all") }

    // 统计各来源收藏数量
    val sourceCounts = remember(favorites) {
        val map = mutableMapOf<String, Int>()
        map["all"] = favorites.size
        favorites.forEach { img ->
            val src = img.source.lowercase()
            map[src] = (map[src] ?: 0) + 1
        }
        map
    }

    val sourceCategories = listOf(
        "all" to "全部收藏",
        "unsplash" to "Unsplash",
        "pixabay" to "Pixabay",
        "pexels" to "Pexels",
        "met" to "The Met",
        "wallhaven" to "Wallhaven",
        "wikimedia" to "维基共享",
        "bing" to "必应壁纸"
    )

    // 过滤出当前分类的收藏作品
    val displayedFavorites = remember(favorites, selectedSourceCategory) {
        if (selectedSourceCategory == "all") {
            favorites
        } else {
            favorites.filter { it.source.equals(selectedSourceCategory, ignoreCase = true) }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // 顶部标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Folder",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "我的珍藏",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "共 ${favorites.size} 件作品",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 来源自动分类文件夹胶囊栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sourceCategories.forEach { (sourceKey, displayName) ->
                val count = sourceCounts[sourceKey] ?: 0
                val isSelected = selectedSourceCategory == sourceKey

                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSourceCategory = sourceKey },
                    label = {
                        Text(
                            text = if (count > 0) "$displayName ($count)" else displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        selected = isSelected,
                        enabled = true
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 内容展示区
        Box(modifier = Modifier.weight(1f)) {
            if (displayedFavorites.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (selectedSourceCategory == "all") "暂无收藏作品" else "该图库下暂无收藏",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "在探索页面浏览时点击收藏，将自动归类保存到此处",
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
                    items(displayedFavorites, key = { it.id }) { img ->
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
