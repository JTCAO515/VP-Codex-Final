package com.visepanda.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.visepanda.designsystem.SurfaceDefault
import com.visepanda.designsystem.SurfaceElevated
import com.visepanda.designsystem.VisePandaSpacing

@Composable
fun VpShimmer(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    height: Int = 16
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            SurfaceElevated.copy(alpha = 0.6f),
            SurfaceElevated.copy(alpha = 1.0f),
            SurfaceElevated.copy(alpha = 0.6f)
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = 0f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(shimmerBrush)
    )
}

@Composable
fun VpCityCardShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDefault)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(SurfaceElevated.copy(alpha = 0.6f))
        )
        Column(modifier = Modifier.padding(VisePandaSpacing.lg)) {
            VpShimmer(widthFraction = 0.5f, height = 20)
            Spacer(modifier = Modifier.height(VisePandaSpacing.sm))
            VpShimmer(widthFraction = 0.8f, height = 14)
        }
    }
}

@Composable
fun VpSectionHeaderShimmer(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = VisePandaSpacing.lg)) {
        VpShimmer(widthFraction = 0.35f, height = 22)
        Spacer(modifier = Modifier.height(VisePandaSpacing.sm))
        VpShimmer(widthFraction = 0.55f, height = 14)
    }
}
