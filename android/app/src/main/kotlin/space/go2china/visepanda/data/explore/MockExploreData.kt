package space.go2china.visepanda.data.explore

import space.go2china.visepanda.data.model.BookingCandidate
import space.go2china.visepanda.data.model.Coordinates

/**
 * v0.3.22 (#47): Dianping-style Explore — expanded from 3 to 5 categories,
 * subcategory support, distance field, editorial overlay.
 */
enum class ExploreCategory(val labelEn: String, val labelZh: String, val emoji: String) {
    Food("Food", "美食", "🍜"),
    Attraction("Attractions", "景点", "🏯"),
    Stay("Hotels", "酒店", "🏨"),
    Shopping("Shopping", "购物", "🛍️"),
    Experience("Experiences", "体验", "💆"),
}

/** Subcategory — the semantic key sent to /api/explore/amap as [type] param. */
enum class ExploreSubcategory(
    val key: String,
    val labelEn: String,
    val labelZh: String,
    val category: ExploreCategory
) {
    // Food
    FoodAll("food", "All", "全部", ExploreCategory.Food),
    Hotpot("food.hotpot", "Hot Pot", "火锅", ExploreCategory.Food),
    Sichuan("food.sichuan", "Sichuan", "川菜", ExploreCategory.Food),
    Cantonese("food.cantonese", "Cantonese", "粤菜", ExploreCategory.Food),
    Japanese("food.japanese", "Japanese", "日料", ExploreCategory.Food),
    Bbq("food.bbq", "BBQ", "烧烤", ExploreCategory.Food),
    Bakery("food.bakery", "Bakery & Desserts", "面包甜品", ExploreCategory.Food),
    Snacks("food.snacks", "Snacks", "小吃快餐", ExploreCategory.Food),
    Coffee("food.coffee", "Coffee", "咖啡厅", ExploreCategory.Food),
    // Attraction
    AttractionAll("attractions", "All", "全部", ExploreCategory.Attraction),
    Scenic("attractions.scenic", "Scenic Spots", "风景名胜", ExploreCategory.Attraction),
    Park("attractions.park", "Parks & Squares", "公园广场", ExploreCategory.Attraction),
    Museum("attractions.museum", "Museums", "博物馆", ExploreCategory.Attraction),
    Temple("attractions.temple", "Temples", "寺庙道观", ExploreCategory.Attraction),
    // Stay
    StayAll("stays", "All", "全部", ExploreCategory.Stay),
    StarHotel("stays.star", "Star Hotels", "星级酒店", ExploreCategory.Stay),
    BudgetHotel("stays.budget", "Budget Hotels", "快捷连锁", ExploreCategory.Stay),
    Guesthouse("stays.guesthouse", "Guesthouses", "民宿客栈", ExploreCategory.Stay),
    // Shopping
    ShoppingAll("shopping", "All", "全部", ExploreCategory.Shopping),
    Mall("shopping.mall", "Malls", "商场", ExploreCategory.Shopping),
    SpecialtyStreet("shopping.specialty", "Specialty Streets", "特产街区", ExploreCategory.Shopping),
    Supermarket("shopping.supermarket", "Supermarkets", "超市便利", ExploreCategory.Shopping),
    // Experience
    ExperienceAll("experiences", "All", "全部", ExploreCategory.Experience),
    Massage("experiences.massage", "Massage & Foot Spa", "按摩足疗", ExploreCategory.Experience),
    Spa("experiences.spa", "SPA & Beauty", "SPA美容", ExploreCategory.Experience),
    Teahouse("experiences.teahouse", "Teahouses", "茶馆", ExploreCategory.Experience),
    Ktv("experiences.ktv", "KTV", "KTV", ExploreCategory.Experience),
}

/** Computed price bucket for client-side filtering. */
enum class PriceBucket { Under50, Under100, Under200, Above200 }

fun Int.toPriceBucket(): PriceBucket? = when {
    this in 1..49 -> PriceBucket.Under50
    this in 50..99 -> PriceBucket.Under100
    this in 100..199 -> PriceBucket.Under200
    this >= 200 -> PriceBucket.Above200
    else -> null
}

data class ExplorePoi(
    val id: String,
    val name: String,
    val chineseName: String,
    val city: String,
    val category: ExploreCategory,
    val subcategory: ExploreSubcategory? = null,
    val rating: Double,          // 0.0 = no rating — use hasRating
    val costPerPerson: Int = 0,  // 0 = unknown
    val priceHint: String,       // legacy display string
    val description: String,
    val address: String? = null,
    val phone: String? = null,
    val openingHours: String? = null,
    val mapUrl: String? = null,
    val photoUrl: String? = null,
    val businessArea: String? = null,
    val distanceMeters: Int? = null,  // null = unknown / no location
    val bookingCandidates: List<BookingCandidate> = emptyList(),
    val sourceLabel: String? = null,
    val coordinates: Coordinates? = null,
    /** editorial overlay (Issue #49, optional superset) */
    val editorialSummary: String? = null,
    val editorialBadges: List<String>? = null,
) {
    val hasRating: Boolean get() = rating > 0.0
    val priceBucket: PriceBucket? get() = costPerPerson.toPriceBucket()
}

/** UGC feed mock card for Explore home page. NOT real user content. */
data class UgcFeedItem(
    val id: String,
    val cityId: String,          // matches AMAP_CITY_MAP key, e.g. "beijing"
    val title: String,
    val imageUrl: String,        // placeholder / Unsplash public URL
    val authorNick: String,
    val likeCount: Int,
)
