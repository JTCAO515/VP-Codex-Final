import Foundation

enum ExploreCategory: String, CaseIterable, Identifiable, Codable {
    case food
    case attractions
    case hotels
    case shopping
    case experiences

    var id: String { rawValue }

    var title: String {
        switch self {
        case .food: "Food"
        case .attractions: "Sights"
        case .hotels: "Hotels"
        case .shopping: "Shopping"
        case .experiences: "Experiences"
        }
    }

    var icon: String {
        switch self {
        case .food: "fork.knife"
        case .attractions: "camera"
        case .hotels: "bed.double"
        case .shopping: "bag"
        case .experiences: "sparkles"
        }
    }

    var semanticKey: String {
        switch self {
        case .food: "food"
        case .attractions: "attractions"
        case .hotels: "hotels"
        case .shopping: "shopping"
        case .experiences: "experiences"
        }
    }

    static func from(refCategory: String) -> ExploreCategory {
        switch refCategory {
        case "attraction", "attractions":
            .attractions
        case "hotel", "hotels", "stay", "stays":
            .hotels
        case "shopping":
            .shopping
        case "experience", "experiences":
            .experiences
        default:
            .food
        }
    }

    var subcategories: [ExploreSubcategory] {
        switch self {
        case .food:
            [
                .init(title: "All", key: "food"),
                .init(title: "Hotpot", key: "food.hotpot"),
                .init(title: "Sichuan", key: "food.sichuan"),
                .init(title: "Cantonese", key: "food.cantonese"),
                .init(title: "Japanese", key: "food.japanese"),
                .init(title: "BBQ", key: "food.bbq"),
                .init(title: "Dessert", key: "food.dessert"),
                .init(title: "Fast food", key: "food.fastfood"),
                .init(title: "Cafe", key: "food.cafe")
            ]
        case .attractions:
            [
                .init(title: "All", key: "attractions"),
                .init(title: "Scenic", key: "attractions.scenic"),
                .init(title: "Parks", key: "attractions.park"),
                .init(title: "Museums", key: "attractions.museum"),
                .init(title: "Temples", key: "attractions.temple")
            ]
        case .hotels:
            [
                .init(title: "All", key: "hotels"),
                .init(title: "Star hotels", key: "hotels.star"),
                .init(title: "Economy", key: "hotels.economy"),
                .init(title: "Hostels", key: "hotels.hostel")
            ]
        case .shopping:
            [
                .init(title: "All", key: "shopping"),
                .init(title: "Malls", key: "shopping.mall"),
                .init(title: "Specialty streets", key: "shopping.specialty"),
                .init(title: "Supermarkets", key: "shopping.supermarket")
            ]
        case .experiences:
            [
                .init(title: "All", key: "experiences"),
                .init(title: "Massage", key: "experiences.massage"),
                .init(title: "Baths", key: "experiences.bath"),
                .init(title: "SPA", key: "experiences.spa"),
                .init(title: "Tea houses", key: "experiences.teahouse"),
                .init(title: "KTV", key: "experiences.ktv")
            ]
        }
    }
}

struct ExploreCity: Identifiable, Equatable {
    var id: String
    var name: String
    var chineseName: String

    static let supported: [ExploreCity] = [
        .init(id: "beijing", name: "Beijing", chineseName: "北京"),
        .init(id: "shanghai", name: "Shanghai", chineseName: "上海"),
        .init(id: "chengdu", name: "Chengdu", chineseName: "成都"),
        .init(id: "xian", name: "Xi'an", chineseName: "西安"),
        .init(id: "guangzhou", name: "Guangzhou", chineseName: "广州"),
        .init(id: "hangzhou", name: "Hangzhou", chineseName: "杭州"),
        .init(id: "suzhou", name: "Suzhou", chineseName: "苏州"),
        .init(id: "chongqing", name: "Chongqing", chineseName: "重庆"),
        .init(id: "nanjing", name: "Nanjing", chineseName: "南京")
    ]

    static func city(id: String) -> ExploreCity {
        supported.first { $0.id == id } ?? supported[1]
    }
}

struct ExploreSubcategory: Identifiable, Equatable {
    var title: String
    var key: String
    var id: String { key }
}

enum ExploreDistance: String, CaseIterable, Identifiable {
    case nearby500
    case nearby1000
    case nearby3000
    case nearby5000
    case citywide

    var id: String { rawValue }

    var title: String {
        switch self {
        case .nearby500: "500m"
        case .nearby1000: "1km"
        case .nearby3000: "3km"
        case .nearby5000: "5km"
        case .citywide: "All city"
        }
    }

    var radius: Int? {
        switch self {
        case .nearby500: 500
        case .nearby1000: 1_000
        case .nearby3000: 3_000
        case .nearby5000: 5_000
        case .citywide: nil
        }
    }
}

enum ExploreSort: String, CaseIterable, Identifiable {
    case smart
    case nearest
    case rating
    case priceLow

    var id: String { rawValue }

    var title: String {
        switch self {
        case .smart: "Smart"
        case .nearest: "Nearest"
        case .rating: "Highest rated"
        case .priceLow: "Lowest price"
        }
    }
}

enum ExplorePriceFilter: String, CaseIterable, Identifiable {
    case under50
    case between50And100
    case between100And200
    case above200

    var id: String { rawValue }

    var title: String {
        switch self {
        case .under50: "¥0-50"
        case .between50And100: "¥50-100"
        case .between100And200: "¥100-200"
        case .above200: "¥200+"
        }
    }

    func contains(_ cost: Double) -> Bool {
        switch self {
        case .under50: cost < 50
        case .between50And100: cost >= 50 && cost < 100
        case .between100And200: cost >= 100 && cost < 200
        case .above200: cost >= 200
        }
    }
}

struct ExploreFilterState: Equatable {
    var priceFilters: Set<ExplorePriceFilter> = []
    var requiresRating = false

    var activeCount: Int {
        priceFilters.count + (requiresRating ? 1 : 0)
    }
}

struct TravelerFit: Equatable {
    var firstTimerFit: Bool?
    var paymentFriendliness: String?
    var languageDifficulty: String?
    var routeFit: String?
    var rainyDayFit: Bool?
    var nightFit: Bool?
    var crowdRisk: String?
    var luggageFit: Bool?
    var watchOut: String?

    var isEmpty: Bool {
        firstTimerFit == nil &&
            paymentFriendliness == nil &&
            languageDifficulty == nil &&
            routeFit == nil &&
            rainyDayFit == nil &&
            nightFit == nil &&
            crowdRisk == nil &&
            luggageFit == nil &&
            watchOut == nil
    }
}

struct ExploreAmapResponse: Decodable {
    var ok: Bool
    var cityId: String
    var type: String
    var page: Int?
    var hasMore: Bool?
    var pois: [ExploreAmapPoi]
}

struct ExploreAmapPoi: Decodable, Identifiable, Equatable {
    var id: String
    var name: String
    var type: String?
    var address: String?
    var tel: String?
    var opentimeWeek: String?
    var businessArea: String?
    var location: String?
    var bizExt: ExploreBizExt?
    var photos: [ExploreAmapPhoto]?
    var editorial: ExploreEditorial?

    enum CodingKeys: String, CodingKey {
        case id
        case name
        case type
        case address
        case tel
        case opentimeWeek = "opentime_week"
        case businessArea = "business_area"
        case location
        case bizExt = "biz_ext"
        case photos
        case editorial
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decode(String.self, forKey: .id)
        name = try container.decode(String.self, forKey: .name)
        type = try container.decodeIfPresent(String.self, forKey: .type)
        address = try container.decodeFlexibleString(forKey: .address)
        tel = try container.decodeFlexibleString(forKey: .tel)
        opentimeWeek = try container.decodeIfPresent(String.self, forKey: .opentimeWeek)
        businessArea = try container.decodeIfPresent(String.self, forKey: .businessArea)
        location = try container.decodeIfPresent(String.self, forKey: .location)
        bizExt = try? container.decodeIfPresent(ExploreBizExt.self, forKey: .bizExt)
        photos = try container.decodeIfPresent([ExploreAmapPhoto].self, forKey: .photos)
        editorial = try container.decodeIfPresent(ExploreEditorial.self, forKey: .editorial)
    }

    var ratingValue: Double? {
        Double(bizExt?.rating ?? "")
    }

    var costValue: Double? {
        Double(bizExt?.cost ?? "")
    }

    var photoURL: String? {
        photos?.compactMap(\.url).first
    }

    var coordinates: Coordinates? {
        guard let location else { return nil }
        let parts = location.split(separator: ",").compactMap { Double($0) }
        guard parts.count == 2 else { return nil }
        return Coordinates(lat: parts[1], lng: parts[0])
    }

    var travelerFit: TravelerFit? {
        TravelerFitDeriver.derive(from: self)
    }
}

struct ExploreBizExt: Decodable, Equatable {
    var rating: String?
    var cost: String?

    enum CodingKeys: String, CodingKey {
        case rating
        case cost
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        rating = try container.decodeFlexibleString(forKey: .rating)
        cost = try container.decodeFlexibleString(forKey: .cost)
    }
}

struct ExploreAmapPhoto: Decodable, Equatable {
    var url: String?
    var title: String?
}

struct ExploreEditorial: Decodable, Equatable {
    var summary: String?
    var tags: [String]?
    var badges: [String]?
    var badge: String?
}

private extension KeyedDecodingContainer {
    func decodeFlexibleString(forKey key: Key) throws -> String? {
        if let value = try? decodeIfPresent(String.self, forKey: key) {
            return value.isEmpty ? nil : value
        }
        if let values = try? decodeIfPresent([String].self, forKey: key) {
            return values.first { !$0.isEmpty }
        }
        return nil
    }
}

private enum TravelerFitDeriver {
    static func derive(from poi: ExploreAmapPoi) -> TravelerFit? {
        let text = searchText(for: poi)
        guard !text.isEmpty else { return nil }

        var fit = TravelerFit()
        fit.firstTimerFit = firstTimerFit(text: text, rating: poi.ratingValue)
        fit.paymentFriendliness = paymentFriendliness(text: text)
        fit.languageDifficulty = languageDifficulty(text: text)
        fit.rainyDayFit = rainyDayFit(text: text)
        fit.nightFit = nightFit(openingText: poi.opentimeWeek)
        fit.crowdRisk = crowdRisk(text: text, rating: poi.ratingValue)
        fit.luggageFit = luggageFit(text: text)
        fit.watchOut = watchOut(for: fit)

        return fit.isEmpty ? nil : fit
    }

    private static func searchText(for poi: ExploreAmapPoi) -> String {
        (
            [
                poi.type,
                poi.address,
                poi.businessArea,
                poi.opentimeWeek,
                poi.editorial?.summary,
                poi.editorial?.badge
            ] +
            (poi.editorial?.tags ?? []) +
            (poi.editorial?.badges ?? [])
        )
        .compactMap { $0 }
        .joined(separator: " ")
        .lowercased()
        // Curated POI tags use kebab-case (e.g. "english-menu", from
        // scripts/curated-seeds/*.sql), but the keyword lists below match on
        // space-separated phrases ("english menu") — without this, every
        // hyphenated tag would silently never match anything.
        .replacingOccurrences(of: "-", with: " ")
    }

    private static func firstTimerFit(text: String, rating: Double?) -> Bool? {
        if containsAny(text, ["first-timer", "first timer", "beginner", "classic", "landmark", "vp pick"]) {
            return true
        }
        if containsAny(text, ["local-only", "locals only", "hard to find"]) {
            return false
        }
        if let rating, rating >= 4.6, containsAny(text, ["museum", "scenic", "sight", "attraction"]) {
            return true
        }
        return nil
    }

    private static func paymentFriendliness(text: String) -> String? {
        if containsAny(text, ["card accepted", "visa accepted", "mastercard accepted", "foreign card"]) {
            return "Card accepted"
        }
        if containsAny(text, ["cash only", "cash-only"]) {
            return "Cash only"
        }
        return nil
    }

    private static func languageDifficulty(text: String) -> String? {
        if containsAny(text, ["english menu", "english service", "foreigner-friendly", "foreign visitor"]) {
            return "Lower"
        }
        if containsAny(text, ["chinese only", "no english", "local-only"]) {
            return "Higher"
        }
        return nil
    }

    private static func rainyDayFit(text: String) -> Bool? {
        if containsAny(text, ["museum", "mall", "shopping", "hotel", "spa", "massage", "ktv", "cinema", "teahouse"]) {
            return true
        }
        if containsAny(text, ["park", "garden", "mountain", "beach", "outdoor"]) {
            return false
        }
        return nil
    }

    private static func nightFit(openingText: String?) -> Bool? {
        guard let openingText, !openingText.isEmpty else { return nil }
        let text = openingText.lowercased()
        if containsAny(text, ["24小时", "全天", "24h", "24 h", "all day"]) {
            return true
        }

        let hours = text
            .split { !$0.isNumber && $0 != ":" }
            .compactMap { Int($0.split(separator: ":").first ?? "") }
        guard let latestHour = hours.max() else { return nil }
        return latestHour >= 21
    }

    private static func crowdRisk(text: String, rating: Double?) -> String? {
        if containsAny(text, ["crowded", "busy", "popular", "hot spot", "landmark"]) || (rating ?? 0) >= 4.7 {
            return "High"
        }
        return nil
    }

    private static func luggageFit(text: String) -> Bool? {
        if containsAny(text, ["hotel", "mall", "airport", "railway station", "train station"]) {
            return true
        }
        if containsAny(text, ["park", "garden", "mountain", "temple", "crowded"]) {
            return false
        }
        return nil
    }

    private static func watchOut(for fit: TravelerFit) -> String? {
        if fit.crowdRisk == "High" {
            return "Expect crowds at peak times."
        }
        if fit.rainyDayFit == false {
            return "Best in dry weather."
        }
        return nil
    }

    private static func containsAny(_ text: String, _ needles: [String]) -> Bool {
        needles.contains { text.contains($0) }
    }
}
