import SwiftUI

@main
struct VisePandaApp: App {
    @StateObject private var store = TripStore()
    @StateObject private var authStore = AuthStore()

    var body: some Scene {
        WindowGroup {
            AppRootView()
                .environmentObject(store)
                .environmentObject(authStore)
                .preferredColorScheme(.light)
        }
    }
}
