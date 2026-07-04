import SwiftUI

struct DayDetailView: View {
    let day: TripDay

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("\(day.city) · Day \(day.day)")
                        .font(VPFont.display(28))
                        .foregroundStyle(VPColor.ink)
                    Text(day.note)
                        .font(VPFont.body(14))
                        .foregroundStyle(VPColor.inkSoft)
                }

                VPCard {
                    VStack(alignment: .leading, spacing: 10) {
                        Label(day.transport, systemImage: "tram")
                        Label(day.stay, systemImage: "bed.double")
                        Label(day.food.joined(separator: " · "), systemImage: "fork.knife")
                    }
                    .font(VPFont.body(15, weight: .semibold))
                    .foregroundStyle(VPColor.inkMuted)
                }

                ForEach(day.blocks) { block in
                    VPCard {
                        VStack(alignment: .leading, spacing: 10) {
                            HStack {
                                Text(block.time.rawValue)
                                    .font(VPFont.body(12, weight: .bold))
                                    .foregroundStyle(VPColor.cinnabar)
                                Spacer()
                                if let source = block.sourceLabel {
                                    Text(source)
                                        .font(VPFont.body(12, weight: .semibold))
                                        .foregroundStyle(VPColor.inkSoft)
                                }
                            }

                            Text(block.title)
                                .font(VPFont.display(22))
                                .foregroundStyle(VPColor.ink)
                            Text(block.description)
                                .font(VPFont.body(15))
                                .foregroundStyle(VPColor.inkMuted)

                            if let address = block.chineseAddress ?? block.address {
                                Label(address, systemImage: "mappin.and.ellipse")
                                    .font(VPFont.body(13, weight: .semibold))
                                    .foregroundStyle(VPColor.inkSoft)
                            }

                            ForEach(block.highlights ?? [], id: \.self) { highlight in
                                Label(highlight, systemImage: "checkmark.circle")
                                    .font(VPFont.body(13))
                                    .foregroundStyle(VPColor.inkMuted)
                            }
                        }
                    }
                }
            }
            .padding(20)
        }
        .background(VPColor.paper)
        .navigationTitle("Day \(day.day)")
        .navigationBarTitleDisplayMode(.inline)
    }
}
