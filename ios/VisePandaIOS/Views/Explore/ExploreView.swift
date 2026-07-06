import CoreLocation
import SwiftUI

struct ExploreView: View {
    @EnvironmentObject private var store: TripStore
    @AppStorage("space.go2china.visepanda.explore.city") private var selectedCityId = "shanghai"
    @State private var showingCityPicker = false
    @State private var comingSoonPost: ExploreUGCPost?
    @State private var path: [ExploreRoute] = []

    var body: some View {
        NavigationStack(path: $path) {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    header
                    searchBar
                    categoryGrid
                    ugcFeed
                }
                .padding(.horizontal, 20)
                .padding(.top, 14)
                .padding(.bottom, 22)
            }
            .background(VPColor.paper)
            .navigationBarHidden(true)
            .navigationDestination(for: ExploreRoute.self) { route in
                ExploreChannelView(cityId: $selectedCityId, category: route.category, focusRef: route.ref)
                    .onAppear { store.setBottomBarHidden(true) }
                    .onDisappear { store.setBottomBarHidden(false) }
            }
            .sheet(isPresented: $showingCityPicker) {
                CityPickerSheet(selectedCityId: $selectedCityId)
                    .presentationDetents([.medium])
            }
            .alert("Coming soon", isPresented: Binding(
                get: { comingSoonPost != nil },
                set: { if !$0 { comingSoonPost = nil } }
            )) {
                Button("OK", role: .cancel) {}
            } message: {
                Text("Community posts are local placeholders in this build.")
            }
            .onAppear(perform: openPendingRef)
            .onChange(of: store.pendingExploreRef) { _, _ in
                openPendingRef()
            }
        }
    }

    private var city: ExploreCity {
        ExploreCity.city(id: selectedCityId)
    }

    private func openPendingRef() {
        guard let ref = store.pendingExploreRef else { return }
        selectedCityId = ref.cityId
        path = [ExploreRoute(ref: ref)]
        store.pendingExploreRef = nil
    }

    private var header: some View {
        HStack(alignment: .center, spacing: 12) {
            Button {
                showingCityPicker = true
            } label: {
                HStack(spacing: 7) {
                    Image(systemName: "location")
                    Text(city.chineseName)
                    Image(systemName: "chevron.down")
                        .font(.system(size: 11, weight: .bold))
                }
                .font(VPFont.body(14, weight: .bold))
                .foregroundStyle(VPColor.ink)
                .padding(.horizontal, 12)
                .padding(.vertical, 10)
                .background(VPColor.paperWarm)
                .clipShape(Capsule())
            }
            .buttonStyle(.plain)

            Spacer()

            Text("Explore")
                .font(VPFont.display(28))
                .foregroundStyle(VPColor.ink)
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

    private var categoryGrid: some View {
        HStack(spacing: 9) {
            ForEach(ExploreCategory.allCases) { category in
                NavigationLink(value: ExploreRoute(category: category)) {
                    VStack(spacing: 8) {
                        Image(systemName: category.icon)
                            .font(.system(size: 19, weight: .bold))
                            .foregroundStyle(VPColor.cinnabar)
                            .frame(width: 42, height: 42)
                            .background(VPColor.cinnabar.opacity(0.1))
                            .clipShape(Circle())

                        Text(category.title)
                            .font(VPFont.body(11, weight: .bold))
                            .foregroundStyle(VPColor.inkMuted)
                            .lineLimit(1)
                            .minimumScaleFactor(0.75)
                    }
                    .frame(maxWidth: .infinity)
                }
                .buttonStyle(.plain)
            }
        }
    }

    private var ugcFeed: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("\(city.name) notes")
                .font(VPFont.body(18, weight: .bold))
                .foregroundStyle(VPColor.ink)

            let posts = ExploreMockData.posts(for: city.id)
            LazyVGrid(columns: [GridItem(.flexible(), spacing: 12), GridItem(.flexible(), spacing: 12)], spacing: 12) {
                ForEach(posts) { post in
                    Button {
                        comingSoonPost = post
                    } label: {
                        UGCPostCard(post: post)
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }
}

private struct ExploreRoute: Hashable {
    var category: ExploreCategory
    var ref: ButlerExploreRef?

    init(category: ExploreCategory) {
        self.category = category
        ref = nil
    }

    init(ref: ButlerExploreRef) {
        category = ExploreCategory.from(refCategory: ref.category)
        self.ref = ref
    }
}

private struct CityPickerSheet: View {
    @Binding var selectedCityId: String
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List(ExploreCity.supported) { city in
                Button {
                    selectedCityId = city.id
                    dismiss()
                } label: {
                    HStack {
                        VStack(alignment: .leading, spacing: 3) {
                            Text(city.chineseName)
                                .font(VPFont.body(17, weight: .bold))
                                .foregroundStyle(VPColor.ink)
                            Text(city.name)
                                .font(VPFont.body(13))
                                .foregroundStyle(VPColor.inkSoft)
                        }
                        Spacer()
                        if selectedCityId == city.id {
                            Image(systemName: "checkmark")
                                .foregroundStyle(VPColor.cinnabar)
                        }
                    }
                }
            }
            .navigationTitle("Choose city")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

private struct UGCPostCard: View {
    let post: ExploreUGCPost

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            ZStack {
                LinearGradient(
                    colors: [VPColor.paperWarm, VPColor.cinnabar.opacity(0.15), VPColor.sage.opacity(0.16)],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                Image(systemName: post.imageName)
                    .font(.system(size: 26, weight: .bold))
                    .foregroundStyle(VPColor.ink.opacity(0.72))
            }
            .frame(height: post.id.hasSuffix("sights") ? 132 : 104)

            VStack(alignment: .leading, spacing: 8) {
                Text(post.title)
                    .font(VPFont.body(14, weight: .bold))
                    .foregroundStyle(VPColor.ink)
                    .lineLimit(3)
                    .fixedSize(horizontal: false, vertical: true)

                HStack {
                    Text(post.author)
                    Spacer()
                    Label(String(post.likes), systemImage: "heart")
                }
                .font(VPFont.body(11, weight: .semibold))
                .foregroundStyle(VPColor.inkSoft)
            }
            .padding(12)
        }
        .background(VPColor.paperSoft)
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        .overlay {
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(VPColor.outline.opacity(0.75), lineWidth: 1)
        }
    }
}

private struct ExploreChannelView: View {
    @EnvironmentObject private var store: TripStore
    @Environment(\.dismiss) private var dismiss
    @Binding var cityId: String
    let category: ExploreCategory

    @StateObject private var locationProvider = ExploreLocationProvider()
    @State private var selectedSubcategory: ExploreSubcategory
    @State private var distance: ExploreDistance = .citywide
    @State private var sort: ExploreSort = .smart
    @State private var filters = ExploreFilterState()
    @State private var draftFilters = ExploreFilterState()
    @State private var openPanel: FilterPanel?
    @State private var pois: [ExploreAmapPoi] = []
    @State private var page = 1
    @State private var hasMore = true
    @State private var isLoading = false
    @State private var notice: String?
    @State private var selectedPoi: ExploreAmapPoi?
    @State private var showingCityPicker = false
    @State private var didOpenFocusRef = false
    private let focusRef: ButlerExploreRef?

    init(cityId: Binding<String>, category: ExploreCategory, focusRef: ButlerExploreRef? = nil) {
        _cityId = cityId
        self.category = category
        self.focusRef = focusRef
        let key = focusRef?.subcategory ?? category.semanticKey
        let subcategory = category.subcategories.first { $0.key == key } ?? category.subcategories[0]
        _selectedSubcategory = State(initialValue: subcategory)
    }

    private var city: ExploreCity {
        ExploreCity.city(id: cityId)
    }

    private var visiblePois: [ExploreAmapPoi] {
        var result = pois
        if filters.requiresRating {
            result = result.filter { $0.ratingValue != nil }
        }
        if !filters.priceFilters.isEmpty {
            result = result.filter { poi in
                guard let cost = poi.costValue else { return false }
                return filters.priceFilters.contains { $0.contains(cost) }
            }
        }
        switch sort {
        case .smart, .nearest:
            return result
        case .rating:
            return result.sorted { ($0.ratingValue ?? -1) > ($1.ratingValue ?? -1) }
        case .priceLow:
            return result.sorted { ($0.costValue ?? .greatestFiniteMagnitude) < ($1.costValue ?? .greatestFiniteMagnitude) }
        }
    }

    var body: some View {
        ZStack(alignment: .top) {
            VPColor.paper.ignoresSafeArea()

            VStack(spacing: 0) {
                header
                filterBar

                ScrollView {
                    LazyVStack(spacing: 12) {
                        if let notice {
                            NoticeBanner(text: notice) {
                                self.notice = nil
                            }
                                .padding(.horizontal, 20)
                        }

                        if visiblePois.isEmpty && !isLoading {
                            EmptyExploreState(message: pois.isEmpty ? "Live Explore data is unavailable or empty right now." : "No merchants match these filters.")
                                .padding(.horizontal, 20)
                                .padding(.top, 28)
                        } else {
                            ForEach(visiblePois) { poi in
                                Button {
                                    selectedPoi = poi
                                } label: {
                                    MerchantCard(poi: poi, category: category, userLocation: locationProvider.coordinate)
                                }
                                .buttonStyle(.plain)
                                .padding(.horizontal, 20)
                                .onAppear {
                                    loadMoreIfNeeded(current: poi)
                                }
                            }
                        }

                        if isLoading {
                            ProgressView()
                                .tint(VPColor.cinnabar)
                                .padding(.vertical, 24)
                        }
                    }
                    .padding(.top, 12)
                    .padding(.bottom, 26)
                }
                .refreshable {
                    await load(reset: true)
                }
            }

            if let openPanel {
                Color.black.opacity(0.2)
                    .ignoresSafeArea()
                    .padding(.top, 106)
                    .onTapGesture {
                        withAnimation(.easeOut(duration: 0.18)) {
                            self.openPanel = nil
                        }
                    }

                dropdown(panel: openPanel)
                    .padding(.top, 106)
                    .transition(.move(edge: .top).combined(with: .opacity))
            }
        }
        .navigationBarBackButtonHidden(true)
        .sheet(isPresented: $showingCityPicker) {
            CityPickerSheet(selectedCityId: $cityId)
                .presentationDetents([.medium])
        }
        .sheet(item: $selectedPoi) { poi in
            PoiDetailSheet(
                poi: poi,
                category: category,
                onAddToTrip: {
                    store.addPlaceToPlan(poi.name)
                    notice = "Added \(poi.name) to your trip request."
                },
                onAskButler: {
                    store.prefillChat("Tell me about \(poi.name) in \(city.name). Is it a good fit for my trip, and where would you place it?")
                }
            )
            .presentationDetents([.medium, .large])
        }
        .task {
            locationProvider.requestOnce()
            await load(reset: true)
        }
        .onChange(of: locationProvider.status) { _, status in
            if status == .available, distance == .citywide {
                distance = .nearby3000
                Task { await load(reset: true) }
            } else if status == .denied || status == .failed {
                distance = .citywide
                notice = "Location is unavailable, so Explore is showing all-city results."
            }
        }
        .onChange(of: cityId) { _, _ in
            Task { await load(reset: true) }
        }
    }

    private var header: some View {
        HStack(spacing: 12) {
            Button {
                dismiss()
            } label: {
                Image(systemName: "chevron.left")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(VPColor.ink)
                    .frame(width: 38, height: 38)
                    .background(VPColor.paperSoft)
                    .clipShape(Circle())
                    .overlay { Circle().stroke(VPColor.outline, lineWidth: 1) }
            }

            Text(category.title)
                .font(VPFont.display(24))
                .foregroundStyle(VPColor.ink)

            Spacer()

            Button {
                showingCityPicker = true
            } label: {
                HStack(spacing: 6) {
                    Text(city.chineseName)
                    Image(systemName: "chevron.down")
                        .font(.system(size: 10, weight: .bold))
                }
                .font(VPFont.body(13, weight: .bold))
                .foregroundStyle(VPColor.inkMuted)
                .padding(.horizontal, 11)
                .padding(.vertical, 8)
                .background(VPColor.paperWarm)
                .clipShape(Capsule())
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 20)
        .padding(.top, 10)
        .padding(.bottom, 8)
        .background(VPColor.paper)
    }

    private var filterBar: some View {
        HStack(spacing: 0) {
            filterButton(.distance, title: distance.title)
            filterButton(.category, title: selectedSubcategory.title)
            filterButton(.sort, title: sort.title)
            filterButton(.filters, title: filters.activeCount == 0 ? "Filter" : "Filter·\(filters.activeCount)")
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(VPColor.paperSoft)
        .overlay(alignment: .bottom) {
            Rectangle()
                .fill(VPColor.outline.opacity(0.8))
                .frame(height: 1)
        }
    }

    private func filterButton(_ panel: FilterPanel, title: String) -> some View {
        Button {
            withAnimation(.easeOut(duration: 0.18)) {
                if openPanel == panel {
                    openPanel = nil
                } else {
                    if panel == .filters {
                        draftFilters = filters
                    }
                    openPanel = panel
                }
            }
        } label: {
            HStack(spacing: 4) {
                Text(title)
                    .lineLimit(1)
                    .minimumScaleFactor(0.72)
                Image(systemName: openPanel == panel ? "chevron.up" : "chevron.down")
                    .font(.system(size: 9, weight: .bold))
            }
            .font(VPFont.body(12, weight: .bold))
            .foregroundStyle(openPanel == panel || isActive(panel) ? VPColor.cinnabar : VPColor.inkMuted)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 9)
        }
        .buttonStyle(.plain)
    }

    private func dropdown(panel: FilterPanel) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            switch panel {
            case .distance:
                singleSelection(ExploreDistance.allCases, selected: distance, disabled: { _ in false }) { value in
                    distance = value
                    openPanel = nil
                    Task { await load(reset: true) }
                }
            case .category:
                singleSelection(category.subcategories, selected: selectedSubcategory, disabled: { _ in false }) { value in
                    selectedSubcategory = value
                    openPanel = nil
                    Task { await load(reset: true) }
                }
            case .sort:
                singleSelection(ExploreSort.allCases, selected: sort, disabled: { value in
                    value == .nearest && locationProvider.coordinate == nil
                }) { value in
                    sort = value
                    openPanel = nil
                    Task { await load(reset: true) }
                }
            case .filters:
                filterPanel
            }
        }
        .background(VPColor.paperSoft)
        .overlay(alignment: .bottom) {
            Rectangle()
                .fill(VPColor.outline.opacity(0.8))
                .frame(height: 1)
        }
    }

    private func singleSelection<Item: Identifiable & Equatable>(
        _ items: [Item],
        selected: Item,
        disabled: @escaping (Item) -> Bool,
        onSelect: @escaping (Item) -> Void
    ) -> some View where Item.ID == String {
        VStack(spacing: 0) {
            ForEach(items) { item in
                Button {
                    if !disabled(item) {
                        onSelect(item)
                    }
                } label: {
                    HStack {
                        Text(title(for: item))
                            .font(VPFont.body(15, weight: .semibold))
                            .foregroundStyle(disabled(item) ? VPColor.inkSoft.opacity(0.45) : VPColor.ink)
                        Spacer()
                        if item == selected {
                            Image(systemName: "checkmark")
                                .foregroundStyle(VPColor.cinnabar)
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 14)
                }
                .buttonStyle(.plain)
            }
        }
    }

    private var filterPanel: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Price per person")
                .font(VPFont.body(14, weight: .bold))
                .foregroundStyle(VPColor.ink)
            Text("Price filters only include merchants with Amap cost data.")
                .font(VPFont.body(12, weight: .semibold))
                .foregroundStyle(VPColor.inkSoft)

            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                ForEach(ExplorePriceFilter.allCases) { price in
                    toggleChip(title: price.title, selected: draftFilters.priceFilters.contains(price)) {
                        if draftFilters.priceFilters.contains(price) {
                            draftFilters.priceFilters.remove(price)
                        } else {
                            draftFilters.priceFilters.insert(price)
                        }
                    }
                }
            }

            Toggle(isOn: $draftFilters.requiresRating) {
                Text("Has rating")
                    .font(VPFont.body(15, weight: .semibold))
                    .foregroundStyle(VPColor.ink)
            }
            .tint(VPColor.cinnabar)

            HStack(spacing: 12) {
                Button {
                    draftFilters = ExploreFilterState()
                } label: {
                    Text("Reset")
                        .font(VPFont.body(15, weight: .bold))
                        .foregroundStyle(VPColor.inkMuted)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 13)
                        .background(VPColor.paperWarm)
                        .clipShape(Capsule())
                }

                Button {
                    filters = draftFilters
                    openPanel = nil
                } label: {
                    Text("Apply")
                        .font(VPFont.body(15, weight: .bold))
                        .foregroundStyle(VPColor.paperSoft)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 13)
                        .background(VPColor.cinnabar)
                        .clipShape(Capsule())
                }
            }
        }
        .padding(20)
    }

    private func toggleChip(title: String, selected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(VPFont.body(13, weight: .bold))
                .foregroundStyle(selected ? VPColor.paperSoft : VPColor.inkMuted)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 11)
                .background(selected ? VPColor.ink : VPColor.paperWarm)
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }

    private func isActive(_ panel: FilterPanel) -> Bool {
        switch panel {
        case .distance:
            distance != .citywide
        case .category:
            selectedSubcategory.key != category.semanticKey
        case .sort:
            sort != .smart
        case .filters:
            filters.activeCount > 0
        }
    }

    private func title<Item>(for item: Item) -> String {
        switch item {
        case let value as ExploreDistance:
            value == .citywide ? value.title : "Nearby \(value.title)"
        case let value as ExploreSubcategory:
            value.title
        case let value as ExploreSort:
            value.title
        default:
            String(describing: item)
        }
    }

    private func loadMoreIfNeeded(current poi: ExploreAmapPoi) {
        guard hasMore, !isLoading, visiblePois.last?.id == poi.id else { return }
        Task { await load(reset: false) }
    }

    private func load(reset: Bool) async {
        if isLoading { return }
        isLoading = true
        if reset {
            page = 1
            hasMore = true
            pois = []
        }

        let requestedPage = page
        let aroundLocation = locationProvider.coordinate
        // Bug fix (architect review of PR #54, 2026-07-05): `useAround` only
        // turned on when the "Nearby" filter itself had a radius, so picking
        // Sort = Nearest while "Nearby" stayed "All city" silently fell back
        // to weight-sort — the sort control still showed Nearest as selected
        // with no indication it was ignored. Also request around mode when
        // the sort alone calls for it. Amap's around-search requires a
        // radius; "All city" has none, so fall back to its max (50km) to
        // keep "all city" scope while still getting server-side distance sort.
        let useAround = (distance.radius != nil || sort == .nearest) && aroundLocation != nil
        let locationString = aroundLocation.map { "\($0.longitude),\($0.latitude)" }
        let effectiveRadius = distance.radius ?? 50_000

        do {
            let response = try await VisePandaAPIClient().fetchExploreAmap(
                cityId: cityId,
                type: selectedSubcategory.key,
                page: requestedPage,
                mode: useAround ? "around" : "city",
                location: useAround ? locationString : nil,
                radius: useAround ? effectiveRadius : nil,
                sort: sort == .nearest && useAround ? "distance" : "weight"
            )
            if reset {
                pois = response.pois
            } else {
                pois.append(contentsOf: response.pois)
            }
            hasMore = response.hasMore ?? false
            page = requestedPage + 1
            notice = useAround ? nil : notice
            openFocusedPoiIfPossible()
        } catch {
            if reset {
                pois = []
            }
            hasMore = false
            notice = "Live Explore is unavailable: \(error.localizedDescription)"
        }

        isLoading = false
    }

    private func openFocusedPoiIfPossible() {
        guard !didOpenFocusRef, let focusRef else { return }
        didOpenFocusRef = true
        selectedPoi = pois.first { $0.id == focusRef.amapPoiId || "amap-\($0.id)" == focusRef.amapPoiId }
        if selectedPoi == nil {
            notice = "That Butler recommendation was not in the current Explore result page."
        }
    }
}

private enum FilterPanel: String, Identifiable {
    case distance
    case category
    case sort
    case filters

    var id: String { rawValue }
}

private struct MerchantCard: View {
    let poi: ExploreAmapPoi
    let category: ExploreCategory
    let userLocation: CLLocationCoordinate2D?

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            AsyncImage(url: poi.photoURL.flatMap(URL.init(string:))) { phase in
                switch phase {
                case let .success(image):
                    image.resizable().scaledToFill()
                default:
                    ZStack {
                        VPColor.paperWarm
                        Image(systemName: category.icon)
                            .font(.system(size: 22, weight: .bold))
                            .foregroundStyle(VPColor.cinnabar)
                    }
                }
            }
            .frame(width: 78, height: 78)
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))

            VStack(alignment: .leading, spacing: 7) {
                HStack(alignment: .firstTextBaseline) {
                    Text(poi.name)
                        .font(VPFont.body(16, weight: .bold))
                        .foregroundStyle(VPColor.ink)
                        .lineLimit(2)
                    Spacer()
                    if let rating = poi.ratingValue {
                        Label(String(format: "%.1f", rating), systemImage: "star.fill")
                            .font(VPFont.body(12, weight: .bold))
                            .foregroundStyle(VPColor.gold)
                    }
                }

                HStack(spacing: 8) {
                    if let cost = poi.costValue {
                        Text("¥\(Int(cost))/person")
                    }
                    Text(poi.type?.split(separator: ";").last.map(String.init) ?? category.title)
                }
                .font(VPFont.body(12, weight: .semibold))
                .foregroundStyle(VPColor.inkSoft)

                HStack(spacing: 8) {
                    if let area = poi.businessArea, !area.isEmpty {
                        Text(area)
                    }
                    if let distance = distanceText {
                        Text(distance)
                    }
                }
                .font(VPFont.body(12, weight: .semibold))
                .foregroundStyle(VPColor.inkMuted)

                if poi.editorial != nil {
                    VPStatusPill(title: "VisePanda recommended", tone: .ready)
                }
            }
        }
        .padding(12)
        .background(VPColor.paperSoft)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay {
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(VPColor.outline.opacity(0.75), lineWidth: 1)
        }
    }

    private var distanceText: String? {
        guard let userLocation, let coordinates = poi.coordinates else { return nil }
        let from = CLLocation(latitude: userLocation.latitude, longitude: userLocation.longitude)
        let to = CLLocation(latitude: coordinates.lat, longitude: coordinates.lng)
        let meters = from.distance(from: to)
        if meters < 1_000 {
            return "\(Int(meters))m"
        }
        return String(format: "%.1fkm", meters / 1_000)
    }
}

private struct PoiDetailSheet: View {
    let poi: ExploreAmapPoi
    let category: ExploreCategory
    let onAddToTrip: () -> Void
    let onAskButler: () -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                MerchantCard(poi: poi, category: category, userLocation: nil)

                if let summary = poi.editorial?.summary {
                    VPCard {
                        VStack(alignment: .leading, spacing: 8) {
                            VPStatusPill(title: poi.editorial?.badge ?? "VisePanda Editorial", tone: .ready)
                            Text(summary)
                                .font(VPFont.body(14))
                                .foregroundStyle(VPColor.inkMuted)
                        }
                    }
                }

                VStack(alignment: .leading, spacing: 10) {
                    optionalLine("Address", poi.address)
                    optionalLine("Phone", poi.tel)
                    optionalLine("Hours", poi.opentimeWeek)
                }

                HStack(spacing: 12) {
                    Button {
                        onAddToTrip()
                        dismiss()
                    } label: {
                        Text("Add to Trip")
                            .font(VPFont.body(15, weight: .bold))
                            .foregroundStyle(VPColor.paperSoft)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 13)
                            .background(VPColor.cinnabar)
                            .clipShape(Capsule())
                    }

                    Button {
                        onAskButler()
                        dismiss()
                    } label: {
                        Text("Ask Butler")
                            .font(VPFont.body(15, weight: .bold))
                            .foregroundStyle(VPColor.ink)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 13)
                            .background(VPColor.paperWarm)
                            .clipShape(Capsule())
                    }
                }
            }
            .padding(20)
        }
        .background(VPColor.paper)
    }

    private func optionalLine(_ label: String, _ value: String?) -> some View {
        Group {
            if let value, !value.isEmpty {
                VStack(alignment: .leading, spacing: 3) {
                    Text(label)
                        .font(VPFont.body(12, weight: .bold))
                        .foregroundStyle(VPColor.inkSoft)
                    Text(value)
                        .font(VPFont.body(14))
                        .foregroundStyle(VPColor.ink)
                }
            }
        }
    }
}

private struct NoticeBanner: View {
    let text: String
    let onDismiss: () -> Void

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: text.hasPrefix("Added") ? "checkmark.circle" : "exclamationmark.triangle")
            Text(text)
                .frame(maxWidth: .infinity, alignment: .leading)
            Button(action: onDismiss) {
                Image(systemName: "xmark")
                    .font(.system(size: 10, weight: .bold))
            }
            .accessibilityLabel("Dismiss Explore notice")
        }
        .font(VPFont.body(12, weight: .semibold))
        .foregroundStyle(text.hasPrefix("Added") ? VPColor.sage : VPColor.cinnabar)
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background((text.hasPrefix("Added") ? VPColor.sage : VPColor.cinnabar).opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
    }
}

private struct EmptyExploreState: View {
    let message: String

    var body: some View {
        VStack(spacing: 10) {
            Image(systemName: "wifi.exclamationmark")
                .font(.system(size: 26, weight: .bold))
                .foregroundStyle(VPColor.inkSoft)
            Text(message)
                .font(VPFont.body(14, weight: .semibold))
                .foregroundStyle(VPColor.inkMuted)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(24)
        .background(VPColor.paperSoft)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay {
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(VPColor.outline.opacity(0.7), lineWidth: 1)
        }
    }
}

private final class ExploreLocationProvider: NSObject, ObservableObject, CLLocationManagerDelegate {
    enum Status: Equatable {
        case idle
        case requesting
        case available
        case denied
        case failed
    }

    @Published var coordinate: CLLocationCoordinate2D?
    @Published var status: Status = .idle

    private let manager = CLLocationManager()

    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
    }

    func requestOnce() {
        guard status == .idle else { return }
        status = .requesting

        switch manager.authorizationStatus {
        case .notDetermined:
            manager.requestWhenInUseAuthorization()
        case .authorizedAlways, .authorizedWhenInUse:
            manager.requestLocation()
        case .denied, .restricted:
            status = .denied
        @unknown default:
            status = .failed
        }
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        switch manager.authorizationStatus {
        case .authorizedAlways, .authorizedWhenInUse:
            manager.requestLocation()
        case .denied, .restricted:
            status = .denied
        case .notDetermined:
            break
        @unknown default:
            status = .failed
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        coordinate = locations.last?.coordinate
        status = coordinate == nil ? .failed : .available
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        status = .failed
    }
}
