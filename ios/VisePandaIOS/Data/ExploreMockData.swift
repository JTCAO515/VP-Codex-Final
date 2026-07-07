import Foundation

struct ExploreUGCPost: Identifiable, Equatable {
    var id: String
    var cityId: String
    var title: String
    var author: String
    var likes: Int
    var category: ExploreCategory
    var imageName: String
}

enum ExploreMockData {
    // Mock-only community feed placeholder for Issue #48. These posts are local
    // static layout content until the real Community product exists.
    static let ugcPosts: [ExploreUGCPost] = ExploreCity.supported.flatMap { city in
        [
            ExploreUGCPost(
                id: "\(city.id)-food",
                cityId: city.id,
                title: "\(city.name) bites worth crossing town for",
                author: "Maya",
                likes: 128,
                category: .food,
                imageName: "fork.knife"
            ),
            ExploreUGCPost(
                id: "\(city.id)-sights",
                cityId: city.id,
                title: "A lazy afternoon route through \(city.name)",
                author: "Leo",
                likes: 96,
                category: .attractions,
                imageName: "camera"
            ),
            ExploreUGCPost(
                id: "\(city.id)-shopping",
                cityId: city.id,
                title: "Small gifts and local shelves to browse",
                author: "Anika",
                likes: 74,
                category: .shopping,
                imageName: "bag"
            ),
            ExploreUGCPost(
                id: "\(city.id)-tea",
                cityId: city.id,
                title: "Where I would pause for tea",
                author: "Jun",
                likes: 111,
                category: .experiences,
                imageName: "cup.and.saucer"
            )
        ]
    }

    static func posts(for cityId: String) -> [ExploreUGCPost] {
        ugcPosts.filter { $0.cityId == cityId }
    }
}
