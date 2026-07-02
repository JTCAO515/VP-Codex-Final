package space.go2china.visepanda.ui.butler

import space.go2china.visepanda.data.model.ButlerChatMessage

data class ButlerUiState(
    val messages: List<ButlerChatMessage> = emptyList(),
    val input: String = "",
    val suggestions: List<String> = DEFAULT_SUGGESTIONS,
    val modelLabel: String = "Ready",
    val sending: Boolean = false,
    val offlineFallback: Boolean = false,
    val errorMessage: String? = null,
) {
    companion object {
        val DEFAULT_SUGGESTIONS = listOf(
            "Make today easier",
            "What should I do next?",
            "Check my booking gaps",
        )
    }
}
