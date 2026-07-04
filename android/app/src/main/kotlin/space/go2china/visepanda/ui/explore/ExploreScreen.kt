package space.go2china.visepanda.ui.explore

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import space.go2china.visepanda.data.explore.*
import space.go2china.visepanda.ui.theme.*

@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Attractions, 1: Food, 2: Stays

    val activeCity = uiState.cities.find { it.id == uiState.activeCityId }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Paper
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.loading && uiState.cities.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Cinnabar)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = Dimens.SpaceLG, vertical = Dimens.SpaceMD)
                ) {
                    // 1. Header (City Banner)
                    item {
                        CityBannerCard(city = activeCity)
                        Spacer(modifier = Modifier.height(Dimens.SpaceMD))
                    }

                    // 2. Provider status probe alert
                    item {
                        ProviderStatusAlert(status = uiState.providerStatus)
                        Spacer(modifier = Modifier.height(Dimens.SpaceMD))
                    }

                    // 3. City Selection row
                    item {
                        Text(
                            text = "Select Destination",
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                        CitySelectionRow(
                            cities = uiState.cities,
                            selectedCityId = uiState.activeCityId,
                            onCitySelected = { viewModel.selectCity(it) }
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpaceMD))
                    }

                    // 4. Tabs for POI categories
                    item {
                        PoiCategoryTabs(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpaceMD))
                    }

                    // 5. POI List
                    val pois: List<ExploreRichMeta> = when (selectedTab) {
                        0 -> uiState.attractions
                        1 -> uiState.foodSpots
                        else -> uiState.stays
                    }

                    if (pois.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimens.SpaceXL),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No content available for this category.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = InkSoft
                                )
                            }
                        }
                    } else {
                        items(pois) { poi ->
                            val name = when (poi) {
                                is ExploreAttraction -> poi.name
                                is ExploreFoodSpot -> poi.name
                                is ExploreStay -> poi.name
                                else -> ""
                            }
                            val id = when (poi) {
                                is ExploreAttraction -> poi.id
                                is ExploreFoodSpot -> poi.id
                                is ExploreStay -> poi.id
                                else -> ""
                            }
                            val description = when (poi) {
                                is ExploreAttraction -> poi.description
                                is ExploreFoodSpot -> poi.description
                                is ExploreStay -> poi.description
                                else -> ""
                            }
                            val categoryLabel = when (poi) {
                                is ExploreAttraction -> poi.category
                                is ExploreFoodSpot -> poi.dish
                                is ExploreStay -> poi.area
                                else -> ""
                            }
                            val categoryType = when (selectedTab) {
                                0 -> "attraction"
                                1 -> "food"
                                else -> "stay"
                            }

                            PoiCard(
                                name = name,
                                description = description,
                                categoryLabel = categoryLabel,
                                poi = poi,
                                icon = when (selectedTab) {
                                    0 -> Icons.Default.Museum
                                    1 -> Icons.Default.Restaurant
                                    else -> Icons.Default.Hotel
                                },
                                onAddToTrip = {
                                    viewModel.addToTrip(
                                        item = poi,
                                        cityName = activeCity?.name ?: "Beijing",
                                        name = name,
                                        category = categoryType,
                                        id = id,
                                        context = categoryLabel.ifBlank { null },
                                        onNavigateToChat = {
                                            Toast.makeText(context, "Added to Butler! Switch to Chat to check updates.", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                onSaveForLater = {
                                    viewModel.saveForLater(
                                        item = poi,
                                        cityName = activeCity?.name ?: "Beijing",
                                        name = name,
                                        category = categoryType,
                                        id = id,
                                        context = categoryLabel.ifBlank { null },
                                        onNavigateToChat = {
                                            Toast.makeText(context, "Saved to candidate list! Schedule it in Trips.", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpaceMD))
                        }
                    }

                    // 6. Clearance for floating bottom navigation bar
                    item {
                        Spacer(modifier = Modifier.height(Dimens.BottomNavContentClearance))
                    }
                }
            }
        }
    }
}

@Composable
private fun CityBannerCard(city: ExploreCity?) {
    if (city == null) return

    Surface(
        shape = RoundedCornerShape(Dimens.RadiusLG),
        color = SurfaceContainerLowest,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineVariantLight, RoundedCornerShape(Dimens.RadiusLG))
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpaceLG)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                )
                Surface(
                    shape = RoundedCornerShape(Dimens.RadiusPill),
                    color = SurfaceContainerLow,
                    modifier = Modifier.padding(start = Dimens.SpaceSM)
                ) {
                    Text(
                        text = city.region,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = InkSoft
                        ),
                        modifier = Modifier.padding(horizontal = Dimens.SpaceSM, vertical = Dimens.SpaceXS)
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpaceXS))
            Text(
                text = city.tagline,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic,
                    color = InkMuted
                )
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceMD))
            
            // Best for tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM),
                modifier = Modifier.fillMaxWidth()
            ) {
                city.bestFor.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(SurfaceContainer, RoundedCornerShape(Dimens.RadiusSM))
                            .border(0.5.dp, OutlineVariantLight, RoundedCornerShape(Dimens.RadiusSM))
                            .padding(horizontal = Dimens.SpaceSM, vertical = Dimens.SpaceXS)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = InkMuted
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderStatusAlert(status: ExploreProviderStatus?) {
    if (status == null) return

    val isStatic = status.mode == "static"
    val containerColor = if (isStatic) SecondaryContainerTint else TertiaryContainerTint
    val borderColor = if (isStatic) Gold else Sage
    val textColor = if (isStatic) Ink else OnTertiaryContainerDark

    Surface(
        shape = RoundedCornerShape(Dimens.RadiusMD),
        color = containerColor,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(Dimens.RadiusMD))
    ) {
        Row(
            modifier = Modifier.padding(Dimens.SpaceMD),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (isStatic) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isStatic) Gold else Sage,
                modifier = Modifier
                    .size(20.dp)
                    .offset(y = 2.dp)
            )
            Spacer(modifier = Modifier.width(Dimens.SpaceMD))
            Column {
                Text(
                    text = if (isStatic) "Offline Fallback Active" else "Amap Live Search Active",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isStatic) {
                        "VisePanda is currently showing static curated travel references because live POI search is offline."
                    } else {
                        "Amap live POI provider is active. Accessing real-time foreign-friendly coordinates."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = textColor.copy(alpha = 0.85f)
                    )
                )
            }
        }
    }
}

@Composable
private fun CitySelectionRow(
    cities: List<ExploreCity>,
    selectedCityId: String,
    onCitySelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(cities) { city ->
            val isSelected = city.id == selectedCityId
            val backColor = if (isSelected) Cinnabar else SurfaceContainerLow
            val borderCol = if (isSelected) Cinnabar else OutlineVariantLight
            val textCol = if (isSelected) PaperSoft else InkSoft

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.RadiusPill))
                    .background(backColor)
                    .border(1.dp, borderCol, RoundedCornerShape(Dimens.RadiusPill))
                    .clickable { onCitySelected(city.id) }
                    .padding(horizontal = Dimens.SpaceMD, vertical = Dimens.SpaceSM)
            ) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = textCol
                    )
                )
            }
        }
    }
}

@Composable
private fun PoiCategoryTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Transparent,
        contentColor = Cinnabar,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color = Cinnabar
            )
        },
        divider = {
            HorizontalDivider(color = OutlineVariantLight)
        }
    ) {
        Tab(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            text = { Text("Attractions", fontWeight = FontWeight.Bold) },
            selectedContentColor = Cinnabar,
            unselectedContentColor = InkSoft
        )
        Tab(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            text = { Text("Food Spots", fontWeight = FontWeight.Bold) },
            selectedContentColor = Cinnabar,
            unselectedContentColor = InkSoft
        )
        Tab(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            text = { Text("Stays", fontWeight = FontWeight.Bold) },
            selectedContentColor = Cinnabar,
            unselectedContentColor = InkSoft
        )
    }
}

@Composable
private fun PoiCard(
    name: String,
    description: String,
    categoryLabel: String,
    poi: ExploreRichMeta,
    icon: ImageVector,
    onAddToTrip: () -> Unit,
    onSaveForLater: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(Dimens.RadiusLG),
        color = SurfaceContainerLowest,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineVariantLight, RoundedCornerShape(Dimens.RadiusLG))
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpaceMD)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category Icon Box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SurfaceContainerLow, RoundedCornerShape(Dimens.RadiusMD))
                        .border(0.5.dp, OutlineVariantLight, RoundedCornerShape(Dimens.RadiusMD)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Cinnabar,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.SpaceMD))

                // POI core metadata
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = InkSoft
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceMD))

            // Rich Details row (Rating, Price, business hours)
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                poi.rating?.let { rating ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = rating,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                        )
                    }
                }

                poi.priceLevel?.let { priceLevel ->
                    Text(
                        text = priceLevel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Sage,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (!poi.openHours.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = InkSoft,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Open hours",
                            style = MaterialTheme.typography.labelSmall.copy(color = InkSoft)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Confidence badge
                Surface(
                    shape = RoundedCornerShape(Dimens.RadiusSM),
                    color = SurfaceContainerLow,
                    modifier = Modifier.border(0.5.dp, OutlineVariantLight, RoundedCornerShape(Dimens.RadiusSM))
                ) {
                    Text(
                        text = poi.confidence,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = InkSoft,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Fit Rationale block
            if (poi.fitRationale.isNotBlank()) {
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainerLow, RoundedCornerShape(Dimens.RadiusMD))
                        .padding(horizontal = Dimens.SpaceMD, vertical = Dimens.SpaceSM)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = InkSoft,
                            modifier = Modifier
                                .size(14.dp)
                                .offset(y = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpaceSM))
                        Text(
                            text = poi.fitRationale,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic,
                                color = InkMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceMD))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onSaveForLater,
                    shape = RoundedCornerShape(Dimens.RadiusPill),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Ink
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceSM))
                    Text("Save for Later")
                }

                Spacer(modifier = Modifier.width(Dimens.SpaceSM))

                Button(
                    onClick = onAddToTrip,
                    shape = RoundedCornerShape(Dimens.RadiusPill),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Cinnabar,
                        contentColor = PaperSoft
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceSM))
                    Text("Add to Trip")
                }
            }
        }
    }
}
