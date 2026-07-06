package space.jtcao.visepanda.data.repository

import space.jtcao.visepanda.data.model.Trip
import java.util.UUID

object TripAutoSave {

    fun createTripOrNull(city: String?, content: String): Trip? {
        val normalized = content.trim()
        if (normalized.isBlank()) return null

        val looksLikeTrip =
            Regex("""(?i)\bday\s*\d+\b""").containsMatchIn(normalized) ||
            normalized.contains("### Trip Overview") ||
            normalized.contains("Morning:") ||
            normalized.contains("Afternoon:") ||
            normalized.contains("Evening:")

        if (!looksLikeTrip) return null

        val detectedDays = Regex("""(?i)\bday\s*(\d+)\b""")
            .findAll(normalized)
            .mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }
            .maxOrNull() ?: 0

        val safeCity = city?.takeIf { it.isNotBlank() } ?: "china"
        val prettyCity = safeCity.replaceFirstChar { it.uppercase() }
        val title = if (detectedDays > 0) {
            "$prettyCity ${detectedDays}-day trip"
        } else {
            "$prettyCity trip"
        }

        return Trip(
            id = UUID.randomUUID().toString(),
            title = title,
            city = safeCity,
            days = detectedDays,
            content = normalized
        )
    }
}
