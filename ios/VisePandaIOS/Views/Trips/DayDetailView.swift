import AVFoundation
import SwiftUI
import UIKit

struct DayDetailView: View {
    @EnvironmentObject private var store: TripStore
    let day: TripDay
    @State private var editingBlock: TripBlock?
    @State private var editingNote: BlockNoteDraft?
    @State private var editedDescription = ""
    @State private var editedNote = ""
    @State private var localDisplayCard: LocalDisplayCard?
    @AppStorage("localDisplayDietaryRestrictions") private var dietaryRestrictionStorage = ""

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
                    if store.recentlyUpdatedDays.contains(currentDay.day) {
                        Label("Updated by Copilot just now", systemImage: "sparkles")
                            .font(VPFont.body(12, weight: .bold))
                            .foregroundStyle(VPColor.cinnabar)
                            .padding(.top, 4)
                    }
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

                if currentDay.blocks.isEmpty {
                    detailLoadingCard
                } else {
                    ForEach(Array(currentDay.blocks.enumerated()), id: \.element.id) { index, block in
                        blockCard(block: block, index: index)
                    }
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
        .sheet(item: $editingNote) { draft in
            NavigationStack {
                VStack(alignment: .leading, spacing: 14) {
                    Text(draft.title)
                        .font(VPFont.display(24))
                    TextEditor(text: $editedNote)
                        .font(VPFont.body(15))
                        .frame(minHeight: 160)
                        .padding(8)
                        .background(VPColor.paperWarm)
                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                    Text("This note is saved only on this device and is not sent to Copilot.")
                        .font(VPFont.body(12, weight: .semibold))
                        .foregroundStyle(VPColor.inkSoft)
                    Spacer()
                }
                .padding(20)
                .background(VPColor.paper)
                .navigationTitle("Add note")
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") { editingNote = nil }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Save") {
                            store.updateBlockNote(dayNumber: draft.dayNumber, blockId: draft.blockId, note: editedNote)
                            editingNote = nil
                        }
                    }
                }
            }
        }
        .sheet(item: $localDisplayCard) { card in
            ShowToLocalSheet(card: card)
                .presentationDetents([.medium, .large])
        }
    }

    private var detailLoadingCard: some View {
        VPCard {
            VStack(alignment: .leading, spacing: 10) {
                if store.pendingDetailDays.contains(currentDay.day) {
                    Label("Generating details…", systemImage: "sparkles")
                        .font(VPFont.body(15, weight: .bold))
                        .foregroundStyle(VPColor.ink)
                    Text("Copilot has created the trip outline and is filling in the daily schedule.")
                        .font(VPFont.body(13, weight: .semibold))
                        .foregroundStyle(VPColor.inkMuted)
                } else if store.failedDetailDays.contains(currentDay.day) {
                    Text("Details didn't load")
                        .font(VPFont.body(15, weight: .bold))
                        .foregroundStyle(VPColor.ink)
                    Button {
                        store.retrySkeletonDetails(for: currentDay.day)
                    } label: {
                        Label("Tap to retry", systemImage: "arrow.clockwise")
                            .font(VPFont.body(14, weight: .bold))
                            .foregroundStyle(VPColor.paperSoft)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(VPColor.cinnabar)
                            .clipShape(Capsule())
                    }
                    .buttonStyle(.plain)
                } else {
                    Text("This day has no scheduled blocks yet.")
                        .font(VPFont.body(15, weight: .semibold))
                        .foregroundStyle(VPColor.inkMuted)
                }
            }
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

                if let note = store.blockNote(dayNumber: currentDay.day, blockId: block.id), !note.isEmpty {
                    VStack(alignment: .leading, spacing: 4) {
                        Label("Your note", systemImage: "note.text")
                            .font(VPFont.body(12, weight: .bold))
                            .foregroundStyle(VPColor.cinnabar)
                        Text(note)
                            .font(VPFont.body(13, weight: .semibold))
                            .foregroundStyle(VPColor.inkMuted)
                    }
                    .padding(10)
                    .background(VPColor.paperWarm)
                    .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                }

                if let address = block.chineseAddress ?? block.address {
                    Label(address, systemImage: "mappin.and.ellipse")
                        .font(VPFont.body(13, weight: .semibold))
                        .foregroundStyle(VPColor.inkSoft)

                    Button {
                        localDisplayCard = LocalDisplayCard(
                            kind: .address,
                            title: block.title,
                            headline: address,
                            detail: block.chineseAddress == nil ? nil : block.address
                        )
                    } label: {
                        Label("Show to Local", systemImage: "text.viewfinder")
                            .font(VPFont.body(13, weight: .bold))
                            .foregroundStyle(VPColor.cinnabar)
                    }
                    .buttonStyle(.plain)
                }

                if isDiningBlock(block), !selectedDietaryRestrictions.isEmpty {
                    Button {
                        localDisplayCard = allergyCard
                    } label: {
                        Label("Show dietary needs", systemImage: "fork.knife.circle")
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
                        Text("Ask Copilot to schedule")
                            .font(VPFont.body(14, weight: .bold))
                            .foregroundStyle(VPColor.paperSoft)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(VPColor.cinnabar)
                            .clipShape(Capsule())
                        }
                }

                Button {
                    editedNote = store.blockNote(dayNumber: currentDay.day, blockId: block.id) ?? ""
                    editingNote = BlockNoteDraft(dayNumber: currentDay.day, blockId: block.id, title: block.title)
                } label: {
                    Label(store.blockNote(dayNumber: currentDay.day, blockId: block.id) == nil ? "Add note" : "Edit note", systemImage: "note.text")
                        .font(VPFont.body(13, weight: .bold))
                        .foregroundStyle(VPColor.cinnabar)
                }
                .buttonStyle(.plain)
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

    private var selectedDietaryRestrictions: [DietaryRestriction] {
        dietaryRestrictionStorage
            .split(separator: ",")
            .compactMap { DietaryRestriction(rawValue: String($0)) }
    }

    private var allergyCard: LocalDisplayCard {
        LocalDisplayCard(
            kind: .allergy,
            title: "Dietary needs",
            headline: selectedDietaryRestrictions.map(\.chinese).joined(separator: "\n"),
            detail: "Please show this to restaurant staff."
        )
    }

    private func isDiningBlock(_ block: TripBlock) -> Bool {
        let text = ([block.title, block.description] + (block.highlights ?? []))
            .joined(separator: " ")
            .lowercased()
        return ["food", "restaurant", "lunch", "dinner", "breakfast", "cafe", "tea", "hotpot", "noodle", "meal"]
            .contains { text.contains($0) }
    }
}

private struct BlockNoteDraft: Identifiable {
    var dayNumber: Int
    var blockId: String
    var title: String
    var id: String { "\(dayNumber)-\(blockId)" }
}

struct ShowToLocalSheet: View {
    let card: LocalDisplayCard
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
                            Text(card.headline)
                                .font(VPFont.display(34, weight: .bold))
                                .foregroundStyle(VPColor.ink)
                                .fixedSize(horizontal: false, vertical: true)
                            if let detail = card.detail, !detail.isEmpty {
                                Text(detail)
                                    .font(VPFont.body(15, weight: .semibold))
                                    .foregroundStyle(VPColor.inkMuted)
                            }
                            if let disclaimer = card.disclaimer, !disclaimer.isEmpty {
                                Text(disclaimer)
                                    .font(VPFont.body(12, weight: .bold))
                                    .foregroundStyle(VPColor.cinnabar)
                                    .padding(10)
                                    .background(VPColor.cinnabar.opacity(0.08))
                                    .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                            }
                        }
                    }

                    if card.showEmergencyAction {
                        Link(destination: URL(string: "tel:120")!) {
                            Label("Call 120 ambulance", systemImage: "phone.fill")
                                .font(VPFont.body(15, weight: .bold))
                                .foregroundStyle(VPColor.paperSoft)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .background(VPColor.cinnabar)
                                .clipShape(Capsule())
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
                            UIPasteboard.general.string = card.copyText
                            copied = true
                        } label: {
                            Label(copied ? "Copied" : "Copy", systemImage: copied ? "checkmark" : "doc.on.doc")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.bordered)
                    }

                    Text(helpText)
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
        let utterance = AVSpeechUtterance(string: card.headline)
        utterance.voice = AVSpeechSynthesisVoice(language: "zh-CN")
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate
        speaker.speak(utterance)
    }

    private var helpText: String {
        switch card.kind {
        case .address:
            "Use this when speaking with taxi drivers, hotel staff, restaurants, or station staff."
        case .allergy:
            "Use this when speaking with restaurant staff. The Chinese text is fixed and pre-translated."
        case .symptom:
            "Use this with pharmacy staff, doctors, hotel staff, or nearby helpers."
        }
    }
}
