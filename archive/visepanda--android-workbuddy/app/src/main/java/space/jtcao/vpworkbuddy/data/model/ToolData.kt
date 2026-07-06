package space.jtcao.vpworkbuddy.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Tool (toolkit item) — from GET /api/tools
 */
@Serializable
data class ToolItem(
    @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("icon") val icon: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("content") val content: ToolContent = ToolContent()
)

@Serializable
data class ToolContent(
    @SerialName("sections") val sections: List<ToolSection> = emptyList()
)

@Serializable
data class ToolSection(
    @SerialName("title") val title: String = "",
    @SerialName("body") val body: String = "",
    @SerialName("items") val items: List<String> = emptyList()
)
