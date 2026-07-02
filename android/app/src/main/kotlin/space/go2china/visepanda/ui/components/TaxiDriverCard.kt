package space.go2china.visepanda.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import java.util.Locale
import space.go2china.visepanda.R
import space.go2china.visepanda.data.model.TripBlock
import space.go2china.visepanda.ui.theme.Dimens

/**
 * v0.3.2 synthesis explicitly removed the shake-to-trigger / power-button
 * global trigger from the v0.3.1 draft (see
 * docs/planning/v0.3.2-android-planning-synthesis.md, "Global Background
 * Taxi-Card Triggers Are Removed") in favor of a visible button on Today,
 * the current-trip card, and Day Detail. This composable is that single
 * shared implementation — every call site renders the same full-screen
 * bilingual card, so there is exactly one place to get the large-text/
 * copy-to-clipboard behavior right.
 */
@Composable
fun TaxiDriverCardButton(block: TripBlock, modifier: Modifier = Modifier) {
    var showCard by remember { mutableStateOf(false) }

    Button(onClick = { showCard = true }, modifier = modifier) {
        Text(stringResource(R.string.today_show_taxi_card))
    }

    if (showCard) {
        TaxiDriverCardDialog(block = block, onDismiss = { showCard = false })
    }
}

@Composable
private fun TaxiDriverCardDialog(block: TripBlock, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var copied by remember(block) { mutableStateOf(false) }

    // Speak matches the Figma reference's Copy/Speak action pair. Unlike the
    // camera/mic composer buttons (staged for v0.3.8), TextToSpeech needs no
    // runtime permission, so wiring it for real here doesn't cross the
    // "point-of-use permission" boundary the project otherwise holds to.
    var ttsReady by remember { mutableStateOf(false) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        lateinit var engine: TextToSpeech
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = engine.setLanguage(Locale.SIMPLIFIED_CHINESE)
                ttsReady = result == TextToSpeech.LANG_AVAILABLE ||
                    result == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                    result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
            }
        }
        ttsEngine = engine
        onDispose {
            engine.stop()
            engine.shutdown()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.taxi_card_title)) },
        text = {
            Column {
                Text(
                    // 52sp matches the high-contrast large-Chinese-character
                    // spec from the operator-approved Figma Make visual
                    // reference — see DESIGN.md ADR-105. lineHeight must be
                    // bumped along with fontSize (not left at headlineMedium's
                    // 32sp default), or multi-line Chinese addresses render
                    // with overlapping/crowded glyph strokes.
                    text = block.chineseAddress ?: block.title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 52.sp, lineHeight = 64.sp),
                )
                if (block.address != null) {
                    Text(
                        text = block.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Dimens.SpaceSM),
                    )
                }
                if (copied) {
                    Text(
                        text = stringResource(R.string.taxi_card_copied),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = Dimens.SpaceSM),
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(Dimens.SpaceSM),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = {
                    val text = block.chineseAddress ?: block.address ?: block.title
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("taxi_card_address", text))
                    copied = true
                }) {
                    Text(stringResource(R.string.taxi_card_copy_address))
                }
                TextButton(
                    onClick = {
                        val text = block.chineseAddress ?: block.address ?: block.title
                        ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "taxi_card_address")
                    },
                    enabled = ttsReady,
                ) {
                    Text(stringResource(R.string.taxi_card_speak))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.taxi_card_close))
                }
            }
        },
    )
}
