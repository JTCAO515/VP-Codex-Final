package com.visepanda.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.visepanda.core.designsystem.color.GoldDark
import com.visepanda.core.designsystem.color.SurfaceElevated

@Composable
fun VpShimmer(
    modifier: Modifier = Modifier,
    width: Float = 1.0f,
    height: Int = 16,
) {
    val transition = rememberInfiniteTransition()
    val shimmerTranslate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            SurfaceElevated,
            GoldDark.copy(alpha = 0.3f),
            SurfaceElevated,
        ),
        start = Offset(shimmerTranslate - 200f, 0f),
        end = Offset(shimmerTranslate, 0f),
    )

    Box(
        modifier = modifier
            .fillMaxWidth(fraction = width)
            .height(height.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(brush),
    )
}

@Composable
fun VpShimmerCard(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        VpShimmer(width = 0.6f, height = 20)
        Spacer(modifier = Modifier.height(8.dp))
        VpShimmer(width = 1.0f, height = 14)
        Spacer(modifier = Modifier.height(4.dp))
        VpShimmer(width = 0.8f, height = 14)
        Spacer(modifier = Modifier.height(4.dp))
        VpShimmer(width = 0.5f, height = 14)
    }
}
