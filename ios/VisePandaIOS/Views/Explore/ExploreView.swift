import SwiftUI

struct ExploreView: View {
    @EnvironmentObject private var store: TripStore
    @State private var selectedCategory = "Attractions"

    private let categories = ["Attractions", "Food", "Hotels", "Activities"]
    private let places = ExplorePlace.seed

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                header
                searchBar
                categoryTabs

                ForEach(places) { place in
                    ExplorePlaceCard(place: place) {
                        store.addPlaceToPlan(place.name)
                    }
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 14)
            .padding(.bottom, 20)
        }
        .background(VPColor.paper)
    }

    private var header: some View {
        HStack(alignment: .top) {
            VStack(alignment: .leading, spacing: 3) {
                Text("Explore")
                    .font(VPFont.display(26))
                    .foregroundStyle(VPColor.ink)
                Text("Shanghai")
                    .font(VPFont.body(13, weight: .semibold))
                    .foregroundStyle(VPColor.inkSoft)
            }

            Spacer()

            HStack(spacing: 6) {
                Image(systemName: "location")
                Text("Shanghai")
                Image(systemName: "chevron.down")
            }
            .font(VPFont.body(13, weight: .semibold))
            .foregroundStyle(VPColor.inkMuted)
            .padding(.horizontal, 10)
            .padding(.vertical, 8)
            .background(VPColor.paperWarm)
            .clipShape(Capsule())
        }
    }

    private var searchBar: some View {
        HStack(spacing: 10) {
            Image(systemName: "magnifyingglass")
                .foregroundStyle(VPColor.inkSoft)
            Text("Search places, food, hotels")
                .font(VPFont.body(15))
                .foregroundStyle(VPColor.inkSoft)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(VPColor.paperSoft)
        .clipShape(Capsule())
        .overlay {
            Capsule()
                .stroke(VPColor.outline, lineWidth: 1)
        }
    }

    private var categoryTabs: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(categories, id: \.self) { category in
                    Button {
                        selectedCategory = category
                    } label: {
                        VPChip(title: category, selected: selectedCategory == category)
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }
}

private struct ExplorePlace: Identifiable {
    var id: String
    var chineseName: String
    var name: String
    var subtitle: String
    var reason: String
    var rating: String?

    static let seed = [
        ExplorePlace(
            id: "yu-garden",
            chineseName: "豫园",
            name: "Yu Garden",
            subtitle: "Static starter guide",
            reason: "Classical Ming garden - walkable in rain, covered pavilions.",
            rating: nil
        ),
        ExplorePlace(
            id: "jing-an-temple",
            chineseName: "静安寺",
            name: "Jing'an Temple",
            subtitle: "Static starter guide",
            reason: "Central, 30-min visit, easy metro from your hotel.",
            rating: nil
        )
    ]
}

private struct ExplorePlaceCard: View {
    let place: ExplorePlace
    let onAdd: () -> Void

    var body: some View {
        VPCard(padding: 0) {
            VStack(alignment: .leading, spacing: 0) {
                ZStack(alignment: .topTrailing) {
                    LinearGradient(
                        colors: [Color(hex: 0xFFD6D1), Color(hex: 0xFFF4EA)],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                    .frame(height: 116)

                    Text(place.chineseName)
                        .font(VPFont.display(34))
                        .foregroundStyle(VPColor.ink)
                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomLeading)
                        .padding(18)

                    Button {} label: {
                        Image(systemName: "bookmark")
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundStyle(VPColor.inkMuted)
                            .frame(width: 42, height: 42)
                            .background(VPColor.paperSoft.opacity(0.85))
                            .clipShape(Circle())
                    }
                    .padding(12)
                }

                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        VStack(alignment: .leading, spacing: 3) {
                            Text(place.name)
                                .font(VPFont.body(18, weight: .bold))
                                .foregroundStyle(VPColor.ink)
                            Text(place.subtitle)
                                .font(VPFont.body(12, weight: .semibold))
                                .foregroundStyle(VPColor.inkSoft)
                        }
                        Spacer()
                        if let rating = place.rating {
                            Label(rating, systemImage: "star.fill")
                                .font(VPFont.body(13, weight: .bold))
                                .foregroundStyle(VPColor.gold)
                        }
                    }

                    Text("Why this fits · \(place.reason)")
                        .font(VPFont.body(13, weight: .semibold))
                        .foregroundStyle(VPColor.inkMuted)
                        .padding(12)
                        .background(VPColor.paperWarm)
                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))

                    HStack(spacing: 10) {
                        Button(action: onAdd) {
                            Text("Add to Plan")
                                .font(VPFont.body(15, weight: .bold))
                                .foregroundStyle(VPColor.paperSoft)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 13)
                                .background(VPColor.cinnabar)
                                .clipShape(Capsule())
                        }

                        Button {} label: {
                            Text("Save")
                                .font(VPFont.body(15, weight: .bold))
                                .foregroundStyle(VPColor.inkMuted)
                                .padding(.horizontal, 18)
                                .padding(.vertical, 13)
                                .background(VPColor.paperSoft)
                                .clipShape(Capsule())
                                .overlay {
                                    Capsule()
                                        .stroke(VPColor.outline, lineWidth: 1)
                                }
                        }

                        Button {} label: {
                            Image(systemName: "mappin")
                                .font(.system(size: 17, weight: .bold))
                                .foregroundStyle(VPColor.inkMuted)
                                .frame(width: 46, height: 46)
                                .background(VPColor.paperSoft)
                                .clipShape(Circle())
                                .overlay {
                                    Circle()
                                        .stroke(VPColor.outline, lineWidth: 1)
                                }
                        }
                    }
                }
                .padding(16)
            }
        }
        .clipShape(RoundedRectangle(cornerRadius: VPRadius.lg, style: .continuous))
    }
}
