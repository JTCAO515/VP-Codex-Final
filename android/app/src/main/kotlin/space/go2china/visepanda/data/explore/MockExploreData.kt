package space.go2china.visepanda.data.explore

/**
 * v0.3.14: static mock POI catalog for the Explore screen. Mock-first per
 * operator constraint — no real Amap/POI API call is made; "Add to Trip"
 * writes one of these directly into the active trip via
 * [space.go2china.visepanda.data.repository.TripRepository.addPoiToDay].
 */
enum class ExploreCategory {
    Attraction,
    Food,
    Stay,
}

data class ExplorePoi(
    val id: String,
    val name: String,
    val chineseName: String,
    val city: String,
    val category: ExploreCategory,
    val rating: Double,
    val priceHint: String,
    val description: String,
    val address: String? = null,
    val phone: String? = null,
    val openingHours: String? = null,
    val mapUrl: String? = null,
    val bookingCandidates: List<space.go2china.visepanda.data.model.BookingCandidate> = emptyList(),
    val sourceLabel: String? = null,
    val coordinates: space.go2china.visepanda.data.model.Coordinates? = null
)

object MockExploreData {
    val cities = listOf("Beijing", "Shanghai")

    val pois: List<ExplorePoi> = listOf(
        // Beijing
        ExplorePoi("attr-bj-1", "Forbidden City", "故宫", "Beijing", ExploreCategory.Attraction, 4.8, "¥60", "The imperial palace complex at the heart of old Beijing."),
        ExplorePoi("attr-bj-2", "Great Wall · Mutianyu", "长城·慕田峪", "Beijing", ExploreCategory.Attraction, 4.7, "¥180", "A well-preserved, less crowded stretch of the Great Wall."),
        ExplorePoi("attr-bj-3", "Temple of Heaven", "天坛", "Beijing", ExploreCategory.Attraction, 4.6, "¥34", "A Ming-dynasty temple complex and park."),
        ExplorePoi("food-bj-1", "Da Dong Roast Duck", "大董烤鸭", "Beijing", ExploreCategory.Food, 4.6, "¥¥¥", "Modern take on Beijing's signature roast duck."),
        ExplorePoi("food-bj-2", "Hutong Noodle House", "胡同面馆", "Beijing", ExploreCategory.Food, 4.4, "¥", "Casual hand-pulled noodles near the old hutongs."),
        ExplorePoi("stay-bj-1", "Beijing City-Center Hotel", "北京市中心酒店", "Beijing", ExploreCategory.Stay, 4.5, "¥600/night", "Central location close to the subway and main sights."),
        ExplorePoi("stay-bj-2", "Hutong Courtyard Inn", "胡同四合院客栈", "Beijing", ExploreCategory.Stay, 4.3, "¥450/night", "A restored courtyard house in a historic hutong."),
        // Shanghai
        ExplorePoi("attr-sh-1", "The Bund", "外滩", "Shanghai", ExploreCategory.Attraction, 4.7, "Free", "Riverfront promenade with colonial-era architecture."),
        ExplorePoi("attr-sh-2", "Yu Garden", "豫园", "Shanghai", ExploreCategory.Attraction, 4.5, "¥40", "A classical Ming-dynasty garden in the old city."),
        ExplorePoi("attr-sh-3", "Nanjing Road", "南京路", "Shanghai", ExploreCategory.Attraction, 4.4, "Free", "Shanghai's best-known shopping and walking street."),
        ExplorePoi("food-sh-1", "Xiaolongbao House", "小笼包馆", "Shanghai", ExploreCategory.Food, 4.6, "¥¥", "Classic Shanghainese soup dumplings."),
        ExplorePoi("food-sh-2", "Yunnan Road Food Street", "云南路美食街", "Shanghai", ExploreCategory.Food, 4.3, "¥", "A street of local snack stalls near People's Square."),
        ExplorePoi("stay-sh-1", "Shanghai City-Center Hotel", "上海市中心酒店", "Shanghai", ExploreCategory.Stay, 4.5, "¥750/night", "Walking distance to the Bund and Nanjing Road."),
        ExplorePoi("stay-sh-2", "French Concession Boutique Hotel", "法租界精品酒店", "Shanghai", ExploreCategory.Stay, 4.6, "¥900/night", "A quiet boutique hotel in the former French Concession."),
    )
}
