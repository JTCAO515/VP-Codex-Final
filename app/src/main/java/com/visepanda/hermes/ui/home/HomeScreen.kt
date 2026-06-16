package com.visepanda.hermes.ui.home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.visepanda.designsystem.Background
import com.visepanda.designsystem.Gold
import com.visepanda.designsystem.GoldLight
import com.visepanda.designsystem.GoldPale
import com.visepanda.designsystem.JadeGrey
import com.visepanda.designsystem.Surface
import com.visepanda.designsystem.SurfaceElevated
import com.visepanda.designsystem.TextPrimary
import com.visepanda.designsystem.TextSecondary
import com.visepanda.designsystem.TextTertiary
import com.visepanda.designsystem.VisePandaShapes
import com.visepanda.designsystem.VisePandaSpacing
import com.visepanda.designsystem.components.VpCard
import com.visepanda.designsystem.components.VpCityCard
import com.visepanda.designsystem.components.VpCityCardShimmer
import com.visepanda.designsystem.components.VpEnterAnimation
import com.visepanda.designsystem.components.VpGoldButton
import com.visepanda.designsystem.components.VpShimmer
import kotlinx.coroutines.delay

// ── Data Models ──

data class CityData(
    val name: String,
    val description: String,
    val tags: List<String>,
    val imageUrl: String? = null
)

data class InspirationData(
    val icon: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null
)

data class EssentialData(
    val icon: String,
    val label: String,
    val key: String
)

// Rich content for each essential
data class EssentialContent(
    val title: String,
    val icon: String,
    val sections: List<EssentialSection>
)

data class EssentialSection(
    val heading: String,
    val body: String,
    val tip: String? = null     // gold-highlighted tip at the bottom
)

// ── Hardcoded Data ──

private val featuredCities = listOf(
    CityData("Beijing", "Capital of China", listOf("Culture", "History"), "https://www.go2china.space/static/img/city-beijing.jpg"),
    CityData("Shanghai", "Pearl of the Orient", listOf("Modern", "Nightlife"), "https://www.go2china.space/static/img/city-shanghai.jpg"),
    CityData("Xi'an", "Ancient Capital", listOf("History", "Food"), "https://www.go2china.space/static/img/city-xian.jpg"),
    CityData("Chengdu", "Home of Pandas", listOf("Nature", "Food"), "https://www.go2china.space/static/img/city-chengdu.jpg"),
    CityData("Guangzhou", "Canton Cuisine", listOf("Food", "Shopping"), "https://www.go2china.space/static/img/city-guangzhou.jpg")
)

private val inspirations = listOf(
    InspirationData("🗺️", "First Time in China", "Essential tips & must-see destinations for your inaugural visit.", "https://www.go2china.space/static/img/inspiration-first-time.jpg"),
    InspirationData("🍜", "Foodie's Journey", "From Beijing duck to dim sum — a culinary tour across China.", "https://www.go2china.space/static/img/inspiration-foodie.jpg"),
    InspirationData("🏯", "Hidden Gems", "Lesser-known destinations that offer authentic Chinese experiences.", "https://www.go2china.space/static/img/inspiration-hidden-gems.jpg")
)

private val essentials = listOf(
    EssentialData("📱", "Visa", "visa"),
    EssentialData("💰", "Currency", "currency"),
    EssentialData("🌐", "VPN", "vpn"),
    EssentialData("🚄", "Trains", "trains"),
    EssentialData("🏨", "Hotels", "hotels"),
    EssentialData("🗣️", "Language", "language")
)

// ── Essential Content ──

private val essentialContentMap = mapOf(
    "visa" to EssentialContent(
        title = "China Visa Guide",
        icon = "📱",
        sections = listOf(
            EssentialSection(
                heading = "Do I need a visa?",
                body = "Most foreign travelers need a visa to enter mainland China. Citizens of 59 countries can enjoy 144-hour transit without a visa if they have a confirmed onward ticket and stay within approved cities (Beijing, Shanghai, Guangzhou, Chengdu, and 20+ others).",
                tip = "Check your country's visa policy at visaforchina.org before booking flights."
            ),
            EssentialSection(
                heading = "L Tourist Visa",
                body = "The standard L visa is for tourism. Valid for 30-90 days, single or multiple entry. You'll need: valid passport (6+ months), completed application form, recent photo, flight itinerary, hotel bookings, and proof of funds. Processing takes 4-7 business days.",
                tip = "Apply at least 3 weeks before your trip. Rush service (2-3 days) available at extra cost."
            ),
            EssentialSection(
                heading = "Visa on Arrival & TWOV",
                body = "Visa on Arrival is available ONLY at select ports (Shanghai, Beijing, Guangzhou, Chengdu, Kunming, Xiamen, Wuhan, Changsha, Guilin, Haikou). The 144-hour Transit Without Visa (TWOV) is the most popular option — covers 3 full days, enough for a short layover trip.",
                tip = "144-hour TWOV: you MUST have a confirmed ticket to a THIRD country (not back to your origin)."
            ),
            EssentialSection(
                heading = "Hong Kong & Macau",
                body = "Hong Kong and Macau have separate visa policies from mainland China. Most nationalities get visa-free entry for 14-90 days. Your China visa does NOT automatically allow entry to HK/Macau, and vice versa.",
                tip = "Traveling to HK before mainland? Get your China visa in Hong Kong through the China Travel Service."
            )
        )
    ),
    "currency" to EssentialContent(
        title = "Currency & Payments",
        icon = "💰",
        sections = listOf(
            EssentialSection(
                heading = "Chinese Yuan (RMB)",
                body = "The official currency is the Chinese Yuan Renminbi (CNY/RMB). Banknotes come in 1, 5, 10, 20, 50, and 100 yuan. Coins are 0.1, 0.5, and 1 yuan. Exchange rates fluctuate; check xe.com for live rates. As of 2026, ¥1 ≈ $0.14 USD.",
                tip = "Download a currency converter app. Don't exchange at airports — rates are 3-5% worse than city exchange shops."
            ),
            EssentialSection(
                heading = "Mobile Payments (WeChat Pay & Alipay)",
                body = "China is nearly cashless. WeChat Pay and Alipay are accepted everywhere — from street food stalls to luxury malls. Foreigners can now link international Visa/Mastercard to both apps. Setup takes 10 minutes: download the app, register with your passport, link your card.",
                tip = "Link a Visa/Mastercard BEFORE you leave. Some features require in-China network to activate."
            ),
            EssentialSection(
                heading = "ATMs & Cash",
                body = "ATMs are widely available in cities. Bank of China, ICBC, and China Construction Bank ATMs accept foreign cards. Withdrawal limits: typically ¥2,500-3,000 per transaction, ¥10,000-20,000 per day. Carry ¥500-1,000 cash for taxis, small vendors, and emergencies.",
                tip = "Always carry some cash. Street food, small shops, and some taxis don't accept cards or mobile pay."
            ),
            EssentialSection(
                heading = "Credit Cards",
                body = "International credit cards (Visa, Mastercard, Amex) are accepted at high-end hotels, international restaurants, and large shopping malls. Smaller establishments, local restaurants, and street vendors rarely accept foreign cards. UnionPay is universal.",
                tip = "Get a UnionPay card if possible, or link your foreign card to Alipay/WeChat for full coverage."
            )
        )
    ),
    "vpn" to EssentialContent(
        title = "VPN & Internet",
        icon = "🌐",
        sections = listOf(
            EssentialSection(
                heading = "The Great Firewall",
                body = "China blocks Google, Gmail, YouTube, Instagram, Facebook, Twitter/X, WhatsApp, Telegram, and many news sites. All this content is inaccessible without a VPN. The blocks apply to both WiFi and mobile data.",
                tip = "Install and TEST your VPN before you leave China. It's very hard to set up once inside."
            ),
            EssentialSection(
                heading = "Best VPNs for China",
                body = "Not all VPNs work in China. Reliable options in 2026: Astrill, ExpressVPN, NordVPN, Surfshark, VyprVPN. Avoid free VPNs — they don't work and may steal your data. Expect to pay $8-15/month for a quality service.",
                tip = "Buy 3-6 months upfront. Some VPNs disconnect randomly — having multiple protocols (OpenVPN, WireGuard) helps."
            ),
            EssentialSection(
                heading = "Mobile Data & WiFi",
                body = "Buy a Chinese SIM card at the airport (China Mobile, China Unicom, China Telecom). Tourist SIMs cost ¥100-300 for 7-30 days with 10-50GB data. You need your passport to buy one. Portable WiFi hotspots can be rented at airports for ¥20-40/day.",
                tip = "Airalo and Holafly offer eSIMs you can install before departure — no SIM swapping needed."
            ),
            EssentialSection(
                heading = "Essential Apps",
                body = "Download these before you go: WeChat (messaging/payments), Alipay (payments), DiDi (rides), Meituan (food delivery), Baidu Maps or Gaode (navigation), Trip.com (hotels/flights/trains), Pleco (Chinese dictionary).",
                tip = "Baidu Maps works better than Google Maps in China. Download offline maps for your cities."
            )
        )
    ),
    "trains" to EssentialContent(
        title = "High-Speed Trains",
        icon = "🚄",
        sections = listOf(
            EssentialSection(
                heading = "China's Rail Network",
                body = "China has the world's largest high-speed rail network (over 45,000 km). G-trains (高速铁路) run at 300-350 km/h. D-trains (动车) run at 200-250 km/h. K/T/Z trains are slower but cheaper. Major routes: Beijing-Shanghai (4.5h), Beijing-Guangzhou (8h), Shanghai-Chengdu (11h).",
                tip = "G-trains are the best balance of speed and price. Book first-class for wider seats and power outlets."
            ),
            EssentialSection(
                heading = "Booking Tickets",
                body = "Book online via Trip.com (formerly Ctrip) — the English-friendly platform accepts foreign cards. Alternatively, use 12306.cn (official, Chinese-only). Book at least 7-14 days ahead for popular routes. Tickets go on sale 15 days in advance.",
                tip = "Trip.com charges a small booking fee but is worth it for English support. Use the app for mobile tickets."
            ),
            EssentialSection(
                heading = "At the Station",
                body = "Arrive 30-45 minutes early. You need your passport to enter and board. Follow the signs: 进站口 (Entrance) → 安检 (Security) → 候车室 (Waiting Hall) → 检票口 (Gate). Boarding starts 15 minutes before departure, gates close 5 minutes before.",
                tip = "Don't lose your ticket (paper or digital). You'll scan it at the gate AND on the train."
            ),
            EssentialSection(
                heading = "Onboard Experience",
                body = "Second-class (二等座): 5 seats per row, comfortable. First-class (一等座): 4 seats per row, wider, more legroom. Business-class: 3 seats, full recline, meals included. All classes have AC, power outlets, WiFi (spotty), and a dining car with hot meals.",
                tip = "Bring snacks and water. The dining car food is basic. Download shows before boarding — WiFi is unreliable."
            )
        )
    ),
    "hotels" to EssentialContent(
        title = "Hotels & Accommodation",
        icon = "🏨",
        sections = listOf(
            EssentialSection(
                heading = "Booking Platforms",
                body = "Trip.com (formerly Ctrip) is the best English-friendly booking platform. Booking.com and Agoda also work but have fewer properties. Local platforms like Meituan and Fliggy (Taobao Travel) have the best deals but require Chinese language skills.",
                tip = "Compare prices on Trip.com and Booking.com. Trip.com often has lower rates for domestic properties."
            ),
            EssentialSection(
                heading = "Hotel Types & Budget",
                body = "Budget (¥100-300/night): Hostels, chain hotels (Hanting, Home Inn, 7Days Inn). Mid-range (¥300-800): Boutique hotels, international chains (Holiday Inn, Marriott). Luxury (¥800-3000+): 5-star international brands, resort hotels in scenic areas.",
                tip = "Mid-range Chinese chain hotels offer excellent value — clean, modern, English-speaking staff, free breakfast."
            ),
            EssentialSection(
                heading = "Foreigner-Friendly Hotels",
                body = "By law, only hotels with a 涉外 (foreigner-approved) license can accept foreign guests. Most mid-range and all luxury hotels have this license. Budget hostels may not. Always confirm before booking — Trip.com filters for this automatically.",
                tip = "Call ahead to confirm they accept foreign guests. Some budget hotels listed online may turn you away at check-in."
            ),
            EssentialSection(
                heading = "Check-in Requirements",
                body = "You MUST present your passport at check-in. The hotel will scan it and register you with the local police (this is required by law for all foreigners). You'll fill out a registration form. Keep the registration slip — you may need it for visa extensions.",
                tip = "Carry your passport at all times. Hotels cannot check you in without the physical passport — copies won't work."
            )
        )
    ),
    "language" to EssentialContent(
        title = "Language & Communication",
        icon = "🗣️",
        sections = listOf(
            EssentialSection(
                heading = "English in China",
                body = "English is not widely spoken outside of international hotels, tourist areas, and major airports. In smaller cities and local restaurants, expect zero English. Learn a few key phrases and always have translation apps ready. Young people in big cities know basic English.",
                tip = "Download Pleco (offline Chinese dictionary) and Google Translate (offline packs). You'll use them daily."
            ),
            EssentialSection(
                heading = "Essential Phrases",
                body = "你好 (nǐ hǎo) — Hello. 谢谢 (xiè xiè) — Thank you. 对不起 (duì bu qǐ) — Sorry. 多少钱 (duō shao qián) — How much? 这个 (zhè ge) — This one. 不要了 (bú yào le) — I don't want it. 厕所 (cè suǒ) — Toilet. 菜单 (cài dān) — Menu.",
                tip = "Show the Chinese characters on your phone. Your pronunciation won't be perfect, but the text will be understood."
            ),
            EssentialSection(
                heading = "Translation Apps",
                body = "Google Translate (offline packs), Microsoft Translator, Pleco, and Baidu Translate are essential. Microsoft Translator has excellent camera translation — point at a menu and it translates in real-time. Pleco is the best dictionary with handwriting input.",
                tip = "Download offline language packs before you leave. Internet can be slow on VPN even when it works."
            ),
            EssentialSection(
                heading = "Cultural Tips",
                body = "Avoid sensitive topics: Tiananmen, Taiwan as a country, Tibet independence, Falun Gong, criticism of the Communist Party. Do accept things with both hands. Do not stick chopsticks upright in rice (resembles funeral incense). Tipping is not customary.",
                tip = "Nodding and smiling goes a long way. Chinese people are generally very helpful to foreign visitors."
            )
        )
    )
)

// ── Home Screen ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    var isLoading by remember { mutableStateOf(true) }
    var selectedEssential by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        delay(800)
        isLoading = false
    }

    // Bottom sheet for essential detail
    if (selectedEssential != null) {
        val content = essentialContentMap[selectedEssential]
        if (content != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedEssential = null },
                sheetState = sheetState,
                containerColor = Surface,
                dragHandle = { EssentialSheetHandle() }
            ) {
                EssentialContentSheet(content = content)
            }
        }
    }

    if (isLoading) {
        HomeShimmer(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .background(Background)
        ) {
            item { HeroSection() }
            item { SectionHeader(title = "Featured Cities", actionText = "See all") }
            item { FeaturedCitiesRow() }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Inspiration", actionText = null)
            }
            item { InspirationGrid() }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Travel Essentials", actionText = null)
            }
            item {
                EssentialsGrid(onEssentialClick = { selectedEssential = it })
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// ── Shimmer Loading ──

@Composable
private fun HomeShimmer(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = VisePandaSpacing.lg)
    ) {
        item {
            Spacer(modifier = Modifier.height(40.dp))
            VpShimmer(widthFraction = 0.35f, height = 28)
            Spacer(modifier = Modifier.height(12.dp))
            VpShimmer(widthFraction = 0.7f, height = 16)
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            VpShimmer(widthFraction = 0.4f, height = 22)
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(3) { VpCityCardShimmer(modifier = Modifier.width(200.dp)) }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            VpShimmer(widthFraction = 0.3f, height = 22)
            Spacer(modifier = Modifier.height(12.dp))
            VpShimmer(widthFraction = 1f, height = 80)
            Spacer(modifier = Modifier.height(12.dp))
            VpShimmer(widthFraction = 1f, height = 80)
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            VpShimmer(widthFraction = 0.35f, height = 22)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(3) { VpShimmer(widthFraction = 1f, height = 100) }
            }
        }
    }
}

// ── Hero Section ──

@Composable
private fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        AsyncImage(
            model = "https://www.go2china.space/static/img/city-shanghai.jpg",
            contentDescription = "China travel hero",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x662D2D2D),
                            Color(0x882D2D2D),
                            Color(0xCC2D2D2D)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(colors = listOf(GoldLight, Gold))),
                contentAlignment = Alignment.Center
            ) {
                Text("V", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Your AI China\nTravel Companion",
                style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center, color = Color.White, lineHeight = 40.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Plan your perfect China journey with AI-powered recommendations.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center, color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

// ── Featured Cities ──

@Composable
private fun FeaturedCitiesRow() {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(featuredCities) { city ->
            VpEnterAnimation(index = featuredCities.indexOf(city), staggerDelay = 80) {
                VpCityCard(
                    imageUrl = city.imageUrl, cityName = city.name,
                    description = city.description, tags = city.tags,
                    onClick = { /* Navigate to city detail */ },
                    modifier = Modifier.width(220.dp), height = 240
                )
            }
        }
    }
}

// ── Inspiration Grid ──

@Composable
private fun InspirationGrid() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        inspirations.forEachIndexed { index, item ->
            VpEnterAnimation(index = index, staggerDelay = 80) {
                InspirationCard(item)
            }
        }
    }
}

@Composable
private fun InspirationCard(item: InspirationData) {
    VpCard(onClick = { /* Navigate to guide */ }, modifier = Modifier.fillMaxWidth().height(120.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            // Overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xE62D2D2D),
                                Color(0x662D2D2D)
                            )
                        )
                    )
            )
            // Content
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp).clip(VisePandaShapes.small).background(GoldLight.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) { Text(text = item.icon, fontSize = 22.sp) }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.title,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        item.description,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f), maxLines = 2
                    )
                }
            }
        }
    }
}

// ── Essentials Grid (with bottom sheet trigger) ──

@Composable
private fun EssentialsGrid(onEssentialClick: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        essentials.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { item ->
                    EssentialTile(
                        item = item,
                        onClick = { onEssentialClick(item.key) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size < 3) {
                    repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun EssentialTile(
    item: EssentialData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    VpCard(onClick = onClick, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = item.icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.label,
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = TextSecondary, textAlign = TextAlign.Center
            )
        }
    }
}

// ── Section Header ──

@Composable
private fun SectionHeader(title: String, actionText: String? = "See all") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        if (actionText != null) {
            Text(
                actionText,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = Gold
            )
        }
    }
}

// ════════════════════════════════════════════════════════════
// BOTTOM SHEET — Essential Detail
// ════════════════════════════════════════════════════════════

@Composable
private fun EssentialSheetHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(VisePandaShapes.extraLarge)
                .background(Color(0xFFDDD6C8))
        )
    }
}

@Composable
private fun EssentialContentSheet(content: EssentialContent) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = content.icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = content.title,
                style = androidx.compose.material3.MaterialTheme.typography.displaySmall,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sections
        content.sections.forEachIndexed { index, section ->
            EssentialSectionCard(section = section, index = index)
            if (index < content.sections.lastIndex) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Footer
        Text(
            text = "Last updated June 2026. Always verify current regulations before travel.",
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = TextTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EssentialSectionCard(section: EssentialSection, index: Int) {
    VpEnterAnimation(index = index, staggerDelay = 40) {
        VpCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Heading
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Gold)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = section.heading,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Body
                Text(
                    text = section.body,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )

                // Tip (gold highlighted)
                if (section.tip != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(VisePandaShapes.small)
                            .background(GoldLight.copy(alpha = 0.15f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("💡", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = section.tip,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = com.visepanda.designsystem.GoldDark,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
