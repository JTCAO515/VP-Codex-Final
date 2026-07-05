import AuthenticationServices
import Foundation
import UIKit

@MainActor
final class AuthStore: NSObject, ObservableObject, ASWebAuthenticationPresentationContextProviding {
    @Published private(set) var session: SupabaseAuthSession?
    @Published private(set) var isLoading = false
    @Published var errorMessage: String?

    private let client = SupabaseAuthClient()
    private let keychain = KeychainSessionStore()
    private var webSession: ASWebAuthenticationSession?

    var email: String? { session?.user.email }
    var userId: String? { session?.user.id }
    var isSignedIn: Bool { session != nil }

    override init() {
        session = keychain.load()
        super.init()
        #if DEBUG
        if ProcessInfo.processInfo.arguments.contains("auth-signed-out") {
            keychain.clear()
            session = nil
        } else if ProcessInfo.processInfo.arguments.contains("auth-signed-in") {
            let sample = SupabaseAuthSession(
                accessToken: UUID().uuidString,
                refreshToken: UUID().uuidString,
                expiresIn: 3600,
                tokenType: "bearer",
                user: SupabaseAuthUser(id: "debug-user-id", email: "codex-ios-issue23@example.com")
            )
            try? keychain.save(sample)
            session = sample
        } else if ProcessInfo.processInfo.arguments.contains("auth-offline") {
            keychain.clear()
            session = nil
            errorMessage = "Network connection appears offline. Please try again."
        }
        #endif
    }

    func signIn(email: String, password: String) async {
        await runAuth {
            try await client.signIn(email: email, password: password)
        }
    }

    func signUp(email: String, password: String) async {
        await runAuth {
            try await client.signUp(email: email, password: password)
        }
    }

    func refreshSessionIfNeeded() async {
        #if DEBUG
        if ProcessInfo.processInfo.arguments.contains("auth-signed-in") {
            return
        }
        #endif
        guard let refreshToken = session?.refreshToken else { return }
        do {
            let refreshed = try await client.refresh(refreshToken)
            try keychain.save(refreshed)
            session = refreshed
            errorMessage = nil
        } catch {
            errorMessage = "Saved session could not be refreshed. Please sign in again."
        }
    }

    func signOut() async {
        isLoading = true
        defer { isLoading = false }
        if let accessToken = session?.accessToken {
            try? await client.signOut(accessToken: accessToken)
        }
        keychain.clear()
        session = nil
        errorMessage = nil
    }

    func signInWithGoogle() {
        let webSession = ASWebAuthenticationSession(
            url: client.googleOAuthURL(),
            callbackURLScheme: "space.go2china.visepanda.ios"
        ) { callbackURL, error in
            Task { @MainActor in
                self.isLoading = false
                self.webSession = nil
                if error != nil {
                    self.errorMessage = "Google OAuth was cancelled or is not configured for this app yet."
                    return
                }
                guard let callbackURL else {
                    self.errorMessage = "Google OAuth did not return a callback URL."
                    return
                }
                do {
                    let newSession = try await self.client.session(fromOAuthCallback: callbackURL)
                    try self.keychain.save(newSession)
                    self.session = newSession
                    self.errorMessage = nil
                } catch {
                    self.errorMessage = error.localizedDescription
                }
            }
        }
        isLoading = true
        errorMessage = nil
        webSession.presentationContextProvider = self
        webSession.prefersEphemeralWebBrowserSession = true
        self.webSession = webSession
        webSession.start()
    }

    func presentationAnchor(for session: ASWebAuthenticationSession) -> ASPresentationAnchor {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first { $0.isKeyWindow } ?? ASPresentationAnchor()
    }

    private func runAuth(_ operation: () async throws -> SupabaseAuthSession) async {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }

        do {
            let newSession = try await operation()
            try keychain.save(newSession)
            session = newSession
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
