import SwiftUI

struct TripsView: View {
    @EnvironmentObject private var store: TripStore
    @State private var path: [Int] = []

    var body: some View {
        NavigationStack(path: $path) {
            ScrollView {
                VStack(spacing: 18) {
                    header
                    if store.trip.days.isEmpty {
                        emptyTripCard
                    } else {
                        readinessCard
                        timelineSection
                    }

                    ForEach(store.trip.days) { day in
                        NavigationLink(value: day.day) {
                            DayCard(day: day, updatedByButler: store.recentlyUpdatedDays.contains(day.day))
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 14)
                .padding(.bottom, 20)
            }
            .background(VPColor.paper)
            .navigationBarHidden(true)
            .navigationDestination(for: Int.self) { dayNumber in
                if let day = store.trip.days.first(where: { $0.day == dayNumber }) {
                    DayDetailView(day: day)
                        .onAppear { store.setBottomBarHidden(true) }
                        .onDisappear { store.setBottomBarHidden(false) }
                } else {
                    EmptyTripCard(message: "That day is no longer in this trip.")
                }
            }
            .onAppear(perform: consumePendingDay)
            .onChange(of: store.pendingTripDayNumber) { _, _ in
                consumePendingDay()
            }
        }
    }

    private func consumePendingDay() {
        guard let dayNumber = store.consumePendingTripDayNumber() else { return }
        if store.trip.days.contains(where: { $0.day == dayNumber }) {
            path = [dayNumber]
        } else {
            path = []
        }
    }

    private var tripCompleteness: CompletenessResult {
        TripCompleteness.calculateTripCompleteness(store.trip)
    }

    private var header: some View {
        HStack(alignment: .top) {
            VStack(alignment: .leading, spacing: 3) {
                Text(store.trip.summary.title)
                    .font(VPFont.display(24))
                    .foregroundStyle(VPColor.ink)
                Text(store.trip.days.isEmpty ? "No dates set yet" : "Mar 12 - Mar 16 · Day 2")
                    .font(VPFont.body(13, weight: .semibold))
                    .foregroundStyle(VPColor.inkSoft)
            }

            Spacer()

            Menu {
                Button("Reset local draft", action: store.resetLocalDraft)
            } label: {
                HStack(spacing: 5) {
                    Text("Trips")
                    Image(systemName: "chevron.down")
                }
                .font(VPFont.body(13, weight: .bold))
                .foregroundStyle(VPColor.inkMuted)
                .padding(.horizontal, 12)
                .padding(.vertical, 9)
                .background(VPColor.paperWarm)
                .clipShape(Capsule())
            }
        }
    }

    private var emptyTripCard: some View {
        VPCard {
            VStack(alignment: .leading, spacing: 12) {
                Text("TRIP CANVAS")
                    .font(VPFont.body(11, weight: .bold))
                    .foregroundStyle(VPColor.inkSoft)
                Text("No itinerary created yet")
                    .font(VPFont.display(21))
                    .foregroundStyle(VPColor.ink)
                Text("Ask Butler to create a plan. If the network is offline, the app keeps this starter canvas instead of inventing trip days.")
                    .font(VPFont.body(14, weight: .semibold))
                    .foregroundStyle(VPColor.inkMuted)
                    .fixedSize(horizontal: false, vertical: true)

                Button {
                    store.selectedTab = .chat
                } label: {
                    Text("Ask Butler")
                        .font(VPFont.body(15, weight: .bold))
                        .foregroundStyle(VPColor.paperSoft)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 13)
                        .background(VPColor.cinnabar)
                        .clipShape(Capsule())
                }
            }
        }
    }

    private var readinessCard: some View {
        VPCard {
            HStack(alignment: .top, spacing: 16) {
                VStack(alignment: .leading, spacing: 12) {
                    Text("READINESS")
                        .font(VPFont.body(11, weight: .bold))
                        .foregroundStyle(VPColor.inkSoft)
                    Text(readinessTitle(score: tripCompleteness.score))
                        .font(VPFont.display(19))
                        .foregroundStyle(VPColor.ink)

                    HStack(spacing: 8) {
                        ForEach(tripCompleteness.checks) { check in
                            VPStatusPill(title: check.id.rawValue, tone: check.complete ? .ready : .warning)
                        }
                    }
                }

                Spacer()

                ZStack {
                    Circle()
                        .stroke(VPColor.outline.opacity(0.6), lineWidth: 6)
                    Circle()
                        .trim(from: 0, to: CGFloat(tripCompleteness.score) / 100)
                        .stroke(VPColor.cinnabar, style: StrokeStyle(lineWidth: 6, lineCap: .round))
                        .rotationEffect(.degrees(-90))
                    Text("\(tripCompleteness.score)%")
                        .font(VPFont.body(15, weight: .bold))
                        .foregroundStyle(VPColor.cinnabar)
                }
                .frame(width: 56, height: 56)
            }
        }
    }

    private var timelineSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("NOW / NEXT / LATER")
                .font(VPFont.body(11, weight: .bold))
                .foregroundStyle(VPColor.inkSoft)

            ForEach(TripTimeline.buildTimeline(store.trip).prefix(3)) { entry in
                TimelineEntryCard(entry: entry)
            }
        }
    }

    private func readinessTitle(score: Int) -> String {
        if score >= 80 { return "Almost ready to travel" }
        if score >= 50 { return "Trip basics are taking shape" }
        return "Keep building the trip basics"
    }
}

private struct TimelineEntryCard: View {
    let entry: TimelineEntry

    var body: some View {
        VPCard {
            VStack(alignment: .leading, spacing: 8) {
                Text(entry.position.rawValue)
                    .font(VPFont.body(11, weight: .bold))
                    .foregroundStyle(VPColor.cinnabar)
                Text(entry.block.title)
                    .font(VPFont.body(15, weight: .bold))
                    .foregroundStyle(VPColor.ink)
                Text(entry.block.description)
                    .font(VPFont.body(13))
                    .foregroundStyle(VPColor.inkMuted)
                    .lineLimit(2)
            }
        }
    }
}

private struct DayCard: View {
    let day: TripDay
    let updatedByButler: Bool

    var body: some View {
        let completeness = TripCompleteness.calculateDayCompleteness(day)
        VPCard {
            VStack(alignment: .leading, spacing: 14) {
                HStack {
                    Circle()
                        .fill(day.day == 1 ? VPColor.cinnabar : VPColor.outline)
                        .frame(width: 9, height: 9)
                    Text(dayTitle)
                        .font(VPFont.body(15, weight: .bold))
                        .foregroundStyle(VPColor.ink)
                    if day.day == 1 {
                        VPStatusPill(title: "Today", tone: .red)
                    }
                    if updatedByButler {
                        VPStatusPill(title: "Updated", tone: .ready)
                    }
                    VPStatusPill(title: "\(completeness)%", tone: completeness >= 75 ? .ready : .warning)
                    Spacer()
                    Image(systemName: "chevron.right")
                        .foregroundStyle(VPColor.inkSoft)
                }

                VStack(spacing: 8) {
                    ForEach(day.blocks) { block in
                        TripBlockRow(block: block)
                    }
                }
            }
        }
        .overlay {
            if day.day == 1 {
                RoundedRectangle(cornerRadius: VPRadius.md, style: .continuous)
                    .stroke(VPColor.cinnabar.opacity(0.8), lineWidth: 1.3)
            }
        }
    }

    private var dayTitle: String {
        let weekday = day.day == 1 ? "Tue" : "Wed"
        let date = day.day == 1 ? "Mar 12" : "Mar 13"
        return "\(weekday) · \(date)"
    }
}

private struct EmptyTripCard: View {
    let message: String

    var body: some View {
        VPCard {
            Text(message)
                .font(VPFont.body(15, weight: .semibold))
                .foregroundStyle(VPColor.inkMuted)
        }
        .padding(20)
    }
}

private struct TripBlockRow: View {
    let block: TripBlock

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: iconName)
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(VPColor.inkSoft)
                .frame(width: 28, height: 28)

            Text(timeText)
                .font(VPFont.body(13, weight: .semibold))
                .foregroundStyle(VPColor.inkSoft)
                .frame(width: 42, alignment: .leading)

            VStack(alignment: .leading, spacing: 2) {
                Text(block.title)
                    .font(VPFont.body(14, weight: .bold))
                    .foregroundStyle(VPColor.ink)
                if let chineseAddress = block.chineseAddress {
                    Text(chineseAddress)
                        .font(VPFont.body(11))
                        .foregroundStyle(VPColor.inkSoft)
                }
            }

            Spacer()

            VPStatusPill(title: statusText, tone: statusTone)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 10)
        .background(VPColor.paper.opacity(0.75))
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
    }

    private var iconName: String {
        switch block.time {
        case .morning:
            "sunrise"
        case .afternoon:
            "sun.max"
        case .evening:
            "moon"
        case .flexible:
            "clock"
        }
    }

    private var timeText: String {
        switch block.time {
        case .morning:
            "09:00"
        case .afternoon:
            "13:30"
        case .evening:
            "19:00"
        case .flexible:
            "Flex"
        }
    }

    private var statusText: String {
        switch block.time {
        case .morning:
            "READY"
        case .afternoon:
            "INFO"
        case .evening:
            "CONFIRM"
        case .flexible:
            "FLEX"
        }
    }

    private var statusTone: VPStatusPill.Tone {
        switch block.time {
        case .morning:
            .ready
        case .afternoon:
            .neutral
        case .evening:
            .warning
        case .flexible:
            .neutral
        }
    }
}
