import SwiftUI

struct ChatView: View {
    @EnvironmentObject private var store: TripStore
    @State private var draft = ""

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
            ChatComposer(draft: $draft, isSending: store.isSending) {
                sendDraft()
            }
        }
        .background(VPColor.paper)
    }

    private var chatHeader: some View {
        HStack(spacing: 12) {
            Text("熊")
                .font(.system(size: 17, weight: .bold))
                .foregroundStyle(VPColor.paperSoft)
                .frame(width: 42, height: 42)
                .background(VPColor.cinnabar)
                .clipShape(Circle())

            VStack(alignment: .leading, spacing: 2) {
                Text("Butler")
                    .font(VPFont.display(22))
                    .foregroundStyle(VPColor.ink)
                Text("AI China Travel Butler")
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

    private func scrollToLatest(_ proxy: ScrollViewProxy) {
        guard let last = store.messages.last else { return }
        DispatchQueue.main.async {
            withAnimation(.easeOut(duration: 0.25)) {
                proxy.scrollTo(last.id, anchor: .bottom)
            }
        }
    }
}

private struct MessageBubble: View {
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
                Text("熊")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundStyle(VPColor.paperSoft)
                    .frame(width: 32, height: 32)
                    .background(VPColor.cinnabar)
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

private struct InlineToolCardView: View {
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
            Text("Butler is thinking...")
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
    let onSend: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "wand.and.stars")
                .foregroundStyle(VPColor.cinnabar)

            TextField("Ask VisePanda...", text: $draft, axis: .vertical)
                .font(VPFont.body(15))
                .lineLimit(1...4)
                .submitLabel(.send)
                .onSubmit(onSend)
                .disabled(isSending)

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
