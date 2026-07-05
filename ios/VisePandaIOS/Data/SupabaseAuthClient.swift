import AuthenticationServices
import Foundation

struct SupabaseAuthClient {
    private let baseURL = URL(string: "https://eqbbnworuyksalfpimzw.supabase.co")!
    private let anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVxYmJud29ydXlrc2FsZnBpbXp3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODI3MzM3MzIsImV4cCI6MjA5ODMwOTczMn0.Ucr4lIxsz6w7EbIqbOQVYx1pauxJhOxS54UTL07WarM"
    private let callbackScheme = "space.go2china.visepanda.ios"
    var session: URLSession = .shared

    func signIn(email: String, password: String) async throws -> SupabaseAuthSession {
        var request = makeRequest(path: "auth/v1/token", method: "POST", queryItems: [
            URLQueryItem(name: "grant_type", value: "password")
        ])
        request.httpBody = try JSONEncoder.visePanda.encode(SupabaseAuthRequest(email: email, password: password))
        return try await perform(request, as: SupabaseAuthSession.self)
    }

    func signUp(email: String, password: String) async throws -> SupabaseAuthSession {
        var request = makeRequest(path: "auth/v1/signup", method: "POST")
        request.httpBody = try JSONEncoder.visePanda.encode(SupabaseAuthRequest(email: email, password: password))
        return try await perform(request, as: SupabaseAuthSession.self)
    }

    func refresh(_ refreshToken: String) async throws -> SupabaseAuthSession {
        var request = makeRequest(path: "auth/v1/token", method: "POST", queryItems: [
            URLQueryItem(name: "grant_type", value: "refresh_token")
        ])
        request.httpBody = try JSONEncoder.visePanda.encode(SupabaseRefreshRequest(refreshToken: refreshToken))
        return try await perform(request, as: SupabaseAuthSession.self)
    }

    func signOut(accessToken: String) async throws {
        var request = makeRequest(path: "auth/v1/logout", method: "POST")
        request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        _ = try await performEmpty(request)
    }

    func session(fromOAuthCallback url: URL) async throws -> SupabaseAuthSession {
        let values = callbackValues(url)
        guard let accessToken = values["access_token"],
              let refreshToken = values["refresh_token"] else {
            throw SupabaseAuthClientError.message("Google OAuth did not return a Supabase session.")
        }
        let user = try await fetchUser(accessToken: accessToken)
        return SupabaseAuthSession(
            accessToken: accessToken,
            refreshToken: refreshToken,
            expiresIn: Int(values["expires_in"] ?? "3600") ?? 3600,
            tokenType: values["token_type"] ?? "bearer",
            user: user
        )
    }

    func googleOAuthURL() -> URL {
        var components = URLComponents(url: baseURL.appendingPathComponent("auth/v1/authorize"), resolvingAgainstBaseURL: false)!
        components.queryItems = [
            URLQueryItem(name: "provider", value: "google"),
            URLQueryItem(name: "redirect_to", value: "\(callbackScheme)://auth-callback")
        ]
        return components.url!
    }

    private func makeRequest(path: String, method: String, queryItems: [URLQueryItem] = []) -> URLRequest {
        let url = baseURL.appendingPathComponent(path)
        var components = URLComponents(url: url, resolvingAgainstBaseURL: false)
        components?.queryItems = queryItems.isEmpty ? nil : queryItems

        var request = URLRequest(url: components?.url ?? url)
        request.httpMethod = method
        request.setValue(anonKey, forHTTPHeaderField: "apikey")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("VisePanda-iOS/0.3.8", forHTTPHeaderField: "User-Agent")
        request.timeoutInterval = 30
        return request
    }

    private func perform<T: Decodable>(_ request: URLRequest, as type: T.Type) async throws -> T {
        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse, (200..<300).contains(http.statusCode) else {
            throw decodeError(data, response: response)
        }
        return try JSONDecoder.visePanda.decode(T.self, from: data)
    }

    private func performEmpty(_ request: URLRequest) async throws {
        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse, (200..<300).contains(http.statusCode) else {
            throw decodeError(data, response: response)
        }
    }

    private func fetchUser(accessToken: String) async throws -> SupabaseAuthUser {
        var request = makeRequest(path: "auth/v1/user", method: "GET")
        request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        return try await perform(request, as: SupabaseAuthUser.self)
    }

    private func callbackValues(_ url: URL) -> [String: String] {
        var values: [String: String] = [:]
        if let queryItems = URLComponents(url: url, resolvingAgainstBaseURL: false)?.queryItems {
            queryItems.forEach { values[$0.name] = $0.value }
        }
        if let fragment = url.fragment {
            URLComponents(string: "x://callback?\(fragment)")?.queryItems?.forEach { values[$0.name] = $0.value }
        }
        return values
    }

    private func decodeError(_ data: Data, response: URLResponse) -> Error {
        if let error = try? JSONDecoder.visePanda.decode(SupabaseAuthError.self, from: data) {
            return SupabaseAuthClientError.message(error.displayMessage)
        }
        let status = (response as? HTTPURLResponse)?.statusCode ?? -1
        return SupabaseAuthClientError.message("Authentication service returned HTTP \(status)")
    }
}

enum SupabaseAuthClientError: LocalizedError {
    case message(String)

    var errorDescription: String? {
        switch self {
        case let .message(message): message
        }
    }
}
