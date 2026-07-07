import Foundation

struct VisePandaAPIClient {
    var baseURL = URL(string: "https://go2china.space")!
    var session: URLSession = .shared

    func sendChat(
        message: String,
        trip: TripState,
        messages: [ChatMessage],
        preferenceProfile: UserPreferenceProfile?,
        completeSkeletonFor: TripState? = nil
    ) async throws -> ButlerChatResponse {
        var request = makeJSONRequest(path: "api/chat", method: "POST")
        request.httpBody = try JSONEncoder.visePanda.encode(
            ButlerChatRequest(
                message: message,
                trip: trip,
                messages: messages,
                preferenceProfile: preferenceProfile,
                completeSkeletonFor: completeSkeletonFor
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

    func translateOcr(imageBase64: String, mimeType: String = "image/jpeg") async throws -> TranslateOcrResponse {
        var request = makeJSONRequest(path: "api/translate/ocr", method: "POST")
        request.httpBody = try JSONEncoder.visePanda.encode(
            TranslateOcrRequest(imageBase64: imageBase64, mimeType: mimeType)
        )
        return try await perform(request, as: TranslateOcrResponse.self)
    }

    func translateStt(audioBase64: String, mimeType: String = "audio/mp4", language: String = "zh") async throws -> TranslateSttResponse {
        var request = makeJSONRequest(path: "api/translate/stt", method: "POST")
        request.httpBody = try JSONEncoder.visePanda.encode(
            TranslateSttRequest(audioBase64: audioBase64, mimeType: mimeType, language: language)
        )
        return try await perform(request, as: TranslateSttResponse.self)
    }

    func translateTts(text: String, language: String, voice: String? = nil) async throws -> TranslateTtsURL {
        var request = makeJSONRequest(path: "api/translate/tts", method: "POST")
        request.httpBody = try JSONEncoder.visePanda.encode(
            TranslateTtsRequest(text: text, language: language, voice: voice)
        )
        let response = try await perform(request, as: TranslateTtsResponse.self)
        guard response.ok, let audioUrl = response.audioUrl else {
            throw VisePandaAPIError.http(statusCode: -1, body: response.error ?? "TTS audio URL is blank")
        }
        return audioUrl
    }

    func fetchExploreAmap(
        cityId: String,
        type: String,
        page: Int,
        mode: String = "city",
        location: String? = nil,
        radius: Int? = nil,
        sort: String = "weight"
    ) async throws -> ExploreAmapResponse {
        var queryItems = [
            URLQueryItem(name: "cityId", value: cityId),
            URLQueryItem(name: "type", value: type),
            URLQueryItem(name: "page", value: String(page)),
            URLQueryItem(name: "mode", value: mode),
            URLQueryItem(name: "sort", value: sort)
        ]
        if let location {
            queryItems.append(URLQueryItem(name: "location", value: location))
        }
        if let radius {
            queryItems.append(URLQueryItem(name: "radius", value: String(radius)))
        }

        let request = makeJSONRequest(path: "api/explore/amap", method: "GET", queryItems: queryItems)
        return try await perform(request, as: ExploreAmapResponse.self)
    }

    func fetchMemoryProfile(userId: String) async throws -> UserMemoryProfileResponse {
        let request = makeJSONRequest(path: "butler/memory/profile", method: "GET", queryItems: [
            URLQueryItem(name: "userId", value: userId)
        ])
        return try await perform(request, as: UserMemoryProfileResponse.self)
    }

    func deleteMemoryProfileEntry(userId: String, key: String, value: String) async throws -> UserMemoryDeleteResponse {
        let request = makeJSONRequest(path: "butler/memory/profile", method: "DELETE", queryItems: [
            URLQueryItem(name: "userId", value: userId),
            URLQueryItem(name: "key", value: key),
            URLQueryItem(name: "value", value: value)
        ])
        return try await perform(request, as: UserMemoryDeleteResponse.self)
    }

    private func makeJSONRequest(path: String, method: String, queryItems: [URLQueryItem] = []) -> URLRequest {
        let url = baseURL.appendingPathComponent(path)
        var components = URLComponents(url: url, resolvingAgainstBaseURL: false)
        components?.queryItems = queryItems.isEmpty ? nil : queryItems

        var request = URLRequest(url: components?.url ?? url)
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
