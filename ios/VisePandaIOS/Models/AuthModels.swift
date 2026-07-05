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
