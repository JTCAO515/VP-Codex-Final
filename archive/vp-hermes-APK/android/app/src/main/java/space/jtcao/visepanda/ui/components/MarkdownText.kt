package space.jtcao.visepanda.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

/**
 * Lightweight Markdown renderer for Compose Text.
 *
 * Supports: **bold**, *italic*, `code`, ### Header, - Lists, [links](url)
 * Uses [buildAnnotatedString] — zero external dependencies.
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryAlpha = primaryColor.copy(alpha = 0.1f)

    val annotatedString = buildAnnotatedString {
        val lines = text.split("\n")
        var inList = false

        lines.forEachIndexed { index, line ->
            when {
                // Header: ### Title
                line.matches(Regex("^#{1,3}\\s.*")) -> {
                    val content = line.replace(Regex("^#{1,3}\\s"), "")
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        color = onSurfaceColor
                    )) {
                        appendInlineStyled(content, primaryColor, primaryAlpha)
                    }
                    if (index < lines.lastIndex) append("\n\n")
                }

                // Unordered list: - item
                line.matches(Regex("^-\\s.*")) -> {
                    if (!inList) { inList = true }
                    val content = line.replace(Regex("^-\\s"), "")
                    append("  •  ")
                    appendInlineStyled(content, primaryColor, primaryAlpha)
                    if (index < lines.lastIndex) append("\n")
                }

                // Ordered list: 1. item
                line.matches(Regex("^\\d+\\.\\s.*")) -> {
                    val num = line.substringBefore(".")
                    val content = line.replace(Regex("^\\d+\\.\\s"), "")
                    append("  $num. ")
                    appendInlineStyled(content, primaryColor, primaryAlpha)
                    if (index < lines.lastIndex) append("\n")
                }

                // Horizontal rule: ---
                line.matches(Regex("^-{3,}$")) -> {
                    append("\u2500".repeat(20))
                    if (index < lines.lastIndex) append("\n")
                }

                // Regular paragraph
                line.isBlank() -> {
                    // Preserve paragraph breaks
                    if (index > 0 && index < lines.lastIndex) append("\n")
                }

                else -> {
                    if (inList) { inList = false; if (index > 0) append("\n") }
                    appendInlineStyled(line, primaryColor, primaryAlpha)
                    if (index < lines.lastIndex && lines[index + 1].isNotBlank()) append("\n")
                }
            }
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Parse inline markdown within a single line and append to this builder:
 *   **bold** → Bold
 *   *italic* → Italic
 *   `code`  → Monospace
 *   [text](url) → Link (styled as underlined primary)
 *
 * Colors are passed as params to avoid @Composable access in non-Composable context.
 */
private fun AnnotatedString.Builder.appendInlineStyled(
    text: String,
    primaryColor: Color,
    primaryAlpha: Color
) {
    val pattern = Regex("""(\*\*(.+?)\*\*)|(\*(.+?)\*)|(`(.+?)`)|(\[(.+?)\]\((.+?)\))""")
    var lastIndex = 0

    for (match in pattern.findAll(text)) {
        // Text before this match
        if (match.range.first > lastIndex) {
            append(text.substring(lastIndex, match.range.first))
        }

        when {
            // **bold**
            match.groupValues[1].isNotEmpty() -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groupValues[2])
                }
            }
            // *italic*
            match.groupValues[3].isNotEmpty() -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(match.groupValues[4])
                }
            }
            // `code`
            match.groupValues[5].isNotEmpty() -> {
                withStyle(SpanStyle(
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Normal,
                    color = primaryColor,
                    background = primaryAlpha
                )) {
                    append(match.groupValues[6])
                }
            }
            // [text](url)
            match.groupValues[7].isNotEmpty() -> {
                withStyle(SpanStyle(
                    color = primaryColor,
                    textDecoration = TextDecoration.Underline
                )) {
                    append(match.groupValues[8])
                }
            }
        }

        lastIndex = match.range.last + 1
    }

    // Remaining text after last match
    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}
