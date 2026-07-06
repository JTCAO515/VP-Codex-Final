import AVFoundation
import SwiftUI
import UIKit

struct DayDetailView: View {
    @EnvironmentObject private var store: TripStore
    let day: TripDay
    @State private var editingBlock: TripBlock?
    @State private var editedDescription = ""
    @State private var showToLocalAddress: LocalAddressCard?

    private var currentDay: TripDay {
        store.trip.days.first { $0.day == day.day } ?? day
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("\(currentDay.city) · Day \(currentDay.day)")
                        .font(VPFont.display(28))
                        .foregroundStyle(VPColor.ink)
                    Text(currentDay.note)
                        .font(VPFont.body(14))
                        .foregroundStyle(VPColor.inkSoft)
                }

                VPCard {
                    VStack(alignment: .leading, spacing: 10) {
                        Label(currentDay.transport, systemImage: "tram")
                        Label(currentDay.stay, systemImage: "bed.double")
                        Label(currentDay.food.joined(separator: " · "), systemImage: "fork.knife")
                    }
                    .font(VPFont.body(15, weight: .semibold))
                    .foregroundStyle(VPColor.inkMuted)
                }

                ForEach(Array(currentDay.blocks.enumerated()), id: \.element.id) { index, block in
                    blockCard(block: block, index: index)
                }
            }
            .padding(20)
        }
        .background(VPColor.paper)
        .navigationTitle("Day \(currentDay.day)")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(item: $editingBlock) { block in
            NavigationStack {
                VStack(alignment: .leading, spacing: 14) {
                    Text(block.title)
                        .font(VPFont.display(24))
                    TextEditor(text: $editedDescription)
                        .font(VPFont.body(15))
                        .frame(minHeight: 180)
                        .padding(8)
                        .background(VPColor.paperWarm)
                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                    Spacer()
                }
                .padding(20)
                .background(VPColor.paper)
                .navigationTitle("Edit description")
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") { editingBlock = nil }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Save") {
                            store.updateBlockDescription(dayNumber: currentDay.day, blockId: block.id, description: editedDescription)
                            editingBlock = nil
                        }
                        .disabled(editedDescription.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                    }
                }
            }
        }
        .sheet(item: $showToLocalAddress) { card in
            ShowToLocalSheet(card: card)
                .presentationDetents([.medium, .large])
        }
    }

    private func blockCard(block: TripBlock, index: Int) -> some View {
        VPCard {
            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    Text(block.time == .flexible ? "Needs scheduling" : block.time.rawValue)
                        .font(VPFont.body(12, weight: .bold))
                        .foregroundStyle(VPColor.cinnabar)
                    Spacer()
                    Button {
                        store.moveBlock(dayNumber: currentDay.day, from: index, to: index - 1)
                    } label: {
                        Image(systemName: "arrow.up")
                    }
                    .disabled(index == 0 || block.time == .flexible)
                    Button {
                        store.moveBlock(dayNumber: currentDay.day, from: index, to: index + 1)
                    } label: {
                        Image(systemName: "arrow.down")
                    }
                    .disabled(index == currentDay.blocks.count - 1 || block.time == .flexible)
                    Button {
                        editedDescription = block.description
                        editingBlock = block
                    } label: {
                        Image(systemName: "square.and.pencil")
                    }
                }
                .font(VPFont.body(12, weight: .bold))
                .foregroundStyle(VPColor.cinnabar)

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

                    Button {
                        showToLocalAddress = LocalAddressCard(
                            title: block.title,
                            address: address,
                            detail: block.chineseAddress == nil ? nil : block.address
                        )
                    } label: {
                        Label("Show to Local", systemImage: "text.viewfinder")
                            .font(VPFont.body(13, weight: .bold))
                            .foregroundStyle(VPColor.cinnabar)
                    }
                    .buttonStyle(.plain)
                }

                ForEach(block.highlights ?? [], id: \.self) { highlight in
                    Label(highlight, systemImage: "checkmark.circle")
                        .font(VPFont.body(13))
                        .foregroundStyle(VPColor.inkMuted)
                }

                ForEach(block.bookingCandidates ?? []) { candidate in
                    bookingCandidateRow(candidate)
                }

                if block.time == .flexible {
                    Button {
                        store.prefillChat("Please schedule \(block.title) into Day \(currentDay.day) in \(currentDay.city) and rebalance the day.")
                    } label: {
                        Text("Ask Butler to schedule")
                            .font(VPFont.body(14, weight: .bold))
                            .foregroundStyle(VPColor.paperSoft)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(VPColor.cinnabar)
                            .clipShape(Capsule())
                    }
                }
            }
        }
    }

    private func bookingCandidateRow(_ candidate: BookingCandidate) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(candidate.label)
                .font(VPFont.body(14, weight: .bold))
                .foregroundStyle(VPColor.ink)
            Text(candidate.status == .infoOnly ? "Info only · \(candidate.provider)" : candidate.note)
                .font(VPFont.body(12, weight: .semibold))
                .foregroundStyle(VPColor.gold)
            if let price = candidate.priceHint {
                Text(price)
                    .font(VPFont.body(12, weight: .semibold))
                    .foregroundStyle(VPColor.inkSoft)
            }
        }
        .padding(10)
        .background(VPColor.paperWarm)
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
    }
}

struct LocalAddressCard: Identifiable {
    var id: String { title + address }
    var title: String
    var address: String
    var detail: String?
}

private struct ShowToLocalSheet: View {
    let card: LocalAddressCard
    @Environment(\.dismiss) private var dismiss
    @State private var speaker = AVSpeechSynthesizer()
    @State private var copied = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    VPCard {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Show to Local")
                                .font(VPFont.body(13, weight: .bold))
                                .foregroundStyle(VPColor.cinnabar)
                            Text(card.title)
                                .font(VPFont.display(24))
                                .foregroundStyle(VPColor.ink)
                            Text(card.address)
                                .font(VPFont.display(34, weight: .bold))
                                .foregroundStyle(VPColor.ink)
                                .fixedSize(horizontal: false, vertical: true)
                            if let detail = card.detail, !detail.isEmpty {
                                Text(detail)
                                    .font(VPFont.body(15, weight: .semibold))
                                    .foregroundStyle(VPColor.inkMuted)
                            }
                        }
                    }

                    HStack(spacing: 12) {
                        Button {
                            speak()
                        } label: {
                            Label("Speak", systemImage: "speaker.wave.2.fill")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        .tint(VPColor.cinnabar)

                        Button {
                            UIPasteboard.general.string = card.address
                            copied = true
                        } label: {
                            Label(copied ? "Copied" : "Copy", systemImage: copied ? "checkmark" : "doc.on.doc")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.bordered)
                    }

                    Text("Use this when speaking with taxi drivers, hotel staff, restaurants, or station staff.")
                        .font(VPFont.body(13, weight: .semibold))
                        .foregroundStyle(VPColor.inkSoft)
                        .fixedSize(horizontal: false, vertical: true)
                }
                .padding(20)
            }
            .background(VPColor.paper)
            .navigationTitle("Show to Local")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }

    private func speak() {
        if speaker.isSpeaking {
            speaker.stopSpeaking(at: .immediate)
        }
        let utterance = AVSpeechUtterance(string: card.address)
        utterance.voice = AVSpeechSynthesisVoice(language: "zh-CN")
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate
        speaker.speak(utterance)
    }
}
