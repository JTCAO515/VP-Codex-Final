import Foundation
import Security

struct KeychainSessionStore {
    private let service = "space.go2china.visepanda.ios.auth"
    private let account = "supabase-session"

    func load() -> SupabaseAuthSession? {
        var query = baseQuery()
        query[kSecReturnData as String] = true
        query[kSecMatchLimit as String] = kSecMatchLimitOne

        var item: CFTypeRef?
        guard SecItemCopyMatching(query as CFDictionary, &item) == errSecSuccess,
              let data = item as? Data else {
            return nil
        }
        return try? JSONDecoder.visePanda.decode(SupabaseAuthSession.self, from: data)
    }

    func save(_ session: SupabaseAuthSession) throws {
        let data = try JSONEncoder.visePanda.encode(session)
        let status = SecItemCopyMatching(baseQuery() as CFDictionary, nil)
        if status == errSecSuccess {
            let updateStatus = SecItemUpdate(baseQuery() as CFDictionary, [kSecValueData as String: data] as CFDictionary)
            if updateStatus != errSecSuccess { throw KeychainSessionStoreError.unhandled(updateStatus) }
        } else {
            var item = baseQuery()
            item[kSecValueData as String] = data
            item[kSecAttrAccessible as String] = kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
            let addStatus = SecItemAdd(item as CFDictionary, nil)
            if addStatus != errSecSuccess { throw KeychainSessionStoreError.unhandled(addStatus) }
        }
    }

    func clear() {
        SecItemDelete(baseQuery() as CFDictionary)
    }

    private func baseQuery() -> [String: Any] {
        [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account
        ]
    }
}

enum KeychainSessionStoreError: LocalizedError {
    case unhandled(OSStatus)

    var errorDescription: String? {
        switch self {
        case let .unhandled(status):
            "Could not save session to Keychain (OSStatus \(status))."
        }
    }
}
