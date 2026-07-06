package space.jtcao.visepanda.data.tools

import space.jtcao.visepanda.domain.model.ToolEntry
import space.jtcao.visepanda.domain.repository.ToolRepository

class ToolRepositoryImpl : ToolRepository {
    override suspend fun getEntries(): List<ToolEntry> = listOf(
        ToolEntry(
            id = "payment",
            title = "Payment",
            subtitle = "Mobile payment basics, cash backup, and card expectations in China.",
            category = "Essential"
        ),
        ToolEntry(
            id = "visa",
            title = "Visa & Entry",
            subtitle = "Entry documents, arrival cards, and what to double-check before departure.",
            category = "Essential"
        ),
        ToolEntry(
            id = "connectivity",
            title = "SIM / Internet",
            subtitle = "Stay connected on arrival with eSIM, SIM, Wi‑Fi, and app setup tips.",
            category = "Connectivity"
        ),
        ToolEntry(
            id = "transport",
            title = "Transport",
            subtitle = "Metro, ride-hailing, and high-speed rail basics for city-to-city travel.",
            category = "Mobility"
        ),
        ToolEntry(
            id = "safety",
            title = "Emergency",
            subtitle = "Quick reminders for medical help, local support, and urgent situations.",
            category = "Safety"
        ),
        ToolEntry(
            id = "culture",
            title = "Etiquette",
            subtitle = "Small social cues that help you feel confident in restaurants and hotels.",
            category = "Culture"
        ),
        ToolEntry(
            id = "language",
            title = "Useful Chinese",
            subtitle = "Helpful phrases for food, taxis, directions, hotels, and daily travel.",
            category = "Language"
        )
    )
}
