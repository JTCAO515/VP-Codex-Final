package space.go2china.visepanda.ui.explore

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import space.go2china.visepanda.R
import space.go2china.visepanda.data.explore.*

private val BrandGreen = Color(0xFF2E7D32)
private val PillBg = Color(0xFFF0F4F0)
private val ActivePill = BrandGreen

@Composable
fun ExploreScreen(
    onAskButler: (String) -> Unit = {},
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ── Location helpers ──────────────────────
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    @SuppressLint("MissingPermission")
    fun fetchLocation(onResult: (String?) -> Unit) {
        val cts = CancellationTokenSource()
        fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
            .addOnSuccessListener { loc: Location? ->
                onResult(loc?.let { "${it.longitude},${it.latitude}" })
            }
            .addOnFailureListener { onResult(null) }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
                || permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        locationGranted = granted
        if (granted) {
            scope.launch { fetchLocation { loc -> viewModel.onLocationUpdate(loc, true) } }
        } else {
            viewModel.onLocationUpdate(null, false)
        }
    }

    // ── request location when entering a channel ──
    fun enterChannel(category: ExploreCategory, focus: ExploreFocusTarget? = null) {
        if (locationGranted) {
            scope.launch {
                fetchLocation { loc ->
                    viewModel.navigateToChannel(category, true, loc, focus?.amapPoiId, focus?.cityId)
                }
            }
        } else {
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
            viewModel.navigateToChannel(category, false, null, focus?.amapPoiId, focus?.cityId)
        }
    }

    // ── Chat↔Explore bridge: enter a channel directly when opened from an
    // exploreRef card instead of showing Home (Issue #59). ──
    LaunchedEffect(Unit) {
        viewModel.consumePendingFocus()?.let { focus ->
            enterChannel(focus.category, focus)
        }
    }

    // ── Back handler ──────────────────────────
    val isChannel = uiState is ExploreUiState.Channel
    if (isChannel) {
        androidx.activity.compose.BackHandler { viewModel.navigateBack() }
    }

    // ── Render ────────────────────────────────
    when (val s = uiState) {
        is ExploreUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator(color = BrandGreen)
        }

        is ExploreUiState.Home -> ExploreHomeContent(
            state = s,
            onCitySelect = viewModel::selectCity,
            onCategoryClick = { category -> enterChannel(category) },
        )

        is ExploreUiState.Channel -> ExploreChannelContent(
            state = s,
            onBack = viewModel::navigateBack,
            onCitySelect = viewModel::selectCity,
            onTogglePanel = viewModel::toggleFilterPanel,
            onDismissPanel = viewModel::dismissFilterPanel,
            onProximity = viewModel::applyProximity,
            onSubcategory = viewModel::applySubcategory,
            onSort = viewModel::applySort,
            onApplyMore = viewModel::applyMoreFilters,
            onLoadMore = viewModel::loadNextPage,
            onAddToTrip = { poi -> viewModel.addToTrip(poi, 1) },
            onAskButler = onAskButler,
            onDismissAdded = viewModel::dismissAddedNotice,
            onDismissNotice = viewModel::dismissLocationNotice,
        )
    }
}

// ══════════════════════════════════════════════════
// Home Screen
// ══════════════════════════════════════════════════

@Composable
private fun ExploreHomeContent(
    state: ExploreUiState.Home,
    onCitySelect: (String) -> Unit,
    onCategoryClick: (ExploreCategory) -> Unit,
) {
    var showCitySheet by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── top bar ──
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { showCitySheet = true },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            ) {
                val city = SUPPORTED_CITIES.find { it.id == state.selectedCityId }
                Text(
                    city?.nameEn ?: state.selectedCityId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.weight(1f))
        }

        // ── category nav ──
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ExploreCategory.entries.forEach { cat ->
                CategoryNavItem(cat, onClick = { onCategoryClick(cat) })
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))

        // ── UGC feed label ──
        Text(
            "Popular in ${SUPPORTED_CITIES.find { it.id == state.selectedCityId }?.nameEn ?: ""}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        // ── UGC mock feed — double column ──
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(state.ugcFeed, key = { it.id }) { item ->
                UgcCard(item)
            }
        }
    }

    if (showCitySheet) {
        CitySelectionSheet(
            cities = state.cities,
            selectedId = state.selectedCityId,
            onSelect = { id ->
                onCitySelect(id)
                showCitySheet = false
            },
            onDismiss = { showCitySheet = false },
        )
    }
}

@Composable
private fun CategoryNavItem(category: ExploreCategory, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
    ) {
        Text(category.emoji, fontSize = 28.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            category.labelEn,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun UgcCard(item: UgcFeedItem) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
            )
            Column(Modifier.padding(8.dp)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.authorNick,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Icon(Icons.Default.Favorite, null, tint = Color(0xFFE57373), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(
                        "${item.likeCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════
// Channel Page
// ══════════════════════════════════════════════════

@Composable
private fun ExploreChannelContent(
    state: ExploreUiState.Channel,
    onBack: () -> Unit,
    onCitySelect: (String) -> Unit,
    onTogglePanel: (FilterPanelOpen) -> Unit,
    onDismissPanel: () -> Unit,
    onProximity: (ProximityFilter) -> Unit,
    onSubcategory: (ExploreSubcategory) -> Unit,
    onSort: (SortMode) -> Unit,
    onApplyMore: (Set<PriceFilter>, Boolean) -> Unit,
    onLoadMore: () -> Unit,
    onAddToTrip: (ExplorePoi) -> Unit,
    onAskButler: (String) -> Unit,
    onDismissAdded: () -> Unit,
    onDismissNotice: () -> Unit,
) {
    var showCitySheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val filters = state.filters

    // Draft state for "More" multi-select panel
    var draftPrices by remember(filters.prices) { mutableStateOf(filters.prices.toMutableSet()) }
    var draftRatedOnly by remember(filters.ratedOnly) { mutableStateOf(filters.ratedOnly) }

    // Observe scroll to trigger load-more
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= listState.layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) onLoadMore()
    }

    // Chat↔Explore bridge (Issue #59): scroll to the POI a Butler exploreRef
    // card pointed at, once it's actually present in a loaded page. Best-
    // effort only — never fabricates a card if the id never shows up.
    val focusIndex = state.focusPoiId?.let { id -> state.pois.indexOfFirst { it.id == "amap-$id" } }
    LaunchedEffect(focusIndex, state.pois) {
        if (focusIndex != null && focusIndex >= 0) {
            listState.animateScrollToItem(focusIndex)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // ── top bar ──
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    state.category.labelEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { showCitySheet = true }) {
                    val city = SUPPORTED_CITIES.find { it.id == state.selectedCityId }
                    Text(
                        city?.nameEn ?: state.selectedCityId,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            // ── filter bar (sticky) ──
            FilterBar(
                filters = filters,
                locationGranted = state.locationGranted,
                onToggle = onTogglePanel,
            )

            HorizontalDivider()

            // ── location notice ──
            if (state.locationUnavailableNotice) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF3E0))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.LocationOff, null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Location off — showing whole city results",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE65100),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismissNotice, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // ── POI list ──
            Box(Modifier.weight(1f)) {
                when {
                    state.isLoading && state.pois.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            CircularProgressIndicator(color = BrandGreen)
                        }
                    }
                    !state.isLoading && state.pois.isEmpty() && state.errorNotice != null -> {
                        Column(
                            Modifier.fillMaxSize(),
                            Arrangement.Center,
                            Alignment.CenterHorizontally,
                        ) {
                            Icon(Icons.Default.WifiOff, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(state.errorNotice, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                    else -> {
                        LazyColumn(state = listState, contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(state.pois, key = { it.id }) { poi ->
                                PoiCard(
                                    poi = poi,
                                    isFocused = state.focusPoiId != null && poi.id == "amap-${state.focusPoiId}",
                                    onAddToTrip = { onAddToTrip(poi) },
                                    onAskButler = { onAskButler(askButlerMessageFor(poi)) },
                                )
                            }
                            if (state.isLoading) {
                                item {
                                    Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BrandGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── filter panels (overlay) ──
        if (filters.panelOpen != FilterPanelOpen.None) {
            // scrim
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(onClick = onDismissPanel)
            )
        }

        // Individual panels (show just below the filter bar ~120dp from top)
        AnimatedVisibility(
            visible = filters.panelOpen == FilterPanelOpen.Proximity,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 120.dp),
        ) {
            ProximityPanel(
                selected = filters.proximity,
                locationGranted = state.locationGranted,
                onSelect = onProximity,
            )
        }

        AnimatedVisibility(
            visible = filters.panelOpen == FilterPanelOpen.Category,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 120.dp),
        ) {
            SubcategoryPanel(
                category = state.category,
                selected = filters.subcategory,
                onSelect = onSubcategory,
            )
        }

        AnimatedVisibility(
            visible = filters.panelOpen == FilterPanelOpen.Sort,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 120.dp),
        ) {
            SortPanel(
                selected = filters.sort,
                locationGranted = state.locationGranted,
                onSelect = onSort,
            )
        }

        AnimatedVisibility(
            visible = filters.panelOpen == FilterPanelOpen.More,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 120.dp),
        ) {
            MorePanel(
                draftPrices = draftPrices,
                draftRatedOnly = draftRatedOnly,
                onPriceToggle = { price ->
                    draftPrices = draftPrices.toMutableSet().apply {
                        if (contains(price)) remove(price) else add(price)
                    }
                },
                onRatedOnlyChange = { draftRatedOnly = it },
                onReset = {
                    draftPrices = mutableSetOf()
                    draftRatedOnly = false
                },
                onConfirm = {
                    onApplyMore(draftPrices.toSet(), draftRatedOnly)
                },
            )
        }

        // ── "Added" snackbar ──
        state.lastAddedPoiName?.let { name ->
            LaunchedEffect(name) {
                kotlinx.coroutines.delay(2500)
                onDismissAdded()
            }
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = { TextButton(onClick = onDismissAdded) { Text("OK") } },
            ) {
                Text("\"$name\" added to Trip")
            }
        }

        if (showCitySheet) {
            CitySelectionSheet(
                cities = SUPPORTED_CITIES,
                selectedId = state.selectedCityId,
                onSelect = { id ->
                    onCitySelect(id)
                    showCitySheet = false
                },
                onDismiss = { showCitySheet = false },
            )
        }
    }
}

// ══════════════════════════════════════════════════
// Filter Bar
// ══════════════════════════════════════════════════

@Composable
private fun FilterBar(
    filters: ChannelFilters,
    locationGranted: Boolean,
    onToggle: (FilterPanelOpen) -> Unit,
) {
    val moreCount = (if (filters.ratedOnly) 1 else 0) + filters.prices.size
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterTab(
            label = if (filters.proximity == ProximityFilter.City) "Nearby" else filters.proximity.labelEn,
            active = filters.proximity != ProximityFilter.City,
            open = filters.panelOpen == FilterPanelOpen.Proximity,
            onClick = { onToggle(FilterPanelOpen.Proximity) },
        )
        FilterTab(
            label = if (filters.subcategory.labelEn == "All") "Category" else filters.subcategory.labelEn,
            active = filters.subcategory.labelEn != "All",
            open = filters.panelOpen == FilterPanelOpen.Category,
            onClick = { onToggle(FilterPanelOpen.Category) },
        )
        FilterTab(
            label = if (filters.sort == SortMode.Smart) "Sort" else filters.sort.labelEn,
            active = filters.sort != SortMode.Smart,
            open = filters.panelOpen == FilterPanelOpen.Sort,
            onClick = { onToggle(FilterPanelOpen.Sort) },
        )
        FilterTab(
            label = if (moreCount == 0) "Filter" else "Filter·$moreCount",
            active = moreCount > 0,
            open = filters.panelOpen == FilterPanelOpen.More,
            onClick = { onToggle(FilterPanelOpen.More) },
        )
    }
}

@Composable
private fun FilterTab(label: String, active: Boolean, open: Boolean, onClick: () -> Unit) {
    val bg = if (active || open) ActivePill else PillBg
    val textColor = if (active || open) Color.White else MaterialTheme.colorScheme.onSurface
    Row(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = textColor)
        Icon(
            if (open) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            null,
            tint = textColor,
            modifier = Modifier.size(16.dp),
        )
    }
}

// ══════════════════════════════════════════════════
// Filter Panels
// ══════════════════════════════════════════════════

@Composable
private fun ProximityPanel(
    selected: ProximityFilter,
    locationGranted: Boolean,
    onSelect: (ProximityFilter) -> Unit,
) {
    PanelSurface {
        ProximityFilter.entries.forEach { opt ->
            SingleOptionRow(
                label = opt.labelEn,
                selected = selected == opt,
                enabled = opt == ProximityFilter.City || locationGranted,
                onClick = { if (opt == ProximityFilter.City || locationGranted) onSelect(opt) },
            )
        }
        if (!locationGranted) {
            Text(
                "Enable location for distance filters",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun SubcategoryPanel(
    category: ExploreCategory,
    selected: ExploreSubcategory,
    onSelect: (ExploreSubcategory) -> Unit,
) {
    val subcategories = ExploreSubcategory.entries.filter { it.category == category }
    PanelSurface {
        subcategories.forEach { sub ->
            SingleOptionRow(
                label = sub.labelEn,
                selected = selected == sub,
                onClick = { onSelect(sub) },
            )
        }
    }
}

@Composable
private fun SortPanel(
    selected: SortMode,
    locationGranted: Boolean,
    onSelect: (SortMode) -> Unit,
) {
    PanelSurface {
        SortMode.entries.forEach { mode ->
            val enabled = mode != SortMode.Nearest || locationGranted
            SingleOptionRow(
                label = mode.labelEn + if (mode == SortMode.Nearest && !locationGranted) " (enable location)" else "",
                selected = selected == mode,
                enabled = enabled,
                onClick = { if (enabled) onSelect(mode) },
            )
        }
    }
}

@Composable
private fun MorePanel(
    draftPrices: Set<PriceFilter>,
    draftRatedOnly: Boolean,
    onPriceToggle: (PriceFilter) -> Unit,
    onRatedOnlyChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    onConfirm: () -> Unit,
) {
    PanelSurface {
        Text(
            "Price per person (only for places with price data)",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        PriceFilter.entries.forEach { pf ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onPriceToggle(pf) }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = pf in draftPrices,
                    onCheckedChange = { onPriceToggle(pf) },
                    colors = CheckboxDefaults.colors(checkedColor = BrandGreen),
                )
                Spacer(Modifier.width(8.dp))
                Text(pf.labelEn, style = MaterialTheme.typography.bodyMedium)
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 4.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { onRatedOnlyChange(!draftRatedOnly) }
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = draftRatedOnly,
                onCheckedChange = onRatedOnlyChange,
                colors = CheckboxDefaults.colors(checkedColor = BrandGreen),
            )
            Spacer(Modifier.width(8.dp))
            Text("Has rating", style = MaterialTheme.typography.bodyMedium)
        }
        HorizontalDivider(Modifier.padding(vertical = 4.dp))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) { Text("Reset") }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            ) { Text("Apply") }
        }
    }
}

@Composable
private fun PanelSurface(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 4.dp),
            content = content,
        )
    }
}

@Composable
private fun SingleOptionRow(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                selected -> BrandGreen
                !enabled -> Color.Gray
                else -> MaterialTheme.colorScheme.onSurface
            },
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(Icons.Default.Check, null, tint = BrandGreen, modifier = Modifier.size(18.dp))
        }
    }
}

// ══════════════════════════════════════════════════
// POI Card
// ══════════════════════════════════════════════════

@Composable
private fun PoiCard(
    poi: ExplorePoi,
    isFocused: Boolean = false,
    onAddToTrip: () -> Unit,
    onAskButler: () -> Unit = {},
) {
    // Chat↔Explore bridge (Issue #59): a Butler exploreRef card jumps here
    // with the referenced POI expanded and highlighted, so the traveler sees
    // exactly what they tapped rather than having to find it in the list.
    var expanded by remember(isFocused) { mutableStateOf(isFocused) }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        border = if (isFocused) BorderStroke(2.dp, BrandGreen) else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { expanded = !expanded },
    ) {
        Row(Modifier.padding(12.dp)) {
            // photo
            if (poi.photoUrl != null) {
                AsyncImage(
                    model = poi.photoUrl,
                    contentDescription = poi.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
            } else {
                Box(
                    Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE8F5E9)),
                    Alignment.Center,
                ) {
                    Text(poi.category.emoji, fontSize = 28.sp)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                // name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        poi.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (poi.editorialSummary != null) {
                        Spacer(Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE8F5E9),
                        ) {
                            Text(
                                "✦ VP Pick",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandGreen,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                // rating + cost
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (poi.hasRating) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                        Text(
                            String.format("%.1f", poi.rating),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFB300),
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    if (poi.priceHint.isNotBlank()) {
                        Text(
                            poi.priceHint,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        )
                    }
                }

                // business area + distance
                val metaLine = listOfNotNull(
                    poi.businessArea?.takeIf { it.isNotBlank() },
                    poi.distanceMeters?.let { m ->
                        if (m < 1000) "${m}m" else String.format("%.1fkm", m / 1000f)
                    },
                ).joinToString(" · ")
                if (metaLine.isNotBlank()) {
                    Text(
                        metaLine,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // expanded: editorial + add to trip
                if (expanded) {
                    poi.editorialSummary?.let { summary ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onAddToTrip,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            modifier = Modifier.height(32.dp),
                        ) {
                            Text("Add to Trip", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = onAskButler,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp),
                        ) {
                            Text("Ask Butler", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

/** Chat↔Explore bridge (Issue #59): editable prefilled prompt for the "Ask Butler" button. */
private fun askButlerMessageFor(poi: ExplorePoi): String {
    val details = listOfNotNull(
        poi.businessArea?.takeIf { it.isNotBlank() },
        if (poi.hasRating) "rating ${String.format("%.1f", poi.rating)}" else null,
    ).joinToString(", ")
    val suffix = if (details.isNotBlank()) " ($details)" else ""
    return "Tell me about ${poi.name}$suffix — is it good for my trip? Can you fit it into my itinerary?"
}

// ══════════════════════════════════════════════════
// City Selection Sheet
// ══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CitySelectionSheet(
    cities: List<CityItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Select City",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        cities.forEach { city ->
            val selected = city.id == selectedId
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(city.id) }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        city.nameEn,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) BrandGreen else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(city.nameZh, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                if (selected) {
                    Icon(Icons.Default.Check, null, tint = BrandGreen)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
