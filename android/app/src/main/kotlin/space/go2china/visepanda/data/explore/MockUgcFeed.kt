package space.go2china.visepanda.data.explore

/**
 * v0.3.22 (#47): Static mock UGC feed for Explore home page.
 *
 * THIS IS MOCK DATA — not real user content, not synced, not from any backend.
 * Community UGC is a later phase. Code uses it purely as a visual placeholder
 * that is city-aware. Content is clearly attributed as "VisePanda Editorial".
 */
object MockUgcFeed {

    // 9 cities matching AMAP_CITY_MAP keys
    val items: List<UgcFeedItem> = listOf(

        // Beijing
        UgcFeedItem("ugc-bj-1", "beijing", "Sunrise at the Great Wall — Mutianyu",
            "https://images.unsplash.com/photo-1508804185872-d7badad00f7d?w=400&q=80",
            "Sarah_Travels", 382),
        UgcFeedItem("ugc-bj-2", "beijing", "Best Peking Duck spots 2025",
            "https://images.unsplash.com/photo-1535083783855-aaab4b870b9a?w=400&q=80",
            "FoodieKing", 214),
        UgcFeedItem("ugc-bj-3", "beijing", "Hidden hutong courtyards worth finding",
            "https://images.unsplash.com/photo-1547981609-4b6bfe67ca0b?w=400&q=80",
            "WanderlustLi", 156),
        UgcFeedItem("ugc-bj-4", "beijing", "Temple of Heaven at dusk: a photo guide",
            "https://images.unsplash.com/photo-1523437255580-75b7d1c785a3?w=400&q=80",
            "LensWalker", 290),

        // Shanghai
        UgcFeedItem("ugc-sh-1", "shanghai", "The Bund at golden hour — best spots",
            "https://images.unsplash.com/photo-1474181487882-5abf3f0ba6c2?w=400&q=80",
            "SkylineChaser", 445),
        UgcFeedItem("ugc-sh-2", "shanghai", "Xiaolongbao hunt: 5 must-try spots",
            "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400&q=80",
            "DumplingQueen", 388),
        UgcFeedItem("ugc-sh-3", "shanghai", "French Concession café crawl",
            "https://images.unsplash.com/photo-1559925393-8be0ec4767c8?w=400&q=80",
            "CaféHopper", 211),
        UgcFeedItem("ugc-sh-4", "shanghai", "Yu Garden: tips for avoiding crowds",
            "https://images.unsplash.com/photo-1582379590393-09eddd0c671d?w=400&q=80",
            "SmartTraveller", 173),

        // Chengdu
        UgcFeedItem("ugc-cd-1", "chengdu", "Panda Base morning visit guide",
            "https://images.unsplash.com/photo-1564349683136-77e08dba1ef7?w=400&q=80",
            "PandaFan99", 512),
        UgcFeedItem("ugc-cd-2", "chengdu", "Chengdu hotpot: a spice-level survival guide",
            "https://images.unsplash.com/photo-1582169296194-e4d644c48063?w=400&q=80",
            "SpiceHunter", 334),
        UgcFeedItem("ugc-cd-3", "chengdu", "Jinli Street — skip the tourist traps",
            "https://images.unsplash.com/photo-1508804052814-cd3ba865a116?w=400&q=80",
            "HonestGuide", 198),
        UgcFeedItem("ugc-cd-4", "chengdu", "Kuanzhai Alley: photo & food diary",
            "https://images.unsplash.com/photo-1509316785289-025f5b846b35?w=400&q=80",
            "AlleyWalker", 167),

        // Xi'an
        UgcFeedItem("ugc-xa-1", "xian", "Terracotta Warriors — what nobody tells you",
            "https://images.unsplash.com/photo-1547891654-e66ed7ebb968?w=400&q=80",
            "HistoryBuff", 421),
        UgcFeedItem("ugc-xa-2", "xian", "Muslim Quarter street food marathon",
            "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400&q=80",
            "StreetFoodPro", 298),
        UgcFeedItem("ugc-xa-3", "xian", "City Wall cycling: sunrise to sunset",
            "https://images.unsplash.com/photo-1518183214770-9cffbec72538?w=400&q=80",
            "CyclingNomad", 176),
        UgcFeedItem("ugc-xa-4", "xian", "Best roujiamo (Chinese burger) places",
            "https://images.unsplash.com/photo-1533089860892-a7c6f0a88666?w=400&q=80",
            "LocalEats", 144),

        // Guangzhou
        UgcFeedItem("ugc-gz-1", "guangzhou", "Dim sum deep dive: 6 classic teahouses",
            "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400&q=80",
            "YumChaLover", 367),
        UgcFeedItem("ugc-gz-2", "guangzhou", "Canton Tower night views",
            "https://images.unsplash.com/photo-1519944801169-45c3fb9e9f7f?w=400&q=80",
            "NightOwlGZ", 228),
        UgcFeedItem("ugc-gz-3", "guangzhou", "Shamian Island: colonial architecture walk",
            "https://images.unsplash.com/photo-1556746834-1cb5b8fabd54?w=400&q=80",
            "ArchWalker", 152),
        UgcFeedItem("ugc-gz-4", "guangzhou", "Guangzhou flower market guide",
            "https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=400&q=80",
            "FloralFinder", 119),

        // Hangzhou
        UgcFeedItem("ugc-hz-1", "hangzhou", "West Lake sunrise: best viewing spots",
            "https://images.unsplash.com/photo-1544914379-806667cd5a45?w=400&q=80",
            "LakeChaser", 389),
        UgcFeedItem("ugc-hz-2", "hangzhou", "Longjing tea plantations — picking season",
            "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&q=80",
            "TeaWalker", 261),
        UgcFeedItem("ugc-hz-3", "hangzhou", "Wuzhen water town day trip from HZ",
            "https://images.unsplash.com/photo-1542052888729-b78ed01f3b48?w=400&q=80",
            "DayTripPro", 195),
        UgcFeedItem("ugc-hz-4", "hangzhou", "Best xiaolongbao outside Shanghai",
            "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400&q=80",
            "DumplingDiary", 142),

        // Suzhou
        UgcFeedItem("ugc-sz-1", "suzhou", "Classical gardens: Humble Administrator's Guide",
            "https://images.unsplash.com/photo-1580619305218-8423a7ef79b4?w=400&q=80",
            "GardenLover", 303),
        UgcFeedItem("ugc-sz-2", "suzhou", "Silk embroidery: where to buy authentically",
            "https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=400&q=80",
            "SilkRoad2025", 174),
        UgcFeedItem("ugc-sz-3", "suzhou", "Tongli water town — perfect day trip",
            "https://images.unsplash.com/photo-1542052888729-b78ed01f3b48?w=400&q=80",
            "CanalsRider", 218),
        UgcFeedItem("ugc-sz-4", "suzhou", "Pingjiang Road food & photography walk",
            "https://images.unsplash.com/photo-1581578017093-cd30bb71eff8?w=400&q=80",
            "StreetPhotoSZ", 148),

        // Chongqing
        UgcFeedItem("ugc-cq-1", "chongqing", "Hongya Cave: best times & photo tips",
            "https://images.unsplash.com/photo-1557456432-b5abf36c6f44?w=400&q=80",
            "CaveExplorer", 418),
        UgcFeedItem("ugc-cq-2", "chongqing", "Chongqing hotpot vs Chengdu — the verdict",
            "https://images.unsplash.com/photo-1582169296194-e4d644c48063?w=400&q=80",
            "HotpotJudge", 337),
        UgcFeedItem("ugc-cq-3", "chongqing", "Eling Park — city view, fewer tourists",
            "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&q=80",
            "LocalSecrets", 189),
        UgcFeedItem("ugc-cq-4", "chongqing", "Jiefangbei night market guide",
            "https://images.unsplash.com/photo-1519802772250-a52a9af0eacb?w=400&q=80",
            "NightMarketCQ", 156),

        // Nanjing
        UgcFeedItem("ugc-nj-1", "nanjing", "Purple Mountain & Ming Tomb walk",
            "https://images.unsplash.com/photo-1523482580672-f109ba8cb9be?w=400&q=80",
            "HistoryWalker", 276),
        UgcFeedItem("ugc-nj-2", "nanjing", "Salted duck: Nanjing's iconic dish guide",
            "https://images.unsplash.com/photo-1533089860892-a7c6f0a88666?w=400&q=80",
            "DuckFanatic", 213),
        UgcFeedItem("ugc-nj-3", "nanjing", "Qinhuai River lantern season",
            "https://images.unsplash.com/photo-1548625361-58a9b86aa83b?w=400&q=80",
            "LanternSeeker", 294),
        UgcFeedItem("ugc-nj-4", "nanjing", "Presidential Palace — half day plan",
            "https://images.unsplash.com/photo-1543053080-af17af1fc0fd?w=400&q=80",
            "CultureNerd", 168),
    )

    fun forCity(cityId: String): List<UgcFeedItem> =
        items.filter { it.cityId == cityId }
}
