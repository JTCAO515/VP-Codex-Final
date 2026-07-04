import AVFoundation
import SwiftUI

struct TranslateView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var selectedTab = 0
    @State private var input = ""
    @State private var translateToChinese = true
    @State private var translating = false
    @State private var result: TranslateResult?
    @State private var errorMessage: String?
    @State private var selectedPhrase: Phrase?
    @State private var tts = AVSpeechSynthesizer()
    @State private var allowAutoTranslate = true
    @State private var speaking = false

    private let api = VisePandaAPIClient()
    private let phrases = StaticTranslateData.phrases

    init() {}

    #if DEBUG
    init(screenshotScenario: TranslateScreenshotScenario) {
        _allowAutoTranslate = State(initialValue: false)
        switch screenshotScenario {
        case .success, .tts:
            _input = State(initialValue: "Water, please")
            _result = State(initialValue: TranslateResult(translation: "请给我水", pinyin: "Qǐng gěi wǒ shuǐ"))
            _speaking = State(initialValue: screenshotScenario == .tts)
        case .phrasebook:
            _selectedTab = State(initialValue: 1)
        case .offline:
            _input = State(initialValue: "Can you help me?")
            _errorMessage = State(initialValue: "translation_provider_unavailable")
        }
    }
    #endif

    var body: some View {
        VStack(spacing: 0) {
            Picker("Translate mode", selection: $selectedTab) {
                Text("Translator").tag(0)
                Text("Phrasebook").tag(1)
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, 20)
            .padding(.top, 10)

            if selectedTab == 0 {
                translatorTab
            } else {
                phrasebookTab
            }
        }
        .background(VPColor.paper)
        .navigationTitle("Translate")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(item: $selectedPhrase) { phrase in
            PhraseBigCard(phrase: phrase, speak: { speak(phrase.chinese, language: "zh-CN") })
        }
    }

    private var translatorTab: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                VPCard {
                    VStack(alignment: .leading, spacing: 10) {
                        Text(translateToChinese ? "English" : "Chinese")
                            .font(VPFont.body(12, weight: .bold))
                            .foregroundStyle(VPColor.cinnabar)

                        TextField("Type a short travel phrase", text: $input, axis: .vertical)
                            .font(VPFont.body(16))
                            .lineLimit(3...6)
                            .padding(12)
                            .background(VPColor.paper)
                            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                            .accessibilityIdentifier("translateInput")

                        HStack {
                            Button {
                                translateToChinese.toggle()
                                result = nil
                                errorMessage = nil
                            } label: {
                                Image(systemName: "arrow.left.arrow.right")
                            }
                            .buttonStyle(.borderless)
                            .accessibilityLabel("Swap language direction")

                            Text(translateToChinese ? "English to Chinese" : "Chinese to English")
                                .font(VPFont.body(13, weight: .semibold))
                                .foregroundStyle(VPColor.inkSoft)

                            Spacer()

                            if translating {
                                ProgressView()
                            }
                        }
                    }
                }

                if let result {
                    translationCard(result)
                }

                if errorMessage != nil {
                    unavailableCard
                }

                Text("Other Translation Modes")
                    .font(VPFont.body(13, weight: .bold))
                    .foregroundStyle(VPColor.inkSoft)

                disabledMode(title: "Camera translation", icon: "camera")
                disabledMode(title: "Voice translation", icon: "mic")
            }
            .padding(20)
        }
        .task(id: "\(input)|\(translateToChinese)") {
            await debounceTranslate()
        }
    }

    private var phrasebookTab: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 14, pinnedViews: [.sectionHeaders]) {
                ForEach(groupedPhrases, id: \.0) { category, categoryPhrases in
                    Section {
                        ForEach(categoryPhrases) { phrase in
                            PhraseRow(phrase: phrase, speak: { speak(phrase.chinese, language: "zh-CN") })
                                .onTapGesture { selectedPhrase = phrase }
                        }
                    } header: {
                        Text(category)
                            .font(VPFont.body(15, weight: .bold))
                            .foregroundStyle(VPColor.cinnabar)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.vertical, 8)
                            .background(VPColor.paper)
                    }
                }
            }
            .padding(20)
        }
    }

    private var groupedPhrases: [(String, [Phrase])] {
        Dictionary(grouping: phrases, by: \.category)
            .sorted { $0.key < $1.key }
            .map { ($0.key, $0.value) }
    }

    private func translationCard(_ result: TranslateResult) -> some View {
        VPCard {
            VStack(alignment: .leading, spacing: 10) {
                Text(translateToChinese ? "Chinese" : "English")
                    .font(VPFont.body(12, weight: .bold))
                    .foregroundStyle(VPColor.cinnabar)

                Text(result.translation)
                    .font(VPFont.display(25, weight: .bold))
                    .foregroundStyle(VPColor.ink)
                    .accessibilityIdentifier("translationResult")

                if translateToChinese, !result.pinyin.isEmpty {
                    Text("Pinyin: \(result.pinyin)")
                        .font(VPFont.body(14, weight: .semibold))
                        .foregroundStyle(VPColor.inkSoft)
                }

                HStack {
                    Button {
                        UIPasteboard.general.string = result.translation
                    } label: {
                        Label("Copy", systemImage: "doc.on.doc")
                    }

                    Spacer()

                    Button {
                        speak(result.translation, language: translateToChinese ? "zh-CN" : "en-US")
                    } label: {
                        Label(speaking ? "Speaking" : "Speak", systemImage: "speaker.wave.2.fill")
                    }
                    .accessibilityIdentifier("ttsButton")
                }
                .font(VPFont.body(14, weight: .bold))
                .foregroundStyle(VPColor.cinnabar)
            }
        }
        .onTapGesture {
            selectedPhrase = Phrase(category: "Translation", english: input, chinese: result.translation, pinyin: result.pinyin)
        }
    }

    private var unavailableCard: some View {
        VPCard {
            Label("Translation unavailable. Try again when the network is available.", systemImage: "exclamationmark.triangle")
                .font(VPFont.body(14, weight: .semibold))
                .foregroundStyle(VPColor.cinnabar)
                .fixedSize(horizontal: false, vertical: true)
                .accessibilityIdentifier("translationUnavailable")
        }
    }

    private func disabledMode(title: String, icon: String) -> some View {
        VPCard {
            HStack {
                Label(title, systemImage: icon)
                    .font(VPFont.body(15, weight: .bold))
                    .foregroundStyle(VPColor.inkSoft.opacity(0.65))
                Spacer()
                VPStatusPill(title: "Coming soon")
            }
        }
    }

    private func debounceTranslate() async {
        guard allowAutoTranslate else { return }
        let text = input.trimmingCharacters(in: .whitespacesAndNewlines)
        guard text.count >= 2 else {
            result = nil
            errorMessage = nil
            translating = false
            return
        }
        do {
            try await Task.sleep(nanoseconds: 650_000_000)
            guard !Task.isCancelled else { return }
            await translate(text)
        } catch {
            return
        }
    }

    @MainActor
    private func translate(_ text: String) async {
        translating = true
        errorMessage = nil
        result = nil

        do {
            let response = try await api.translateText(text, from: translateToChinese ? "en" : "zh", to: translateToChinese ? "zh" : "en")
            if response.ok, let translation = response.translation, !translation.isEmpty {
                result = TranslateResult(translation: translation, pinyin: response.pinyin ?? "")
            } else {
                errorMessage = response.error ?? "translation_provider_unavailable"
            }
        } catch {
            errorMessage = error.localizedDescription
        }
        translating = false
    }

    private func speak(_ text: String, language: String) {
        if tts.isSpeaking { tts.stopSpeaking(at: .immediate) }
        speaking = true
        let utterance = AVSpeechUtterance(string: text)
        utterance.voice = AVSpeechSynthesisVoice(language: language)
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate
        tts.speak(utterance)
        Task {
            try? await Task.sleep(nanoseconds: 1_600_000_000)
            await MainActor.run { speaking = false }
        }
    }
}

#if DEBUG
enum TranslateScreenshotScenario: String {
    case success = "translate-success"
    case tts = "translate-tts"
    case phrasebook = "translate-phrasebook"
    case offline = "translate-offline"

    static var launchArgument: TranslateScreenshotScenario? {
        ProcessInfo.processInfo.arguments
            .compactMap(TranslateScreenshotScenario.init(rawValue:))
            .first
    }
}
#endif

private struct PhraseRow: View {
    let phrase: Phrase
    let speak: () -> Void

    var body: some View {
        VPCard(padding: 14) {
            HStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(phrase.english)
                        .font(VPFont.body(15, weight: .semibold))
                        .foregroundStyle(VPColor.ink)
                    Text(phrase.chinese)
                        .font(VPFont.body(19, weight: .bold))
                        .foregroundStyle(VPColor.cinnabar)
                    Text(phrase.pinyin)
                        .font(VPFont.body(12, weight: .semibold))
                        .foregroundStyle(VPColor.inkSoft)
                }
                Spacer()
                Button(action: speak) {
                    Image(systemName: "speaker.wave.2.fill")
                        .font(.system(size: 18, weight: .bold))
                }
                .tint(VPColor.cinnabar)
                .accessibilityLabel("Play pronunciation")
            }
        }
    }
}

private struct PhraseBigCard: View {
    let phrase: Phrase
    let speak: () -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(spacing: 22) {
            Text("Show to local people")
                .font(VPFont.body(12, weight: .bold))
                .foregroundStyle(VPColor.inkSoft)

            Text(phrase.chinese)
                .font(VPFont.display(34, weight: .bold))
                .foregroundStyle(VPColor.ink)
                .multilineTextAlignment(.center)

            Text(phrase.pinyin)
                .font(VPFont.body(18, weight: .semibold))
                .foregroundStyle(VPColor.inkSoft)
                .multilineTextAlignment(.center)

            Text(phrase.english)
                .font(VPFont.body(16, weight: .semibold))
                .foregroundStyle(VPColor.inkMuted)
                .multilineTextAlignment(.center)

            HStack(spacing: 20) {
                Button(action: speak) {
                    Image(systemName: "speaker.wave.2.fill")
                        .font(.system(size: 24, weight: .bold))
                        .foregroundStyle(VPColor.paperSoft)
                        .frame(width: 56, height: 56)
                        .background(VPColor.ink)
                        .clipShape(Circle())
                }

                Button {
                    dismiss()
                } label: {
                    Image(systemName: "xmark")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundStyle(VPColor.ink)
                        .frame(width: 56, height: 56)
                        .overlay(Circle().stroke(VPColor.ink, lineWidth: 1))
                }
            }
        }
        .padding(26)
        .background(VPColor.paper)
        .presentationDetents([.medium])
    }
}
