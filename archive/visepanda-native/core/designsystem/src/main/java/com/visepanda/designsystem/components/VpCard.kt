package com.visepanda.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.visepanda.designsystem.Background
import com.visepanda.designsystem.GlassGold
import com.visepanda.designsystem.GlassWarm
import com.visepanda.designsystem.GlassWhite
import com.visepanda.designsystem.Gold
import com.visepanda.designsystem.GoldLight
import com.visepanda.designsystem.JadeGrey
import com.visepanda.designsystem.Surface
import com.visepanda.designsystem.SurfaceElevated
import com.visepanda.designsystem.TextPrimary
import com.visepanda.designsystem.TextSecondary
import com.visepanda.designsystem.VisePandaElevation
import com.visepanda.designsystem.VisePandaShapes
import com.visepanda.designsystem.VisePandaSpacing

// ── Warm shadow colors (gold tones) ──
private val ShadowWarmLight = Color(0x0DC9A96E)
private val ShadowWarmMedium = Color(0x1AC9A96E)
private val ShadowWarmDeep = Color(0x30C9A96E)

// ── Enter Animation Wrapper (stagger-ready) ──

@Composable
fun VpEnterAnimation(
    visible: Boolean = true,
    index: Int = 0,
    staggerDelay: Int = 60,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400, delayMillis = index * staggerDelay)) +
                slideInVertically(
                    animationSpec = tween(400, delayMillis = index * staggerDelay),
                    initialOffsetY = { it / 3 }
                )
    ) {
        content()
    }
}

// ── VpCard Base — interactive card with warm shadows ──

@Composable
fun VpCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = VisePandaShapes.medium as RoundedCornerShape,
    containerColor: Color = Surface,
    elevation: Dp = VisePandaElevation.cardDefault,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(200),
        label = "cardScale"
    )
    val animElevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else elevation,
        animationSpec = tween(200),
        label = "cardElevation"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .scale(animScale)
            .shadow(
                elevation = animElevation,
                shape = shape,
                ambientColor = ShadowWarmLight,
                spotColor = ShadowWarmMedium
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        interactionSource = interactionSource
    ) {
        content()
    }
}

// ── VpCityCard — with warm shadow, image, tags ──

@Composable
fun VpCityCard(
    imageUrl: String?,
    cityName: String,
    description: String,
    tags: List<String> = emptyList(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Int = 200
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(200),
        label = "cityCardScale"
    )
    val animElevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 8.dp,
        animationSpec = tween(200),
        label = "cityCardElevation"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .scale(animScale)
            .shadow(
                elevation = animElevation,
                shape = VisePandaShapes.large as RoundedCornerShape,
                ambientColor = ShadowWarmMedium,
                spotColor = ShadowWarmDeep
            ),
        shape = VisePandaShapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        interactionSource = interactionSource
    ) {
        Box(modifier = Modifier.height(height.dp)) {
            // City image or gradient
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = cityName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(VisePandaShapes.large),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height.dp)
                        .clip(VisePandaShapes.large)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFDCC798),
                                    Color(0xFFC9A96E),
                                    Color(0xFFB89255)
                                )
                            )
                        )
                )
            }

            // Bottom gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xCC2D2D2D)
                            )
                        )
                    )
            )

            // City name + description
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = cityName,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }

            // Tags at top-right
            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.take(2).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(VisePandaShapes.extraSmall)
                                .background(GlassWhite)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tag,
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── VpTripCard — with warm shadow + gold accent ──

@Composable
fun VpTripCard(
    title: String,
    city: String,
    days: String,
    preview: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(200),
        label = "tripCardScale"
    )
    val animElevation by animateDpAsState(
        targetValue = if (isPressed) 3.dp else 6.dp,
        animationSpec = tween(200),
        label = "tripCardElevation"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .scale(animScale)
            .shadow(
                elevation = animElevation,
                shape = VisePandaShapes.large as RoundedCornerShape,
                ambientColor = ShadowWarmLight,
                spotColor = ShadowWarmMedium
            ),
        shape = VisePandaShapes.large,
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        interactionSource = interactionSource
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Gold left border accent
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(start = 0.dp)
                    .fillMaxWidth(0.012f)
                    .background(Gold)
            )
            Column(
                modifier = Modifier.padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = 16.dp
                )
            ) {
                Text(
                    text = title,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$city · $days",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = JadeGrey
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = preview,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2
                )
            }
        }
    }
}

// ── VpGlassCard — Frosted glass card with warm gold shimmer ──
// Use with Card for clickable, or standalone Box for non-interactive display.

@Composable
fun VpGlassCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = VisePandaShapes.medium as RoundedCornerShape,
    elevation: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(200),
        label = "glassScale"
    )

    if (onClick != null) {
        // Clickable version — uses Card for ripple + click
        Card(
            onClick = onClick,
            modifier = modifier
                .scale(animScale)
                .shadow(
                    elevation = if (isPressed) 2.dp else elevation,
                    shape = shape,
                    ambientColor = ShadowWarmLight,
                    spotColor = ShadowWarmMedium
                ),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            interactionSource = interactionSource
        ) {
            // Glass background layers (inside ColumnScope from Card)
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize().background(GlassGold)
            )
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize().background(GlassWarm)
            )
            content()
        }
    } else {
        // Non-interactive version — plain Box
        Box(
            modifier = modifier
                .shadow(
                    elevation = elevation,
                    shape = shape,
                    ambientColor = ShadowWarmLight,
                    spotColor = ShadowWarmMedium
                )
                .clip(shape)
                .background(GlassWarm)
        ) {
            Box(
                modifier = Modifier.matchParentSize().background(GlassGold)
            )
            content()
        }
    }
}
