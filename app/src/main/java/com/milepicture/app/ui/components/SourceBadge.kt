package com.milepicture.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milepicture.app.ui.theme.*

@Composable
fun SourceBadge(source: String, modifier: Modifier = Modifier) {
    val (bgColor, displayName) = when (source.lowercase()) {
        "openverse" -> BadgeOpenverse to "Openverse"
        "met" -> BadgeTheMet to "The Met"
        "wikimedia" -> BadgeWikimedia to "Wikimedia"
        "unsplash" -> BadgeUnsplash to "Unsplash"
        "pixabay" -> BadgePixabay to "Pixabay"
        "wallhaven" -> BadgeWallhaven to "Wallhaven"
        "pexels" -> BadgePexels to "Pexels"
        else -> Color(0xFF475569) to source.replaceFirstChar { it.uppercase() }
    }

    Box(
        modifier = modifier
            .background(bgColor.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = displayName,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
