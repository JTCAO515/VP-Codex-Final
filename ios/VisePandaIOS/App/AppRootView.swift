import SwiftUI

struct AppRootView: View {
    @EnvironmentObject private var store: TripStore

    var body: some View {
        #if DEBUG
        if let scenario = TranslateScreenshotScenario.launchArgument {
            NavigationStack {
                TranslateView(screenshotScenario: scenario)
            }
            .background(VPColor.paper)
            .tint(VPColor.cinnabar)
        } else {
            rootContent
        }
        #else
        rootContent
        #endif
    }

    private var rootContent: some View {
        ZStack(alignment: .bottom) {
            VPColor.paper.ignoresSafeArea()

            Group {
                switch store.selectedTab {
                case .trips:
                    TripsView()
                case .explore:
                    ExploreView()
                case .chat:
                    ChatView()
                case .tools:
                    ToolsView()
                case .me:
                    MeView()
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(.bottom, VPSpacing.bottomBarClearance)

            VisePandaBottomBar(selectedTab: $store.selectedTab)
                .ignoresSafeArea(.container, edges: .bottom)
        }
        .background(VPColor.paper)
        .tint(VPColor.cinnabar)
    }
}
