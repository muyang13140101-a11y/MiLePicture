package com.milepicture.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * 商业级 120Hz 爱心粒子爆炸微动效 (Particle Burst)
 * 点击收藏或双击点赞时触发物理级粒子向外迸发与红心果冻回弹
 */
@Composable
fun HeartBurstEffect(
    isFavorite: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(targetState = isFavorite, label = "HeartBurst")

    // 红心回弹缩放
    val heartScale by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                keyframes {
                    durationMillis = 400
                    0.6f at 50 using FastOutSlowInEasing
                    1.35f at 220 using FastOutLinearInEasing
                    1.0f at 400 using FastOutSlowInEasing
                }
            } else {
                tween(durationMillis = 200)
            }
        },
        label = "HeartScale"
    ) { fav ->
        if (fav) 1f else 1f
    }

    // 粒子爆炸进度 (0f -> 1f)
    val burstProgress by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                tween(durationMillis = 450, easing = FastOutSlowInEasing)
            } else {
                snap()
            }
        },
        label = "BurstProgress"
    ) { fav ->
        if (fav) 1f else 0f
    }

    val particleColors = remember {
        listOf(
            Color(0xFFFF2E63),
            Color(0xFFFF9A3C),
            Color(0xFF00EAD3),
            Color(0xFFFFD369),
            Color(0xFFE94560),
            Color(0xFF9D4EDD),
            Color(0xFFFF6B6B),
            Color(0xFF48CAE4)
        )
    }

    Box(
        modifier = modifier.size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        // 粒子迸发层 (仅在点赞瞬间触发 450ms 绚丽粒子)
        if (burstProgress in 0.01f..0.99f && isFavorite) {
            Canvas(modifier = Modifier.size(54.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = size.width * 0.45f
                val currentRadius = maxRadius * burstProgress
                val particleAlpha = (1f - burstProgress).coerceIn(0f, 1f)

                for (i in 0 until 8) {
                    val angle = Math.toRadians((i * 45.0) + (burstProgress * 15.0))
                    val px = center.x + (currentRadius * cos(angle)).toFloat()
                    val py = center.y + (currentRadius * sin(angle)).toFloat()
                    val color = particleColors[i % particleColors.size].copy(alpha = particleAlpha)
                    val particleSize = (3.5f * (1f - burstProgress * 0.5f)).dp.toPx()

                    drawCircle(
                        color = color,
                        radius = particleSize,
                        center = Offset(px, py)
                    )
                }
            }
        }

        // 核心心形图标 (果冻回弹)
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Favorite",
            tint = if (isFavorite) Color(0xFFF43F5E) else Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier
                .size(22.dp)
                .scale(heartScale)
        )
    }
}
