package space.go2china.visepanda.data.repository

import space.go2china.visepanda.data.model.AssistantResponse
import space.go2china.visepanda.data.model.CanvasPatch
import space.go2china.visepanda.data.model.CanvasPatchIntent
import space.go2china.visepanda.data.model.TripConfidence
import space.go2china.visepanda.data.model.TripState
import space.go2china.visepanda.data.model.TripSummaryPatch

object NativeButlerFallback {

    fun createPatch(message: String, current: TripState): CanvasPatch {
        val trimmed = message.trim().ifEmpty { "Help me refine my trip." }
        val lower = trimmed.lowercase()
        val reason = "Native fallback captured: $trimmed"
        val title = when {
            "nanjing" in lower -> "Nanjing 3-Day Trip"
            "shanghai" in lower -> "Shanghai City Trip"
            "beijing" in lower -> "Beijing Essentials"
            else -> current.summary.title
        }

        return CanvasPatch(
            intent = CanvasPatchIntent.AdjustTrip,
            assistantMessage = "I saved this request locally and will sync it when VisePanda is online: $trimmed",
            assistantResponse = AssistantResponse(
                headline = "Saved for the Butler",
                body = "The native app could not reach the live AI service, so I kept your request on this device and preserved your current itinerary.",
                highlights = listOf(
                    "Your trip stays available offline.",
                    "Today and Plan will keep showing the latest cached itinerary.",
                ),
                watchOut = "Live recommendations need the server connection to return.",
                nextStep = "Try again when you are back online",
            ),
            tripSummary = TripSummaryPatch(
                title = title,
                confidence = if (current.summary.confidence == TripConfidence.Draft) {
                    TripConfidence.Refined
                } else {
                    current.summary.confidence
                },
            ),
            reason = reason,
        )
    }

    fun suggestions(): List<String> = listOf(
        "Try again when online",
        "Show my current plan",
    )
}
