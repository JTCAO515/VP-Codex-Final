import SwiftUI

struct VisePandaBottomBar: View {
    @Binding var selectedTab: RootTab

    var body: some View {
        ZStack(alignment: .top) {
            RoundedRectangle(cornerRadius: VPRadius.pill, style: .continuous)
                .fill(VPColor.ink)
                .frame(height: 72)
                .shadow(color: VPColor.ink.opacity(0.18), radius: 18, x: 0, y: 8)

            HStack(spacing: 0) {
                ForEach(RootTab.leftOfCenter) { tab in
                    sideButton(tab)
                }

                Spacer()
                    .frame(width: 74)

                ForEach(RootTab.rightOfCenter) { tab in
                    sideButton(tab)
                }
            }
            .padding(.horizontal, 18)
            .frame(height: 72)

            Button {
                withAnimation(.spring(response: 0.32, dampingFraction: 0.82)) {
                    selectedTab = .chat
                }
            } label: {
                Image(systemName: RootTab.chat.systemImage)
                    .font(.system(size: 28, weight: .semibold))
                    .foregroundStyle(VPColor.paperSoft)
                    .frame(width: 64, height: 64)
                    .background(selectedTab == .chat ? VPColor.cinnabar : VPColor.ink)
                    .clipShape(Circle())
                    .overlay {
                        Circle()
                            .stroke(VPColor.paper, lineWidth: 5)
                    }
                    .shadow(color: VPColor.ink.opacity(0.25), radius: 12, x: 0, y: 6)
            }
            .offset(y: -18)
            .accessibilityLabel("Chat")
        }
        .padding(.horizontal, 20)
        .padding(.bottom, 0)
    }

    private func sideButton(_ tab: RootTab) -> some View {
        Button {
            withAnimation(.spring(response: 0.32, dampingFraction: 0.82)) {
                selectedTab = tab
            }
        } label: {
            Image(systemName: selectedTab == tab ? "\(tab.systemImage).fill" : tab.systemImage)
                .font(.system(size: 23, weight: .semibold))
                .foregroundStyle(selectedTab == tab ? VPColor.cinnabar : VPColor.paper.opacity(0.62))
                .frame(maxWidth: .infinity)
                .frame(height: 54)
                .contentShape(Rectangle())
        }
        .accessibilityLabel(tab.title)
    }
}
