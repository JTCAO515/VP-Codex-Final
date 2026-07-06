package space.jtcao.visepanda.data.chat

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ChatSseClient {

    private val json = Json { ignoreUnknownKeys = true }

    internal fun parseForTest(eventType: String, data: String): ChatSseEvent? {
        return parseEvent(eventType, data)
    }

    private fun parseEvent(eventType: String, data: String): ChatSseEvent? {
        if (data.isBlank()) return null

        return when (eventType) {
            "message" -> parseMessagePayload(data)
            "done" -> ChatSseEvent.Done
            "error" -> ChatSseEvent.Error(data)
            else -> null
        }
    }

    private fun parseMessagePayload(data: String): ChatSseEvent? {
        return try {
            val obj = json.parseToJsonElement(data).jsonObject

            obj["token"]?.let {
                return ChatSseEvent.Token(it.jsonPrimitive.content)
            }
            if (obj["split"]?.jsonPrimitive?.booleanOrNull == true) {
                return ChatSseEvent.Split
            }
            obj["image"]?.jsonObject?.let { image ->
                return ChatSseEvent.Image(
                    key = image["key"]?.jsonPrimitive?.content ?: "",
                    url = image["url"]?.jsonPrimitive?.content ?: "",
                    label = image["label"]?.jsonPrimitive?.content ?: ""
                )
            }
            obj["faq"]?.jsonObject?.let { faq ->
                return ChatSseEvent.Faq(
                    id = faq["id"]?.jsonPrimitive?.content ?: "",
                    title = faq["title"]?.jsonPrimitive?.content ?: "",
                    icon = faq["icon"]?.jsonPrimitive?.content ?: ""
                )
            }
            if (obj["done"]?.jsonPrimitive?.booleanOrNull == true) {
                return ChatSseEvent.Done
            }
            if (obj["error"] != null) {
                return ChatSseEvent.Error(obj["error"]?.jsonPrimitive?.content ?: "Unknown error")
            }
            null
        } catch (_: Exception) {
            null
        }
    }
}
