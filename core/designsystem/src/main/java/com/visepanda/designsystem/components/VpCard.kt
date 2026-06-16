package com.visepanda.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.visepanda.designsystem.Gold
import com.visepanda.designsystem.JadeGrey
import com.visepanda.designsystem.Surface
import com.visepanda.designsystem.TextPrimary
import com.visepanda.designsystem.TextSecondary
import com.visepanda.designsystem.VisePandaSpacing

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
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.height(height.dp)) {
            // City image (loaded from URL) or gradient fallback
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = cityName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFDCC798),
                                    Color(0xFFC9A96E)
                                )
                            )
                        )
                )
            }

            // Dark overlay for readability on both images and gradients
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xBB2D2D2D)
                            )
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(VisePandaSpacing.lg)
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
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun VpTripCard(
    title: String,
    city: String,
    days: String,
    preview: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Gold left border accent
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(start = 0.dp)
                    .fillMaxWidth(0.006f)
                    .background(Gold)
            )
            // Content
            Column(
                modifier = Modifier.padding(
                    start = VisePandaSpacing.lg,
                    end = VisePandaSpacing.lg,
                    top = VisePandaSpacing.md,
                    bottom = VisePandaSpacing.md
                )
            ) {
                Text(
                    text = title,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$city · $days",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = JadeGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
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
