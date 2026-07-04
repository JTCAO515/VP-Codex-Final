import SwiftUI

struct MeView: View {
    @EnvironmentObject private var store: TripStore

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                Text("Me")
                    .font(VPFont.display(26))
                    .foregroundStyle(VPColor.ink)
                    .frame(maxWidth: .infinity, alignment: .leading)

                profileCard
                section(title: "PREFERENCES", rows: preferences)
                section(title: "TRIP HISTORY", rows: trips)
                section(title: "DATA & OFFLINE", rows: dataRows)

                Button(role: .destructive) {
                    store.resetLocalDraft()
                } label: {
                    Text("Reset local iOS draft")
                        .font(VPFont.body(15, weight: .bold))
                        .foregroundStyle(VPColor.cinnabar)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 14)
            .padding(.bottom, 20)
        }
        .background(VPColor.paper)
    }

    private var profileCard: some View {
        VPCard {
            HStack(spacing: 14) {
                Text("EM")
                    .font(VPFont.display(16))
                    .foregroundStyle(VPColor.paperSoft)
                    .frame(width: 54, height: 54)
                    .background(VPColor.ink)
                    .clipShape(Circle())

                VStack(alignment: .leading, spacing: 3) {
                    Text("Guest Traveler")
                        .font(VPFont.body(17, weight: .bold))
                        .foregroundStyle(VPColor.ink)
                    Text("Local iOS profile · English")
                        .font(VPFont.body(12, weight: .semibold))
                        .foregroundStyle(VPColor.inkSoft)
                }

                Spacer()

                Button("Edit") {}
                    .font(VPFont.body(13, weight: .bold))
                    .foregroundStyle(VPColor.cinnabar)
            }
        }
    }

    private var preferences: [ProfileRow] {
        [
            ProfileRow(title: "Dietary", value: "Vegetarian"),
            ProfileRow(title: "Pace", value: "Relaxed"),
            ProfileRow(title: "Budget", value: "¥ mid")
        ]
    }

    private var trips: [ProfileRow] {
        [
            ProfileRow(title: store.trip.summary.title, value: "Current"),
            ProfileRow(title: "Saved trip history", value: "Not connected")
        ]
    }

    private var dataRows: [ProfileRow] {
        [
            ProfileRow(title: "Offline storage", value: "Trip + chat cache"),
            ProfileRow(title: "Language", value: "English"),
            ProfileRow(title: "Export my data", value: "Not available")
        ]
    }

    private func section(title: String, rows: [ProfileRow]) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(VPFont.body(12, weight: .bold))
                .foregroundStyle(VPColor.inkSoft)
                .padding(.leading, 4)

            VPCard(padding: 0) {
                VStack(spacing: 0) {
                    ForEach(rows) { row in
                        ProfileRowView(row: row)
                        if row.id != rows.last?.id {
                            Rectangle()
                                .fill(VPColor.outline.opacity(0.72))
                                .frame(height: 1)
                        }
                    }
                }
            }
        }
    }
}

private struct ProfileRow: Identifiable, Equatable {
    var id: String { title + value }
    var title: String
    var value: String
}

private struct ProfileRowView: View {
    let row: ProfileRow

    var body: some View {
        HStack {
            Text(row.title)
                .font(VPFont.body(15, weight: .semibold))
                .foregroundStyle(VPColor.inkMuted)
            Spacer()
            Text(row.value)
                .font(VPFont.body(13, weight: .semibold))
                .foregroundStyle(VPColor.inkSoft)
            Image(systemName: "chevron.right")
                .font(.system(size: 13, weight: .bold))
                .foregroundStyle(VPColor.inkSoft.opacity(0.7))
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
    }
}
