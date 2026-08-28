package com.milepicture.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.milepicture.app.data.model.UnifiedImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 商业级物理流体卡片 — 极速 120Hz 跟手微动效
 * 1. 物理弹簧阻尼按压缩放 (Bouncy Spring)
 * 2. 双击屏幕出现悬浮点赞粒子与触觉震动反馈
 * 3. 丝滑光影渐变遮罩
 */
@Composable
fun ImageCard(
    image: UnifiedImage,
    onClick: (UnifiedImage) -> Unit,
    onDoubleTapFavorite: ((UnifiedImage) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val ratio = image.aspectRatio?.coerceIn(0.5f, 1.8f) ?: 1.0f
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // 物理弹簧按压缩放状态
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardSpringScale"
    )

    // 双击点赞大红心浮层动效
    var showDoubleTapHeart by remember { mutableStateOf(false) }
    val heartAnimScale by animateFloatAsState(
        targetValue = if (showDoubleTapHeart) 1.25f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "doubleTapHeartScale"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
            .shadow(
                elevation = if (isPressed) 1.dp else 4.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick(image)
                    },
                    onDoubleTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDoubleTapFavorite?.invoke(image)
                        scope.launch {
                            showDoubleTapHeart = true
                            delay(650)
                            showDoubleTapHeart = false
                        }
                    }
                )
            }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            SubcomposeAsyncImage(
                model = image.renditions.thumbnail,
                contentDescription = image.altText ?: image.title,
                contentScale = ContentScale.Crop,
                loading = {
                    ShimmerImageCard(aspectRatio = ratio)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio)
                    .clip(RoundedCornerShape(18.dp))
            )

            // 左上角高质感磨砂来源徽标
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
            ) {
                SourceBadge(source = image.source)
            }

            // 双击点赞爆炸大红心浮层
            if (showDoubleTapHeart || heartAnimScale > 0.05f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.15f * heartAnimScale)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Liked",
                        tint = Color(0xFFFF2E63),
                        modifier = Modifier
                            .size(56.dp)
                            .scale(heartAnimScale)
                    )
                }
            }

            // 底部轻拟物渐变遮罩 + 精细文字
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.78f)
                            )
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = image.title ?: "精选视觉作品",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
