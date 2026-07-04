import SwiftUI

@main
struct VisePandaApp: App {
    @StateObject private var store = TripStore()

    var body: some Scene {
        WindowGroup {
            AppRootView()
                .environmentObject(store)
                .preferredColorScheme(.light)
        }
    }
}
