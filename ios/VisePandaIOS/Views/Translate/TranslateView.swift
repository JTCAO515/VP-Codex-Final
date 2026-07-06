import AVFoundation
import SwiftUI
import UIKit

struct TranslateView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var selectedTab = 0
    @State private var input = ""
    @State private var fromLanguageCode = "en"
    @State private var toLanguageCode = "zh"
    @State private var translating = false
    @State private var result: TranslateResult?
    @State private var errorMessage: String?
    @State private var selectedPhrase: Phrase?
    @State private var tts = AVSpeechSynthesizer()
    @State private var audioPlayer: AVPlayer?
    @State private var allowAutoTranslate = true
    @State private var speaking = false
    @State private var processingMode: TranslateProcessingMode?
    @State private var permissionMessage: String?
    @State private var imagePicker: TranslateImagePickerSource?
    @State private var recorder: AVAudioRecorder?
    @State private var recordingTask: Task<Void, Never>?

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
        case .ocr:
            _input = State(initialValue: "地铁站在哪里？")
            _fromLanguageCode = State(initialValue: "zh")
            _toLanguageCode = State(initialValue: "en")
            _result = State(initialValue: TranslateResult(translation: "Where is the subway station?", pinyin: ""))
        case .stt:
            _input = State(initialValue: "我要去外滩")
            _fromLanguageCode = State(initialValue: "zh")
            _toLanguageCode = State(initialValue: "en")
            _result = State(initialValue: TranslateResult(translation: "I want to go to the Bund.", pinyin: ""))
        case .permission:
            _permissionMessage = State(initialValue: "Camera or microphone permission is required for OCR and voice translation. Enable it in Settings.")
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
            PhraseBigCard(phrase: phrase, speak: { speak(phrase.chinese, languageCode: "zh") })
        }
        .sheet(item: $imagePicker) { picker in
            ImagePicker(sourceType: picker.sourceType) { image in
                Task { await handlePickedImage(image) }
            }
        }
        .onDisappear {
            stopRecording(send: false)
            audioPlayer?.pause()
        }
    }

    private var translatorTab: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                VPCard {
                    VStack(alignment: .leading, spacing: 10) {
                        Text(fromLanguage.displayName)
                            .font(VPFont.body(12, weight: .bold))
                            .foregroundStyle(VPColor.cinnabar)

                        TextField(
                            recorder != nil ? "Recording... tap mic to stop" : (processingMode != nil ? "Transcribing..." : "Type a short travel phrase"),
                            text: $input,
                            axis: .vertical
                        )
                            .font(VPFont.body(16))
                            .lineLimit(3...6)
                            .disabled(recorder != nil || processingMode != nil)
                            .padding(12)
                            .background(VPColor.paper)
                            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                            .accessibilityIdentifier("translateInput")

                        HStack(spacing: 16) {
                            Button(action: startCameraTranslation) {
                                Image(systemName: "camera")
                            }
                            .disabled(processingMode == .ocr || recorder != nil)
                            .accessibilityLabel("Camera translation")
                            .accessibilityIdentifier("cameraTranslationButton")

                            Button(action: toggleVoiceRecording) {
                                Image(systemName: recorder == nil ? "mic" : "stop.circle.fill")
                                    .foregroundStyle(recorder != nil ? VPColor.cinnabar : VPColor.ink)
                            }
                            .disabled(processingMode == .ocr)
                            .accessibilityLabel(recorder == nil ? "Voice translation" : "Stop recording")
                            .accessibilityIdentifier("voiceTranslationButton")

                            if processingMode != nil {
                                ProgressView()
                            }

                            Spacer()
                        }
                        .font(.system(size: 18, weight: .semibold))
                        .tint(VPColor.ink)

                        HStack(spacing: 8) {
                            Button {
                                swapLanguages()
                            } label: {
                                Image(systemName: "arrow.left.arrow.right")
                            }
                            .buttonStyle(.borderless)
                            .accessibilityLabel("Swap language direction")

                            languagePicker("From", selection: $fromLanguageCode)
                            Text("to")
                                .font(VPFont.body(12, weight: .semibold))
                                .foregroundStyle(VPColor.inkSoft)
                            languagePicker("To", selection: $toLanguageCode)

                            Spacer()

                            if translating {
                                ProgressView()
                            }
                        }
                        .font(VPFont.body(14, weight: .semibold))
                        .tint(VPColor.ink)
                    }
                }

                if let result {
                    translationCard(result)
                }

                if errorMessage != nil {
                    unavailableCard
                }

                if let permissionMessage {
                    permissionCard(permissionMessage)
                }

                NavigationLink {
                    CommunicationCardDetail()
                } label: {
                    VPCard {
                        HStack {
                            Label {
                                VStack(alignment: .leading, spacing: 3) {
                                    Text("Quick Phrases")
                                    Text("Show Chinese phrases for directions, food, pay, and help")
                                        .font(VPFont.body(12, weight: .semibold))
                                        .foregroundStyle(VPColor.inkSoft)
                                }
                            } icon: {
                                Image(systemName: "text.bubble.fill")
                            }
                            .font(VPFont.body(15, weight: .bold))
                            .foregroundStyle(VPColor.ink)
                            Spacer()
                            Image(systemName: "chevron.right")
                                .foregroundStyle(VPColor.inkSoft)
                        }
                    }
                }
                .buttonStyle(.plain)
            }
            .padding(20)
        }
        .task(id: "\(input)|\(fromLanguageCode)|\(toLanguageCode)") {
            await debounceTranslate()
        }
    }

    private var phrasebookTab: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 14, pinnedViews: [.sectionHeaders]) {
                ForEach(groupedPhrases, id: \.0) { category, categoryPhrases in
                    Section {
                        ForEach(categoryPhrases) { phrase in
                            PhraseRow(phrase: phrase, speak: { speak(phrase.chinese, languageCode: "zh") })
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
                Text(toLanguage.displayName)
                    .font(VPFont.body(12, weight: .bold))
                    .foregroundStyle(VPColor.cinnabar)

                Text(result.translation)
                    .font(VPFont.display(25, weight: .bold))
                    .foregroundStyle(VPColor.ink)
                    .accessibilityIdentifier("translationResult")

                if toLanguageCode == "zh", !result.pinyin.isEmpty {
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
                        speak(result.translation, languageCode: toLanguageCode)
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

    private var fromLanguage: SupportedLanguage {
        SupportedLanguages.byCode(fromLanguageCode)
    }

    private var toLanguage: SupportedLanguage {
        SupportedLanguages.byCode(toLanguageCode)
    }

    private func languagePicker(_ title: String, selection: Binding<String>) -> some View {
        Picker(title, selection: selection) {
            ForEach(SupportedLanguages.all) { language in
                Text(language.displayName).tag(language.code)
            }
        }
        .pickerStyle(.menu)
        .tint(VPColor.cinnabar)
        .onChange(of: selection.wrappedValue) { _, _ in
            result = nil
            errorMessage = nil
        }
    }

    private func swapLanguages() {
        let previousFrom = fromLanguageCode
        fromLanguageCode = toLanguageCode
        toLanguageCode = previousFrom
        result = nil
        errorMessage = nil
    }

    private func permissionCard(_ message: String) -> some View {
        VPCard {
            Label(message, systemImage: "lock.fill")
                .font(VPFont.body(14, weight: .semibold))
                .foregroundStyle(VPColor.cinnabar)
                .fixedSize(horizontal: false, vertical: true)
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
            let response = try await api.translateText(text, from: fromLanguageCode, to: toLanguageCode)
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

    private func startCameraTranslation() {
        permissionMessage = nil
        errorMessage = nil

        guard UIImagePickerController.isSourceTypeAvailable(.camera) else {
            imagePicker = TranslateImagePickerSource(sourceType: .photoLibrary)
            return
        }

        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            imagePicker = TranslateImagePickerSource(sourceType: .camera)
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { granted in
                Task { @MainActor in
                    if granted {
                        imagePicker = TranslateImagePickerSource(sourceType: .camera)
                    } else {
                        permissionMessage = "Camera permission is required for photo translation. Enable it in Settings."
                    }
                }
            }
        default:
            permissionMessage = "Camera permission is required for photo translation. Enable it in Settings."
        }
    }

    @MainActor
    private func handlePickedImage(_ image: UIImage) async {
        processingMode = .ocr
        permissionMessage = nil
        errorMessage = nil
        result = nil

        guard let jpeg = image.compressedForTranslation() else {
            processingMode = nil
            errorMessage = "image_processing_failed"
            return
        }

        do {
            let response = try await api.translateOcr(imageBase64: jpeg.base64EncodedString())
            if response.ok, let text = response.text?.trimmingCharacters(in: .whitespacesAndNewlines), !text.isEmpty {
                input = text
                await translate(text)
            } else {
                errorMessage = response.error ?? "ocr_failed"
            }
        } catch {
            errorMessage = error.localizedDescription
        }
        processingMode = nil
    }

    private func toggleVoiceRecording() {
        if recorder == nil {
            startVoiceRecording()
        } else {
            stopRecording(send: true)
        }
    }

    private func startVoiceRecording() {
        permissionMessage = nil
        errorMessage = nil

        switch AVAudioSession.sharedInstance().recordPermission {
        case .granted:
            beginRecording()
        case .undetermined:
            AVAudioSession.sharedInstance().requestRecordPermission { granted in
                Task { @MainActor in
                    if granted {
                        beginRecording()
                    } else {
                        permissionMessage = "Microphone permission is required for voice translation. Enable it in Settings."
                    }
                }
            }
        case .denied:
            permissionMessage = "Microphone permission is required for voice translation. Enable it in Settings."
        @unknown default:
            permissionMessage = "Microphone permission is required for voice translation. Enable it in Settings."
        }
    }

    private func beginRecording() {
        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.playAndRecord, mode: .spokenAudio, options: [.defaultToSpeaker])
            try session.setActive(true)

            let url = FileManager.default.temporaryDirectory.appendingPathComponent("visepanda-voice.m4a")
            try? FileManager.default.removeItem(at: url)
            let settings: [String: Any] = [
                AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
                AVSampleRateKey: 16_000,
                AVNumberOfChannelsKey: 1,
                AVEncoderAudioQualityKey: AVAudioQuality.medium.rawValue
            ]
            let nextRecorder = try AVAudioRecorder(url: url, settings: settings)
            nextRecorder.record(forDuration: 30)
            recorder = nextRecorder
            nextRecorder.record()
            recordingTask?.cancel()
            recordingTask = Task {
                try? await Task.sleep(nanoseconds: 30_000_000_000)
                await MainActor.run { stopRecording(send: true) }
            }
        } catch {
            permissionMessage = "Recording failed to start. Try again."
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
            Task { await handleRecordedAudio(url) }
        }
    }

    @MainActor
    private func handleRecordedAudio(_ url: URL) async {
        processingMode = .stt
        permissionMessage = nil
        errorMessage = nil
        result = nil

        do {
            let audio = try Data(contentsOf: url)
            guard !audio.isEmpty else {
                errorMessage = "stt_failed"
                processingMode = nil
                return
            }
            let response = try await api.translateStt(audioBase64: audio.base64EncodedString())
            if response.ok, let text = response.text?.trimmingCharacters(in: .whitespacesAndNewlines), !text.isEmpty {
                input = text
                await translate(text)
            } else {
                errorMessage = response.error ?? "stt_failed"
            }
        } catch {
            errorMessage = error.localizedDescription
        }
        processingMode = nil
    }

    private func speak(_ text: String, languageCode: String) {
        Task { await speakWithBackendFallback(text, languageCode: languageCode) }
    }

    @MainActor
    private func speakWithBackendFallback(_ text: String, languageCode: String) async {
        if tts.isSpeaking { tts.stopSpeaking(at: .immediate) }
        audioPlayer?.pause()
        speaking = true
        do {
            let language = SupportedLanguages.byCode(languageCode)
            let audio = try await api.translateTts(text: text, language: language.ttsLanguageName)
            guard let url = audio.httpsURL else { throw URLError(.badURL) }
            let player = AVPlayer(url: url)
            audioPlayer = player
            player.play()
            try? await Task.sleep(nanoseconds: 1_600_000_000)
            speaking = false
        } catch {
            speakLocally(text, languageCode: languageCode)
        }
    }

    private func speakLocally(_ text: String, languageCode: String) {
        if tts.isSpeaking { tts.stopSpeaking(at: .immediate) }
        speaking = true
        let utterance = AVSpeechUtterance(string: text)
        utterance.voice = AVSpeechSynthesisVoice(language: SupportedLanguages.byCode(languageCode).speechLocale)
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
    case ocr = "translate-ocr"
    case stt = "translate-stt"
    case permission = "translate-permission"

    static var launchArgument: TranslateScreenshotScenario? {
        ProcessInfo.processInfo.arguments
            .compactMap(TranslateScreenshotScenario.init(rawValue:))
            .first
    }
}
#endif

private enum TranslateProcessingMode {
    case ocr
    case stt
}

private struct TranslateImagePickerSource: Identifiable {
    let id = UUID()
    let sourceType: UIImagePickerController.SourceType
}

private struct ImagePicker: UIViewControllerRepresentable {
    let sourceType: UIImagePickerController.SourceType
    let onImage: (UIImage) -> Void
    @Environment(\.dismiss) private var dismiss

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.sourceType = sourceType
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(parent: self)
    }

    final class Coordinator: NSObject, UINavigationControllerDelegate, UIImagePickerControllerDelegate {
        let parent: ImagePicker

        init(parent: ImagePicker) {
            self.parent = parent
        }

        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
            if let image = info[.originalImage] as? UIImage {
                parent.onImage(image)
            }
            parent.dismiss()
        }

        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            parent.dismiss()
        }
    }
}

private extension UIImage {
    func compressedForTranslation() -> Data? {
        let maxSide: CGFloat = 1200
        let scale = min(1, maxSide / max(size.width, size.height))
        let targetSize = CGSize(width: size.width * scale, height: size.height * scale)
        let format = UIGraphicsImageRendererFormat.default()
        format.scale = 1
        let image = UIGraphicsImageRenderer(size: targetSize, format: format).image { _ in
            draw(in: CGRect(origin: .zero, size: targetSize))
        }
        return image.jpegData(compressionQuality: 0.82)
    }
}

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
