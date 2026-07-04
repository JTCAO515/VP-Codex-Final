import Foundation

struct VisePandaAPIClient {
    var baseURL = URL(string: "https://go2china.space")!
    var session: URLSession = .shared

    func sendChat(
        message: String,
        trip: TripState,
        messages: [ChatMessage],
        preferenceProfile: UserPreferenceProfile?
    ) async throws -> ButlerChatResponse {
        var request = makeJSONRequest(path: "api/chat", method: "POST")
        request.httpBody = try JSONEncoder.visePanda.encode(
            ButlerChatRequest(
                message: message,
                trip: trip,
                messages: messages,
                preferenceProfile: preferenceProfile
            )
        )

        return try await perform(request, as: ButlerChatResponse.self)
    }

    func fetchExchangeRates() async throws -> ExchangeRateResponse {
        let request = makeJSONRequest(path: "api/exchange-rate", method: "GET")
        return try await perform(request, as: ExchangeRateResponse.self)
    }

    func translateText(_ text: String, from: String = "en", to: String = "zh") async throws -> TranslationResponse {
        var request = makeJSONRequest(path: "api/translate/text", method: "POST")
        request.httpBody = try JSONEncoder.visePanda.encode(
            TranslationRequest(text: text, from: from, to: to)
        )
        return try await perform(request, as: TranslationResponse.self)
    }

    private func makeJSONRequest(path: String, method: String) -> URLRequest {
        var request = URLRequest(url: baseURL.appendingPathComponent(path))
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("VisePanda-iOS/0.3.8", forHTTPHeaderField: "User-Agent")
        request.timeoutInterval = 30
        return request
    }

    private func perform<T: Decodable>(_ request: URLRequest, as type: T.Type) async throws -> T {
        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, (200..<300).contains(httpResponse.statusCode) else {
            let statusCode = (response as? HTTPURLResponse)?.statusCode ?? -1
            let body = String(data: data, encoding: .utf8) ?? ""
            throw VisePandaAPIError.http(statusCode: statusCode, body: body)
        }

        do {
            return try JSONDecoder.visePanda.decode(T.self, from: data)
        } catch {
            let body = String(data: data, encoding: .utf8) ?? ""
            throw VisePandaAPIError.decoding(error.localizedDescription, body: body)
        }
    }
}

enum VisePandaAPIError: LocalizedError {
    case http(statusCode: Int, body: String)
    case decoding(String, body: String)

    var errorDescription: String? {
        switch self {
        case let .http(statusCode, body):
            if body.isEmpty {
                return "HTTP \(statusCode)"
            }
            return "HTTP \(statusCode): \(body.prefix(180))"
        case let .decoding(message, body):
            if body.isEmpty {
                return "Decode failed: \(message)"
            }
            return "Decode failed: \(message). Body: \(body.prefix(180))"
        }
    }
}

struct ExchangeRateResponse: Codable {
    var ok: Bool
    var base: String?
    var rates: [String: Double]?
    var updatedAt: String?
    var error: String?
}

struct TranslationRequest: Codable {
    var text: String
    var from: String
    var to: String
}

struct TranslationResponse: Codable {
    var ok: Bool
    var provider: String?
    var model: String?
    var from: String?
    var to: String?
    var translation: String?
    var pinyin: String?
    var error: String?
    var qwenError: String?
    var deepSeekError: String?
}

extension JSONEncoder {
    static var visePanda: JSONEncoder {
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.sortedKeys]
        return encoder
    }
}

extension JSONDecoder {
    static var visePanda: JSONDecoder {
        JSONDecoder()
    }
}
