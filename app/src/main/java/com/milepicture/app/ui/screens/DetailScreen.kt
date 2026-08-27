package com.milepicture.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.milepicture.app.data.model.UnifiedImage
import com.milepicture.app.ui.components.ShimmerEffect
import com.milepicture.app.ui.components.SourceBadge
import com.milepicture.app.utils.WallpaperHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    image: UnifiedImage,
    isFavorite: Boolean,
    isDownloading: Boolean,
    onFavoriteToggle: () -> Unit,
    onDownload: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var showInfo by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var isSettingWallpaper by remember { mutableStateOf(false) }

    // 手势无级缩放状态
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    if (showWallpaperDialog) {
        AlertDialog(
            onDismissRequest = { showWallpaperDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Wallpaper, contentDescription = "Wallpaper", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("设为壁纸")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("请选择要应用的目标屏幕：", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            showWallpaperDialog = false
                            val wallUrl = image.renditions.large ?: image.renditions.preview ?: image.renditions.thumbnail
                            scope.launch {
                                WallpaperHelper.setWallpaper(
                                    context = context,
                                    imageUrl = wallUrl,
                                    target = WallpaperHelper.TargetScreen.HOME,
                                    onProgress = { isSettingWallpaper = it },
                                    onResult = { _, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("设为主屏幕桌面")
                    }

                    FilledTonalButton(
                        onClick = {
                            showWallpaperDialog = false
                            val wallUrl = image.renditions.large ?: image.renditions.preview ?: image.renditions.thumbnail
                            scope.launch {
                                WallpaperHelper.setWallpaper(
                                    context = context,
                                    imageUrl = wallUrl,
                                    target = WallpaperHelper.TargetScreen.LOCK,
                                    onProgress = { isSettingWallpaper = it },
                                    onResult = { _, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("设为锁屏壁纸")
                    }

                    Button(
                        onClick = {
                            showWallpaperDialog = false
                            val wallUrl = image.renditions.large ?: image.renditions.preview ?: image.renditions.thumbnail
                            scope.launch {
                                WallpaperHelper.setWallpaper(
                                    context = context,
                                    imageUrl = wallUrl,
                                    target = WallpaperHelper.TargetScreen.BOTH,
                                    onProgress = { isSettingWallpaper = it },
                                    onResult = { _, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("同时设为桌面与锁屏", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showWallpaperDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    var isSharing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 全安卓版本真图片格式分享
                    IconButton(
                        onClick = {
                            val shareImgUrl = image.renditions.preview ?: image.renditions.large ?: image.renditions.thumbnail
                            scope.launch {
                                com.milepicture.app.utils.ImageShareHelper.shareImage(
                                    context = context,
                                    imageUrl = shareImgUrl,
                                    title = image.title ?: "精选高清大图",
                                    landingUrl = image.landingPageUrl,
                                    onProgress = { isSharing = it },
                                    onResult = { success, err ->
                                        if (!success && err != null) {
                                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        },
                        enabled = !isSharing
                    ) {
                        if (isSharing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(Icons.Default.Share, contentDescription = "分享图片")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. 收藏按钮
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onFavoriteToggle()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isFavorite) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "收藏",
                            tint = if (isFavorite) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFavorite) "已收藏" else "收藏",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // 2. 设为壁纸按钮
                    FilledTonalButton(
                        onClick = { showWallpaperDialog = true },
                        enabled = !isSettingWallpaper,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        if (isSettingWallpaper) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(Icons.Default.Wallpaper, contentDescription = "设为壁纸", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("设为壁纸", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // 3. RAW 无损保存按钮（主操作）
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDownload()
                        },
                        enabled = !isDownloading,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("无损保存中...")
                        } else {
                            Icon(Icons.Default.Download, contentDescription = "下载", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("原图保存", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // 沉浸式大图（支持手势捏合双击放大与平移）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            if (scale > 1f) {
                                offset += pan
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    }
            ) {
                SubcomposeAsyncImage(
                    model = image.renditions.preview ?: image.renditions.large ?: image.renditions.thumbnail,
                    contentDescription = image.title,
                    contentScale = ContentScale.Fit,
                    loading = {
                        ShimmerEffect(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 280.dp, max = 480.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 280.dp, max = 520.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 标题 + 来源
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SourceBadge(source = image.source)
                    Text(
                        text = image.title ?: "Untitled",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 标签
                if (image.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        image.tags.take(5).forEach { tag ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(tag, fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 「作品信息与版权声明」可折叠面板
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showInfo = !showInfo }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "作品信息与授权协议",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Icon(
                                imageVector = if (showInfo) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "展开",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(
                            visible = showInfo,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                // 创作者信息
                                image.creator.name?.let { creatorName ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = creatorName.firstOrNull()?.uppercase() ?: "A",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = creatorName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "原作者 / 摄影师",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                // 许可证信息
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = "License",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = image.license.code ?: image.license.licenseClass,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                image.license.attributionText?.let { attr ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = attr,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // 访问原站
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(image.landingPageUrl))
                                        context.startActivity(intent)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.OpenInBrowser, contentDescription = "打开原站", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("访问原站查看高清详情", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
