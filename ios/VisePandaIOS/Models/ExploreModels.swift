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
