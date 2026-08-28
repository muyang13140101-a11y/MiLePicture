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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 商业级 Shimmer 流光骨架屏
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    durationMillis: Int = 1100
) {
    val shimmerColors = listOf(
        Color(0xFF1E283D),
        Color(0xFF2E3D5C),
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
        modifier = modifier.background(brush)
    )
}

/**
 * 统一标准尺寸骨架卡片 (规整美观，支持自适应比例)
 */
@Composable
fun ShimmerImageCard(
    modifier: Modifier = Modifier,
    aspectRatio: Float? = null,
    height: Dp? = null
) {
    val cardModifier = when {
        aspectRatio != null -> modifier.fillMaxWidth().aspectRatio(aspectRatio).clip(RoundedCornerShape(16.dp))
        height != null -> modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(16.dp))
        else -> modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(16.dp))
    }

    ShimmerEffect(modifier = cardModifier)
}

/**
 * 规整统一的首屏骨架屏网格
 */
@Composable
fun ShimmerGrid(
    modifier: Modifier = Modifier
) {
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
                ShimmerImageCard(modifier = Modifier.weight(1f), height = 210.dp)
                ShimmerImageCard(modifier = Modifier.weight(1f), height = 210.dp)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
