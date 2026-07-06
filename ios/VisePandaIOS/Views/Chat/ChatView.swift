import AVFoundation
import SwiftUI

struct ChatView: View {
    @EnvironmentObject private var store: TripStore
    @State private var draft = ""
    @State private var recorder: AVAudioRecorder?
    @State private var recordingTask: Task<Void, Never>?
    @State private var transcribing = false
    @State private var voiceError: String?
    private let api = VisePandaAPIClient()

    var body: some View {
        VStack(spacing: 0) {
            chatHeader

            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 16) {
                        preferenceChips

                        ForEach(store.messages) { message in
                            MessageBubble(message: message)
                                .id(message.id)
                        }

                        if store.isSending {
                            ThinkingBubble()
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 12)
                    .padding(.bottom, 20)
                }
                .scrollDismissesKeyboard(.interactively)
                .onChange(of: store.messages.count) { _, _ in
                    scrollToLatest(proxy)
                }
            }

            suggestionsRow
            if let voiceError {
                Text(voiceError)
                    .font(VPFont.body(12, weight: .semibold))
                    .foregroundStyle(VPColor.cinnabar)
                    .padding(.horizontal, 20)
            }

            ChatComposer(
                draft: $draft,
                isSending: store.isSending,
                isRecording: recorder != nil,
                isTranscribing: transcribing,
                onMic: toggleVoiceRecording
            ) {
                sendDraft()
            }
        }
        .background(VPColor.paper)
        .onAppear(perform: consumePendingDraft)
        .onChange(of: store.pendingChatDraft) { _, _ in
            consumePendingDraft()
        }
    }

    private var chatHeader: some View {
        HStack(spacing: 12) {
            Image("VisePandaAvatar")
                .resizable()
                .scaledToFill()
                .frame(width: 42, height: 42)
                .clipShape(Circle())

            VStack(alignment: .leading, spacing: 2) {
                Text("VisePanda")
                    .font(VPFont.display(22))
                    .foregroundStyle(VPColor.ink)
                Text("China Travel AI Copilot")
                    .font(VPFont.body(12))
                    .foregroundStyle(VPColor.inkSoft)
            }

            Spacer()

            Button {
                store.resetLocalDraft()
            } label: {
                Text("Reset")
                    .font(VPFont.body(13, weight: .semibold))
                    .foregroundStyle(VPColor.inkSoft)
            }
        }
        .padding(.horizontal, 20)
        .padding(.top, 14)
        .padding(.bottom, 6)
    }

    private var preferenceChips: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                VPChip(title: "Vegetarian")
                VPChip(title: "Walks a lot")
                VPChip(title: "Loves tea houses")
                VPChip(title: "Budget ¥ mid")
            }
            .padding(.vertical, 4)
        }
    }

    private var suggestionsRow: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(store.suggestions, id: \.self) { suggestion in
                    Button {
                        draft = suggestion
                        sendDraft()
                    } label: {
                        VPChip(title: suggestion)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 10)
        }
        .background(.ultraThinMaterial)
    }

    private func sendDraft() {
        let text = draft
        draft = ""
        store.send(text)
    }

    private func consumePendingDraft() {
        guard let pending = store.consumePendingChatDraft() else { return }
        draft = pending
    }

    private func scrollToLatest(_ proxy: ScrollViewProxy) {
        guard let last = store.messages.last else { return }
        DispatchQueue.main.async {
            withAnimation(.easeOut(duration: 0.25)) {
                proxy.scrollTo(last.id, anchor: .bottom)
            }
        }
    }

    private func toggleVoiceRecording() {
        if recorder == nil {
            startVoiceRecording()
        } else {
            stopRecording(send: true)
        }
    }

    private func startVoiceRecording() {
        voiceError = nil
        switch AVAudioSession.sharedInstance().recordPermission {
        case .granted:
            beginRecording()
        case .undetermined:
            AVAudioSession.sharedInstance().requestRecordPermission { granted in
                Task { @MainActor in
                    if granted {
                        beginRecording()
                    } else {
                        voiceError = "Microphone permission is required for Copilot voice input."
                    }
                }
            }
        default:
            voiceError = "Microphone permission is required for Copilot voice input."
        }
    }

    private func beginRecording() {
        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.playAndRecord, mode: .spokenAudio, options: [.defaultToSpeaker])
            try session.setActive(true)
            let url = FileManager.default.temporaryDirectory.appendingPathComponent("visepanda-copilot-voice.m4a")
            try? FileManager.default.removeItem(at: url)
            let nextRecorder = try AVAudioRecorder(url: url, settings: [
                AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
                AVSampleRateKey: 16_000,
                AVNumberOfChannelsKey: 1,
                AVEncoderAudioQualityKey: AVAudioQuality.medium.rawValue
            ])
            recorder = nextRecorder
            nextRecorder.record(forDuration: 30)
            recordingTask?.cancel()
            recordingTask = Task {
                try? await Task.sleep(nanoseconds: 30_000_000_000)
                await MainActor.run { stopRecording(send: true) }
            }
        } catch {
            voiceError = "Recording failed to start. Try again."
            recorder = nil
        }
    }

    private func stopRecording(send: Bool) {
        let url = recorder?.url
        recorder?.stop()
        recorder = nil
        recordingTask?.cancel()
        recordingTask = nil
        try? AVAudioSession.sharedInstance().setActive(false)
        if send, let url {
            Task { await transcribe(url) }
        }
    }

    @MainActor
    private func transcribe(_ url: URL) async {
        transcribing = true
        defer { transcribing = false }
        do {
            let audio = try Data(contentsOf: url)
            guard !audio.isEmpty else {
                voiceError = "Recording was too short. Try again."
                return
            }
            let response = try await api.translateStt(audioBase64: audio.base64EncodedString())
            if response.ok, let text = response.text?.trimmingCharacters(in: .whitespacesAndNewlines), !text.isEmpty {
                draft = text
                voiceError = nil
            } else {
                voiceError = "Voice transcription unavailable. Try again online."
            }
        } catch {
            voiceError = "Voice transcription unavailable. Try again online."
        }
    }
}

private struct MessageBubble: View {
    @EnvironmentObject private var store: TripStore
    let message: ChatMessage

    var body: some View {
        if message.role == .user {
            HStack {
                Spacer(minLength: 48)
                Text(message.content)
                    .font(VPFont.body(16, weight: .semibold))
                    .foregroundStyle(VPColor.paperSoft)
                    .padding(.horizontal, 18)
                    .padding(.vertical, 14)
                    .background(VPColor.ink)
                    .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            }
        } else {
            HStack(alignment: .top, spacing: 10) {
                Image("VisePandaAvatar")
                    .resizable()
                    .scaledToFill()
                    .frame(width: 32, height: 32)
                    .clipShape(Circle())

                VPCard(padding: 16) {
                    VStack(alignment: .leading, spacing: 12) {
                        if let response = message.response {
                            Text(response.headline)
                                .font(VPFont.display(20))
                                .foregroundStyle(VPColor.ink)
                            Text(response.body)
                                .font(VPFont.body(15))
                                .foregroundStyle(VPColor.inkMuted)
                                .fixedSize(horizontal: false, vertical: true)

                            VStack(alignment: .leading, spacing: 8) {
                                ForEach(response.highlights, id: \.self) { highlight in
                                    Label(highlight, systemImage: "sparkle")
                                        .font(VPFont.body(13, weight: .semibold))
                                        .foregroundStyle(VPColor.inkMuted)
                                }
                            }
                            .padding(12)
                            .background(VPColor.paperWarm)
                            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))

                            if let watchOut = response.watchOut {
                                Label(watchOut, systemImage: "exclamationmark.triangle")
                                    .font(VPFont.body(13, weight: .semibold))
                                    .foregroundStyle(VPColor.cinnabar)
                                    .padding(12)
                                    .background(VPColor.cinnabar.opacity(0.08))
                                    .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                            }

                            if let toolCards = response.toolCards {
                                ForEach(toolCards) { card in
                                    InlineToolCardView(card: card)
                                }
                            }

                            if let refs = response.exploreRefs, !refs.isEmpty {
                                ExploreRefsRow(refs: refs) { ref in
                                    store.openExplore(ref: ref)
                                }
                            }

                            if let affectedDays = message.affectedDays, !affectedDays.isEmpty {
                                UpdatedDaysRow(days: affectedDays) { day in
                                    store.openTripDay(day)
                                }
                            }

                            Text(response.nextStep)
                                .font(VPFont.body(14, weight: .bold))
                                .foregroundStyle(VPColor.ink)
                        } else {
                            Text(message.content)
                                .font(VPFont.body(15))
                                .foregroundStyle(VPColor.inkMuted)
                        }
                    }
                }
            }
        }
    }
}

private struct UpdatedDaysRow: View {
    let days: [Int]
    let onTap: (Int) -> Void

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 10) {
                ForEach(days, id: \.self) { day in
                    Button {
                        onTap(day)
                    } label: {
                        Label("View Day \(day)", systemImage: "calendar.badge.clock")
                            .font(VPFont.body(13, weight: .bold))
                            .foregroundStyle(VPColor.cinnabar)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 10)
                            .background(VPColor.paperWarm)
                            .clipShape(Capsule())
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }
}

private struct ExploreRefsRow: View {
    let refs: [ButlerExploreRef]
    let onTap: (ButlerExploreRef) -> Void

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 10) {
                ForEach(refs) { ref in
                    Button {
                        onTap(ref)
                    } label: {
                        VStack(alignment: .leading, spacing: 7) {
                            HStack(spacing: 6) {
                                Text(ref.name)
                                    .font(VPFont.body(13, weight: .bold))
                                    .foregroundStyle(VPColor.ink)
                                    .lineLimit(2)
                                if ref.editorial == true {
                                    Image(systemName: "checkmark.seal.fill")
                                        .foregroundStyle(VPColor.sage)
                                }
                            }

                            HStack(spacing: 8) {
                                if let rating = ref.rating {
                                    Label(String(format: "%.1f", rating), systemImage: "star.fill")
                                }
                                if let price = ref.pricePerPerson, !price.isEmpty {
                                    Text("¥\(price)/person")
                                }
                            }
                            .font(VPFont.body(11, weight: .semibold))
                            .foregroundStyle(VPColor.inkSoft)
                        }
                        .frame(width: 150, alignment: .leading)
                        .padding(12)
                        .background(VPColor.paperWarm)
                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }
}

private struct InlineToolCardView: View {
    @EnvironmentObject private var store: TripStore
    let card: InlineToolCard

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(card.title)
                .font(VPFont.body(14, weight: .bold))
                .foregroundStyle(VPColor.ink)
            Text(card.summary)
                .font(VPFont.body(14))
                .foregroundStyle(VPColor.inkMuted)
            ForEach(card.items, id: \.self) { item in
                Text(item)
                    .font(VPFont.body(13))
                    .foregroundStyle(VPColor.inkSoft)
            }
            Button {
                store.openTool(card.categoryId)
            } label: {
                Text(card.nextAction)
                    .font(VPFont.body(13, weight: .bold))
                    .foregroundStyle(card.tone == .warning ? VPColor.cinnabar : VPColor.sage)
            }
            .buttonStyle(.plain)
        }
        .padding(12)
        .background(VPColor.paperWarm)
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
    }
}

private struct ThinkingBubble: View {
    var body: some View {
        HStack {
            ProgressView()
                .tint(VPColor.cinnabar)
            Text("VisePanda is making plan for you")
                .font(VPFont.body(14, weight: .semibold))
                .foregroundStyle(VPColor.inkSoft)
            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 14)
    }
}

private struct ChatComposer: View {
    @Binding var draft: String
    let isSending: Bool
    let isRecording: Bool
    let isTranscribing: Bool
    let onMic: () -> Void
    let onSend: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            VStack(spacing: 2) {
                Image(systemName: "camera.fill")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundStyle(VPColor.inkSoft.opacity(0.45))
                    .frame(width: 30, height: 30)
                    .background(VPColor.paperWarm)
                    .clipShape(Circle())
                    .accessibilityLabel("Camera input unavailable")
                Text("Backend needed")
                    .font(VPFont.body(8, weight: .bold))
                    .foregroundStyle(VPColor.inkSoft.opacity(0.7))
                    .lineLimit(1)
                    .minimumScaleFactor(0.7)
            }

            Button(action: onMic) {
                Image(systemName: isRecording ? "stop.circle.fill" : "mic.fill")
                    .foregroundStyle(isRecording ? VPColor.cinnabar : VPColor.inkSoft)
            }
            .accessibilityLabel(isRecording ? "Stop recording" : "Record voice input")
            .disabled(isSending || isTranscribing)

            TextField(isRecording ? "Recording voice..." : isTranscribing ? "Transcribing..." : "Ask VisePanda...", text: $draft, axis: .vertical)
                .font(VPFont.body(15))
                .lineLimit(1...4)
                .submitLabel(.send)
                .onSubmit(onSend)
                .disabled(isSending || isRecording || isTranscribing)

            Button(action: onSend) {
                Image(systemName: "paperplane.fill")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundStyle(VPColor.paperSoft)
                    .frame(width: 42, height: 42)
                    .background(isSending ? VPColor.inkSoft : VPColor.cinnabar)
                    .clipShape(Circle())
            }
            .disabled(isSending || draft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
        }
        .padding(.leading, 16)
        .padding(.trailing, 8)
        .padding(.vertical, 8)
        .background(VPColor.paperSoft)
        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
        .overlay {
            RoundedRectangle(cornerRadius: 24, style: .continuous)
                .stroke(VPColor.outline, lineWidth: 1)
        }
        .padding(.horizontal, 20)
        .padding(.top, 8)
        .padding(.bottom, 12)
        .background(VPColor.paper)
    }
}
