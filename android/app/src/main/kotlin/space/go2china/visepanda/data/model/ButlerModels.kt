package space.go2china.visepanda.data.model

import com.google.gson.annotations.SerializedName

enum class CanvasPatchIntent {
    @SerializedName("create_trip")
    CreateTrip,
    @SerializedName("adjust_trip")
    AdjustTrip,
    @SerializedName("add_alerts")
    AddAlerts,
}

data class AssistantResponse(
    val headline: String,
    val body: String,
    val highlights: List<String>,
    val watchOut: String? = null,
    val nextStep: String,
    // v0.3.12: mirrors the web contract's InlineToolCard[] (lib/types/trip.ts) —
    // see DESIGN.md ADR-116. Rendered as a read-only card in MessageBubble;
    // `href` deep-linking to a Tools category is intentionally not wired up
    // yet because ui/tools/ToolsScreen.kt is still an honest placeholder.
    val toolCards: List<InlineToolCard>? = null,
)

enum class InlineToolCardTone {
    @SerializedName("info")
    Info,
    @SerializedName("warning")
    Warning,
    @SerializedName("success")
    Success,
}

data class InlineToolCard(
    val id: String,
    val categoryId: String,
    val title: String,
    val summary: String,
    val items: List<String>,
    val nextAction: String,
    val href: String? = null,
    val tone: InlineToolCardTone? = null,
    val sourceLabel: String? = null,
)

data class CanvasPatch(
    val intent: CanvasPatchIntent,
    val assistantMessage: String,
    val assistantResponse: AssistantResponse? = null,
    val tripSummary: TripSummaryPatch? = null,
    val days: List<TripDay>? = null,
    val butlerAlerts: List<ButlerAlert>? = null,
    val reason: String,
)

data class TripSummaryPatch(
    val title: String? = null,
    val durationDays: Int? = null,
    val pace: Pace? = null,
    val travelerStyle: String? = null,
    val destinations: List<String>? = null,
    val confidence: TripConfidence? = null,
)

data class ButlerChatMessage(
    val id: String,
    val role: ButlerMessageRole,
    val content: String,
    val response: AssistantResponse? = null,
    val createdAtEpochMillis: Long,
)

enum class ButlerMessageRole {
    User,
    Assistant,
}

data class ButlerTurnResult(
    val assistantMessage: ButlerChatMessage,
    val trip: TripState,
    val suggestions: List<String>,
    val modelLabel: String,
    val offlineFallback: Boolean,
)
