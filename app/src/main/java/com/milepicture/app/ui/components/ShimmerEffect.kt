package com.milepicture.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Shimmer 骨架屏加载动画组件
 * 用于 HomeScreen 首屏加载和 DetailScreen 大图加载时的优雅过渡
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    durationMillis: Int = 1200
) {
    val shimmerColors = listOf(
        Color(0xFF1E283D),
        Color(0xFF2A3654),
        Color(0xFF1E283D)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim.value - 200f, 0f),
        end = Offset(translateAnim.value + 200f, 300f)
    )

    Box(
        modifier = modifier
            .background(brush)
    )
}

/**
 * 瀑布流骨架屏 — 模拟图片卡片加载中的效果
 */
@Composable
fun ShimmerImageCard(
    modifier: Modifier = Modifier,
    aspectRatio: Float = 1.0f
) {
    ShimmerEffect(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(16.dp))
    )
}

/**
 * 首屏加载骨架屏网格 — 模拟瀑布流布局
 */
@Composable
fun ShimmerGrid(
    modifier: Modifier = Modifier
) {
    val ratios = listOf(1.2f, 0.8f, 0.9f, 1.3f, 1.0f, 0.75f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShimmerImageCard(
                    modifier = Modifier.weight(1f),
                    aspectRatio = ratios[row * 2]
                )
                ShimmerImageCard(
                    modifier = Modifier.weight(1f),
                    aspectRatio = ratios[row * 2 + 1]
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
