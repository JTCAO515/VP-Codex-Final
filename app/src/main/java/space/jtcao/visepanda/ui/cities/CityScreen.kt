package space.jtcao.visepanda.ui.cities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import space.jtcao.visepanda.data.model.City
import space.jtcao.visepanda.data.model.CityDetail
import space.jtcao.visepanda.data.model.FoodItem
import space.jtcao.visepanda.data.repository.CityRepository
import space.jtcao.visepanda.ui.home.getCityEmoji

// ═════════════════════════════════════════════════
//  CITY LIST SCREEN
// ═════════════════════════════════════════════════

@Composable
fun CityListScreen(
    onCityClick: (String) -> Unit,
    viewModel: CityListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PullToRefreshBox(
        isRefreshing = uiState is CityListUiState.Loading,
        onRefresh = { viewModel.load() },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is CityListUiState.Loading -> LoadingGrid()
            is CityListUiState.Success -> CityGrid(
                cities = state.cities,
                onCityClick = onCityClick
            )
            is CityListUiState.Error -> ErrorState(
                message = state.message,
                onRetry = { viewModel.load() }
            )
        }
    }
}

@Composable
private fun CityGrid(
    cities: List<Pair<String, City>>,
    onCityClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(cities, key = { it.first }) { (name, city) ->
            CityCardSmall(
                name = name,
                city = city,
                onClick = { onCityClick(name) }
            )
        }
    }
    // Bottom nav padding handled by Scaffold
}

@Composable
private fun CityCardSmall(
    name: String,
    city: City,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (city.image.isNotEmpty()) {
                AsyncImage(
                    model = CityRepository().getCityImageUrl(name),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        )
                    )
            )
            Column(
                modifier = Modifier.fillMaxSize().padding(10.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(text = getCityEmoji(name), style = MaterialTheme.typography.titleLarge)
                Text(text = name.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                if (city.nameCn.isNotEmpty()) {
                    Text(text = city.nameCn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun LoadingGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(8) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.9f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("😵", style = MaterialTheme.typography.displayMedium)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

// ═════════════════════════════════════════════════
//  CITY DETAIL SCREEN
// ═════════════════════════════════════════════════

@Composable
fun CityDetailScreen(
    cityName: String,
    onBack: () -> Unit,
    viewModel: CityDetailViewModel = viewModel()
) {
    LaunchedEffect(cityName) { viewModel.load(cityName) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cityName.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is CityDetailUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("🐼 Loading...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            is CityDetailUiState.Success -> CityDetailContent(state.detail, state.cityName, Modifier.padding(padding))
            is CityDetailUiState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.load(cityName) }) { Text("Retry") }
                }
            }
        }
    }
}

@Composable
private fun CityDetailContent(
    detail: CityDetail,
    cityName: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // ── Header ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = getCityEmoji(cityName), style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = cityName.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = "${detail.nameCn} · ${detail.province}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Meta ──
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (detail.bestSeason.isNotEmpty()) Text("📅 Best: ${detail.bestSeason}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (detail.days.isNotEmpty()) Text("⏱️ ${detail.days}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (detail.vibe.isNotEmpty()) {
            Text(detail.vibe, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        }

        // ── Budget Tip ──
        if (detail.budgetTip.isNotEmpty()) {
            Card(modifier = Modifier.padding(top = 12.dp).fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))) {
                Text("💰 ${detail.budgetTip}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
            }
        }

        // ── Highlights Tags ──
        if (detail.highlights.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                detail.highlights.take(4).forEach { tag ->
                    Text(tag, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)).padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }

        // ── Price Estimates ──
        if (detail.estimate.midDaily.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            SectionTitle("💰 Price Estimates")
            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    EstimateRow("Budget/day", detail.estimate.budgetDaily)
                    EstimateRow("Mid/day", detail.estimate.midDaily)
                    EstimateRow("Luxury/day", detail.estimate.luxuryDaily)
                    if (detail.estimate.flightAvg.isNotEmpty()) EstimateRow("Avg flight", detail.estimate.flightAvg)
                    if (detail.estimate.foodAvg.isNotEmpty()) EstimateRow("Avg meal", detail.estimate.foodAvg)
                }
            }
        }

        // ── Food ──
        if (detail.food.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            SectionTitle("🍽️ Must-Eat Foods")
            Spacer(Modifier.height(8.dp))
            detail.food.forEach { food ->
                FoodCard(food)
                Spacer(Modifier.height(8.dp))
            }
        }

        // ── Hotels ──
        if (detail.hotels.budget.range.isNotEmpty() || detail.hotels.mid.range.isNotEmpty() || detail.hotels.luxury.range.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            SectionTitle("🏨 Accommodation")
            Spacer(Modifier.height(8.dp))
            HotelTierCard("Budget", detail.hotels.budget)
            Spacer(Modifier.height(6.dp))
            HotelTierCard("Mid", detail.hotels.mid)
            Spacer(Modifier.height(6.dp))
            HotelTierCard("Luxury", detail.hotels.luxury)
            if (detail.hotels.tip.isNotEmpty()) {
                Text("💡 ${detail.hotels.tip}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
        }

        // ── Tips ──
        if (detail.tips.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            SectionTitle("💡 Local Tips")
            Spacer(Modifier.height(8.dp))
            detail.tips.forEach { tip ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(
                        text = if (tip.en.isNotEmpty()) "<b>${tip.en}</b>: ${tip.tip}" else tip.tip,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // ── CTA Button ──
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { /* Navigate to chat with this city */ },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("💬 Plan a Trip to ${cityName.replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.titleSmall)
        }

        Spacer(Modifier.height(80.dp)) // Bottom nav padding
    }
}

// ── Shared sub-components ──

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
}

@Composable
private fun EstimateRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun FoodCard(food: FoodItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (food.mustTry) Text("⭐ ", style = MaterialTheme.typography.bodyMedium)
                Text(food.nameEn, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (food.nameCn.isNotEmpty()) {
                    Spacer(Modifier.width(6.dp))
                    Text(food.nameCn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (food.description.isNotEmpty()) {
                Text(food.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
            if (food.priceRange.isNotEmpty()) {
                Text("💰 ${food.priceRange}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun HotelTierCard(label: String, tier: space.jtcao.visepanda.data.model.HotelTier) {
    if (tier.range.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(60.dp))
            Column {
                Text(tier.range, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                Text(listOfNotNull(tier.desc, tier.areas).joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
