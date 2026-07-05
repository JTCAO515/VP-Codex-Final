import Foundation

struct SupabaseAuthSession: Codable, Equatable {
    var accessToken: String
    var refreshToken: String
    var expiresIn: Int
    var tokenType: String
    var user: SupabaseAuthUser

    enum CodingKeys: String, CodingKey {
        case accessToken = "access_token"
        case refreshToken = "refresh_token"
        case expiresIn = "expires_in"
        case tokenType = "token_type"
        case user
    }
}

struct SupabaseAuthUser: Codable, Equatable {
    var id: String
    var email: String?
}

/// Supabase's `POST /auth/v1/signup` returns two different shapes depending
/// on project settings, both HTTP 200:
/// - Email confirmation OFF: the same session shape as sign-in
///   (`access_token`/`refresh_token`/... + nested `user`).
/// - Email confirmation ON (this project's actual config, confirmed by
///   `confirmation_sent_at` in the real response): a flat user object with no
///   `access_token` at all — the traveler must click the emailed link first.
/// Decoding this response straight into `SupabaseAuthSession` throws
/// `DecodingError.keyNotFound` for `access_token`, which iOS shows verbatim as
/// "The data couldn't be read because it is missing." — a real bug, not a
/// transient network failure. This type tries the session shape first, then
/// falls back to the flat user shape so signup can tell the two apart.
enum SupabaseSignUpResult {
    case signedIn(SupabaseAuthSession)
    case confirmationRequired(email: String)
}

struct SupabaseAuthRequest: Codable {
    var email: String
    var password: String
}

struct SupabaseRefreshRequest: Codable {
    var refreshToken: String

    enum CodingKeys: String, CodingKey {
        case refreshToken = "refresh_token"
    }
}

struct SupabaseAuthError: Codable {
    var error: String?
    var errorDescription: String?
    var msg: String?
    var message: String?

    enum CodingKeys: String, CodingKey {
        case error
        case errorDescription = "error_description"
        case msg
        case message
    }

    var displayMessage: String {
        message ?? msg ?? errorDescription ?? error ?? "Authentication failed"
    }
}
