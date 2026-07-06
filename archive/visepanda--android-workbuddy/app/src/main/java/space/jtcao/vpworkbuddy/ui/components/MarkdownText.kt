package space.jtcao.vpworkbuddy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * Lightweight Markdown renderer for Compose Text.
 *
 * Supports:
 *   **bold**, *italic*, `code`, ### Header, - Lists, 1. Ordered lists,
 *   [links](url), ```code blocks```, --- horizontal rules, | tables |
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryAlpha = primaryColor.copy(alpha = 0.1f)

    Column(modifier = modifier) {
        val lines = text.split("\n")
        var i = 0
        var inList = false
        var inCodeBlock = false
        val codeLines = mutableListOf<String>()

        while (i < lines.size) {
            val line = lines[i]

            // Code block: ``` ... ```
            if (line.trimStart().startsWith("```")) {
                if (inCodeBlock) {
                    // End code block — render accumulated lines
                    renderCodeBlock(codeLines.joinToString("\n"), primaryColor, primaryAlpha)
                    codeLines.clear()
                    inCodeBlock = false
                } else {
                    // Start code block
                    inCodeBlock = true
                }
                i++
                continue
            }

            if (inCodeBlock) {
                codeLines.add(line)
                i++
                continue
            }

            // Table: | col1 | col2 |
            if (line.trimStart().startsWith("|") && line.trimEnd().endsWith("|")) {
                // Collect all table lines
                val tableLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trimStart().startsWith("|") && lines[i].trimEnd().endsWith("|")) {
                    tableLines.add(lines[i])
                    i++
                }
                renderTable(tableLines, primaryColor, onSurfaceColor)
                continue
            }

            val annotated = buildAnnotatedString {
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
                    }

                    // Unordered list: - item
                    line.matches(Regex("^-\\s.*")) -> {
                        if (!inList) inList = true
                        val content = line.replace(Regex("^-\\s"), "")
                        append("  •  ")
                        appendInlineStyled(content, primaryColor, primaryAlpha)
                    }

                    // Ordered list: 1. item
                    line.matches(Regex("^\\d+\\.\\s.*")) -> {
                        val num = line.substringBefore(".")
                        val content = line.replace(Regex("^\\d+\\.\\s"), "")
                        append("  $num. ")
                        appendInlineStyled(content, primaryColor, primaryAlpha)
                    }

                    // Horizontal rule: ---
                    line.matches(Regex("^-{3,}$")) -> {
                        append("\u2500".repeat(20))
                    }

                    // Blank line
                    line.isBlank() -> {
                        if (inList) inList = false
                    }

                    // Regular paragraph
                    else -> {
                        if (inList) inList = false
                        appendInlineStyled(line, primaryColor, primaryAlpha)
                    }
                }
            }

            Text(
                text = annotated,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            i++
        }

        // Unclosed code block at end of text
        if (codeLines.isNotEmpty()) {
            renderCodeBlock(codeLines.joinToString("\n"), primaryColor, primaryAlpha)
        }
    }
}

@Composable
private fun renderCodeBlock(
    code: String,
    primaryColor: Color,
    primaryAlpha: Color
) {
    Text(
        text = code,
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            color = primaryColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(primaryAlpha, RoundedCornerShape(8.dp))
            .padding(12.dp)
    )
}

@Composable
private fun renderTable(
    lines: List<String>,
    primaryColor: Color,
    onSurfaceColor: Color
) {
    if (lines.size < 2) return

    val rows = lines.map { line ->
        line.trim('|').split("|").map { it.trim() }
    }

    // Skip separator line (e.g. |---|---|)
    val header = rows.firstOrNull() ?: return
    val body = rows.drop(1).filter { row ->
        !row.all { it.all { c -> c == '-' || c == ':' } }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(primaryColor.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        // Header row
        Row(modifier = Modifier.fillMaxWidth()) {
            header.forEach { cell ->
                Text(
                    text = cell,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                )
            }
        }
        // Body rows
        body.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { cell ->
                    Text(
                        text = cell,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
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
                    fontFamily = FontFamily.Monospace,
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
