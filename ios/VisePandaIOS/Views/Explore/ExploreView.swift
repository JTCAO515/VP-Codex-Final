import CoreLocation
import SwiftUI

struct ExploreView: View {
    @EnvironmentObject private var store: TripStore
    @AppStorage("space.go2china.visepanda.explore.city") private var selectedCityId = "shanghai"
    @State private var showingCityPicker = false
    @State private var comingSoonPost: ExploreUGCPost?
    @State private var path: [ExploreRoute] = []
    @StateObject private var locationProvider = ExploreLocationProvider()
    @State private var contextualPois: [ExploreAmapPoi] = []
    @State private var contextualLoading = false
    @State private var contextualNotice: String?
    @State private var selectedContextPoi: ExploreAmapPoi?

    var body: some View {
        NavigationStack(path: $path) {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    header
                    contextualDiscovery
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
            .sheet(item: $selectedContextPoi) { poi in
                PoiDetailSheet(
                    poi: poi,
                    category: contextualCategory,
                    onAddToTrip: {
                        store.addPlaceToPlan(poi.name)
                        contextualNotice = "Added \(poi.name) to your trip request."
                    },
                    onAskCopilot: {
                        store.prefillChat("Tell me about \(poi.name) in \(city.name). Is it a good fit for my trip, and where would you place it?")
                    }
                )
                .presentationDetents([.medium, .large])
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
            .task {
                locationProvider.requestOnce()
                await loadContextualPois()
            }
            .onChange(of: locationProvider.status) { _, status in
                if status == .available {
                    Task { await loadContextualPois() }
                } else if status == .denied || status == .failed {
                    contextualNotice = "Location is unavailable, so Explore is showing all-city suggestions."
                }
            }
            .onChange(of: selectedCityId) { _, _ in
                Task { await loadContextualPois() }
            }
        }
    }

    private var city: ExploreCity {
        ExploreCity.city(id: selectedCityId)
    }

    private var contextualCategory: ExploreCategory {
        switch Calendar.current.component(.hour, from: Date()) {
        case 6...13, 18...21:
            .food
        case 14...17:
            .experiences
        default:
            .attractions
        }
    }

    private var contextualSubcategoryKey: String {
        switch Calendar.current.component(.hour, from: Date()) {
        case 6...10:
            "food.cafe"
        case 11...13, 18...21:
            "food"
        case 14...17:
            "experiences.teahouse"
        default:
            contextualCategory.semanticKey
        }
    }

    private var contextualTitle: String {
        switch Calendar.current.component(.hour, from: Date()) {
        case 6...10:
            "Coffee and breakfast near you"
        case 11...13:
            "Lunch options near you"
        case 14...17:
            "Afternoon experiences near you"
        case 18...21:
            "Dinner options near you"
        default:
            "Useful places near you"
        }
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

    private var categoryGrid: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Choose a scene")
                .font(VPFont.body(18, weight: .bold))
                .foregroundStyle(VPColor.ink)
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
    }

    private var contextualDiscovery: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .firstTextBaseline) {
                VStack(alignment: .leading, spacing: 3) {
                    Text(contextualTitle)
                        .font(VPFont.body(18, weight: .bold))
                        .foregroundStyle(VPColor.ink)
                    Text(locationProvider.coordinate == nil ? "Citywide fallback · \(city.name)" : "Walking-distance first · \(city.name)")
                        .font(VPFont.body(12, weight: .semibold))
                        .foregroundStyle(VPColor.inkSoft)
                }
                Spacer()
                NavigationLink(value: ExploreRoute(category: contextualCategory)) {
                    Text("See all")
                        .font(VPFont.body(12, weight: .bold))
                        .foregroundStyle(VPColor.cinnabar)
                }
            }

            if let contextualNotice {
                NoticeBanner(text: contextualNotice) {
                    self.contextualNotice = nil
                }
            }

            if contextualLoading {
                ProgressView()
                    .tint(VPColor.cinnabar)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 24)
            } else if contextualPois.isEmpty {
                EmptyExploreState(message: "Live suggestions are unavailable right now.")
            } else {
                VStack(spacing: 12) {
                    ForEach(contextualPois.prefix(3)) { poi in
                        Button {
                            selectedContextPoi = poi
                        } label: {
                            MerchantCard(poi: poi, category: contextualCategory, userLocation: locationProvider.coordinate)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }

    private func loadContextualPois() async {
        if contextualLoading { return }
        contextualLoading = true
        defer { contextualLoading = false }

        let coordinate = locationProvider.coordinate
        let locationString = coordinate.map { "\($0.longitude),\($0.latitude)" }

        do {
            let response = try await VisePandaAPIClient().fetchExploreAmap(
                cityId: selectedCityId,
                type: contextualSubcategoryKey,
                page: 1,
                mode: coordinate == nil ? "city" : "around",
                location: locationString,
                radius: coordinate == nil ? nil : 3_000,
                sort: coordinate == nil ? "weight" : "distance"
            )
            contextualPois = response.pois
        } catch {
            contextualPois = []
            contextualNotice = "Live Explore is unavailable: \(error.localizedDescription)"
        }
    }

    private var ugcFeed: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Popular in \(city.name)")
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
    @State private var sort: ExploreSort = .smart
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
        switch sort {
        case .smart, .nearest:
            return pois
        case .rating:
            return pois.sorted { ($0.ratingValue ?? -1) > ($1.ratingValue ?? -1) }
        case .priceLow:
            return pois.sorted { ($0.costValue ?? .greatestFiniteMagnitude) < ($1.costValue ?? .greatestFiniteMagnitude) }
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            header

            ScrollView {
                LazyVStack(spacing: 12) {
                    contextPanel

                    if let notice {
                        NoticeBanner(text: notice) {
                            self.notice = nil
                        }
                            .padding(.horizontal, 20)
                    }

                    if visiblePois.isEmpty && !isLoading {
                        EmptyExploreState(message: pois.isEmpty ? "Live Explore data is unavailable or empty right now." : "No places match this scene.")
                            .padding(.horizontal, 20)
                            .padding(.top, 12)
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
        .background(VPColor.paper.ignoresSafeArea())
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
                onAskCopilot: {
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
            if status == .available {
                Task { await load(reset: true) }
            } else if status == .denied || status == .failed {
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

    private var contextPanel: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(locationProvider.coordinate == nil ? "Citywide suggestions for \(city.name)" : "Near you now")
                .font(VPFont.body(18, weight: .bold))
                .foregroundStyle(VPColor.ink)
                .frame(maxWidth: .infinity, alignment: .leading)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(category.subcategories) { subcategory in
                        chip(title: subcategory.title, selected: subcategory == selectedSubcategory) {
                            selectedSubcategory = subcategory
                            Task { await load(reset: true) }
                        }
                    }
                }
                .padding(.horizontal, 20)
            }

            HStack(spacing: 8) {
                chip(title: "Smart", selected: sort == .smart) {
                    sort = .smart
                    Task { await load(reset: true) }
                }
                chip(title: "Nearest", selected: sort == .nearest) {
                    sort = .nearest
                    Task { await load(reset: true) }
                }
                chip(title: "Top rated", selected: sort == .rating) {
                    sort = .rating
                }
                chip(title: "Lower price", selected: sort == .priceLow) {
                    sort = .priceLow
                }
            }
            .padding(.horizontal, 20)

            Text("Explore now uses location, time, and scene chips instead of dropdown filters.")
                .font(VPFont.body(12, weight: .semibold))
                .foregroundStyle(VPColor.inkSoft)
                .padding(.horizontal, 20)
        }
        .padding(.vertical, 12)
        .background(VPColor.paperSoft)
        .overlay(alignment: .bottom) {
            Rectangle()
                .fill(VPColor.outline.opacity(0.8))
                .frame(height: 1)
        }
    }

    private func chip(title: String, selected: Bool, action: @escaping () -> Void) -> some View {
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
        let useAround = aroundLocation != nil
        let locationString = aroundLocation.map { "\($0.longitude),\($0.latitude)" }

        do {
            let response = try await VisePandaAPIClient().fetchExploreAmap(
                cityId: cityId,
                type: selectedSubcategory.key,
                page: requestedPage,
                mode: useAround ? "around" : "city",
                location: useAround ? locationString : nil,
                radius: useAround ? 3_000 : nil,
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
            notice = "That Copilot recommendation was not in the current Explore result page."
        }
    }
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

                if !fitTags.isEmpty {
                    LazyVGrid(columns: [GridItem(.adaptive(minimum: 108), spacing: 6, alignment: .leading)], alignment: .leading, spacing: 6) {
                        ForEach(fitTags, id: \.self) { tag in
                            Text(tag)
                                .font(VPFont.body(11, weight: .bold))
                                .foregroundStyle(VPColor.sage)
                                .fixedSize(horizontal: true, vertical: false)
                                .padding(.horizontal, 9)
                                .padding(.vertical, 5)
                                .background(VPColor.sage.opacity(0.12))
                                .clipShape(Capsule())
                        }
                    }
                }

                if poi.editorial != nil {
                    VPStatusPill(title: "✦ VP Pick", tone: .ready)
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

    private var fitTags: [String] {
        guard let fit = poi.travelerFit(category: category) else { return [] }
        var tags: [String] = []

        if fit.firstTimerFit == true {
            tags.append("Good for first-timers")
        }
        if fit.routeFit?.localizedCaseInsensitiveContains("metro") == true {
            tags.append("Easy by metro")
        }
        if fit.crowdRisk == "High" {
            tags.append("May be crowded")
        }
        if fit.nightFit == false {
            tags.append("Better in daytime")
        }
        if fit.rainyDayFit == true {
            tags.append("Rainy day friendly")
        }
        if fit.luggageFit == true {
            tags.append("Luggage friendly")
        }
        if fit.languageDifficulty == "Lower" {
            tags.append("Easier communication")
        }
        if let payment = fit.paymentFriendliness {
            tags.append(payment)
        }

        return Array(tags.prefix(3))
    }
}

private struct PoiDetailSheet: View {
    let poi: ExploreAmapPoi
    let category: ExploreCategory
    let onAddToTrip: () -> Void
    let onAskCopilot: () -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                MerchantCard(poi: poi, category: category, userLocation: nil)

                if let summary = poi.editorial?.summary {
                    VPCard {
                        VStack(alignment: .leading, spacing: 8) {
                            VPStatusPill(title: "✦ VP Pick", tone: .ready)
                            Text(summary)
                                .font(VPFont.body(14))
                                .foregroundStyle(VPColor.inkMuted)
                        }
                    }
                }

                if !whyThisFitsLines.isEmpty {
                    VPCard {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Why this fits")
                                .font(VPFont.body(13, weight: .bold))
                                .foregroundStyle(VPColor.inkSoft)
                            ForEach(whyThisFitsLines, id: \.self) { line in
                                Text(line)
                                    .font(VPFont.body(14))
                                    .foregroundStyle(VPColor.inkMuted)
                            }
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
                        onAskCopilot()
                        dismiss()
                    } label: {
                        Text("Ask Copilot")
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

    private var whyThisFitsLines: [String] {
        guard let fit = poi.travelerFit(category: category) else { return [] }
        var lines: [String] = []

        if fit.firstTimerFit == true {
            lines.append("A solid pick if this is your first time in China — well-trodden and easy to plan around.")
        } else if fit.firstTimerFit == false {
            lines.append("More of a local spot — worth it if you've already covered the classics.")
        }

        if let payment = fit.paymentFriendliness {
            if payment == "Card accepted" {
                lines.append("Foreign cards are accepted, so you won't need to rely on cash or a local payment app.")
            } else if payment == "Cash only" {
                lines.append("Cash only here — bring RMB since cards and mobile pay may not work.")
            }
        }

        if fit.languageDifficulty == "Lower" {
            lines.append("English menus or service are available, so language shouldn't be a big barrier.")
        } else if fit.languageDifficulty == "Higher" {
            lines.append("Little English on-site — a translation app will help.")
        }

        if let routeFit = fit.routeFit, routeFit.localizedCaseInsensitiveContains("metro") {
            lines.append("Easy to reach by metro, so it slots in well with the rest of a walking or transit-based day.")
        }

        if fit.rainyDayFit == true {
            lines.append("Mostly indoors, so it's a good rainy-day option.")
        } else if fit.rainyDayFit == false {
            lines.append("Best enjoyed outdoors — check the weather before you go.")
        }

        if fit.nightFit == true {
            lines.append("Stays open late, so it also works as an evening plan.")
        } else if fit.nightFit == false {
            lines.append("Better in daytime — it winds down early in the evening.")
        }

        if fit.crowdRisk == "High" {
            lines.append("Popular spot, so expect crowds — arriving early can help.")
        }

        if fit.luggageFit == true {
            lines.append("Fine to visit with luggage in tow, such as on an arrival or departure day.")
        } else if fit.luggageFit == false {
            lines.append("Better without luggage — narrow paths or crowds make it awkward to visit with bags.")
        }

        if let watchOut = fit.watchOut, !lines.contains(where: { $0 == watchOut }) {
            lines.append(watchOut)
        }

        return Array(lines.prefix(4))
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
