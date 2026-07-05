package space.go2china.visepanda.ui.butler

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.File
import space.go2china.visepanda.data.model.ButlerChatMessage
import space.go2china.visepanda.data.model.ButlerMessageRole
import space.go2china.visepanda.data.model.ExploreRef
import space.go2china.visepanda.data.model.InlineToolCard
import space.go2china.visepanda.data.model.InlineToolCardTone
import space.go2china.visepanda.ui.theme.Dimens

@Composable
fun ButlerScreen(
    onOpenToolCategory: (String) -> Unit,
    onExploreRef: (ExploreRef) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ButlerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Voice input (real-device feedback, 2026-07-05): same MediaRecorder ->
    // base64 -> STT flow as Translate (ui/translate/TranslateScreen.kt), but
    // the transcribed text fills the composer instead of auto-sending, so the
    // traveler can review/edit before it goes out.
    val audioFile = remember { File(context.cacheDir, "butler_voice_record.mp4") }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }

    fun startRecording() {
        try {
            if (audioFile.exists()) audioFile.delete()
            val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000)
                setAudioEncodingBitRate(32000)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            viewModel.setRecordingState(true)
        } catch (e: Exception) {
            viewModel.setRecordingState(false)
        }
    }

    fun stopRecordingAndTranscribe() {
        val recorder = mediaRecorder ?: return
        try {
            recorder.stop()
            recorder.release()
        } catch (e: Exception) {
            // Recording too short or release error — nothing to transcribe.
        } finally {
            mediaRecorder = null
        }

        if (audioFile.exists() && audioFile.length() > 0) {
            val base64Audio = Base64.encodeToString(audioFile.readBytes(), Base64.NO_WRAP)
            viewModel.performStt(base64Audio)
        } else {
            viewModel.setRecordingState(false)
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) startRecording() }

    fun onMicClick() {
        if (state.isRecording) {
            stopRecordingAndTranscribe()
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startRecording()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Auto-stop at 30s, matching Translate's cap.
    LaunchedEffect(state.isRecording) {
        if (state.isRecording) {
            var seconds = 0
            while (seconds < 30 && state.isRecording) {
                kotlinx.coroutines.delay(1000)
                seconds++
            }
            if (state.isRecording) stopRecordingAndTranscribe()
        }
    }

    Scaffold(modifier = modifier) { innerPadding ->
        ButlerContent(
            state = state,
            onInputChange = viewModel::updateInput,
            onSend = viewModel::sendCurrentInput,
            onSuggestion = viewModel::sendSuggestion,
            onOpenToolCategory = onOpenToolCategory,
            onExploreRef = onExploreRef,
            onMicClick = ::onMicClick,
            contentPadding = innerPadding,
        )
    }
}

@Composable
private fun ButlerContent(
    state: ButlerUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onSuggestion: (String) -> Unit,
    onOpenToolCategory: (String) -> Unit,
    onExploreRef: (ExploreRef) -> Unit,
    onMicClick: () -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .imePadding(),
    ) {
        ButlerHeader(state)

        if (state.sending) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(Dimens.SpaceLG),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMD),
        ) {
            if (state.messages.isEmpty()) {
                item {
                    EmptyButlerPrompt(onSuggestion = onSuggestion)
                }
            } else {
                items(state.messages, key = { it.id }) { message ->
                    MessageBubble(message, onOpenToolCategory = onOpenToolCategory, onExploreRef = onExploreRef)
                }
            }
        }

        ButlerComposer(
            state = state,
            onInputChange = onInputChange,
            onSend = onSend,
            onSuggestion = onSuggestion,
            onMicClick = onMicClick,
        )
    }
}

@Composable
private fun ButlerHeader(state: ButlerUiState) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.SpaceXS,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceLG)) {
            Text(
                text = "VisePanda Butler",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = if (state.offlineFallback) {
                    "Offline"
                } else {
                    "Ready"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyButlerPrompt(onSuggestion: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceLG)) {
            Text(
                text = "Start with what you need in China.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Ask for a lighter day, booking gaps, food ideas, or a practical next step. The plan updates in Today and Plan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceMD))
            // v0.3.11: a horizontally-scrollable single row instead of a
            // wrapping FlowRow — operator feedback was that suggestion chips
            // took up too much vertical space (DESIGN.md ADR-115).
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                items(ButlerUiState.DEFAULT_SUGGESTIONS) { suggestion ->
                    AssistChip(
                        onClick = { onSuggestion(suggestion) },
                        label = { Text(suggestion) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ButlerChatMessage,
    onOpenToolCategory: (String) -> Unit,
    onExploreRef: (ExploreRef) -> Unit,
) {
    val isUser = message.role == ButlerMessageRole.User
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Card(
            shape = RoundedCornerShape(Dimens.RadiusLG),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                contentColor = if (isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            ),
            modifier = Modifier.fillMaxWidth(if (isUser) 0.82f else 0.92f),
        ) {
            Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
                val response = message.response
                val bubbleContentColor = if (isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
                if (response == null) {
                    Text(text = message.content, style = MaterialTheme.typography.bodyLarge, color = bubbleContentColor)
                } else {
                    Text(text = response.headline, style = MaterialTheme.typography.titleMedium, color = bubbleContentColor)
                    Spacer(modifier = Modifier.height(Dimens.SpaceXS))
                    Text(text = response.body, style = MaterialTheme.typography.bodyMedium, color = bubbleContentColor)
                    if (response.highlights.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                        response.highlights.forEach { highlight ->
                            Text(text = "- $highlight", style = MaterialTheme.typography.bodySmall, color = bubbleContentColor)
                        }
                    }
                    response.watchOut?.let {
                        Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                    Text(
                        text = response.nextStep,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    response.toolCards.orEmpty().forEach { toolCard ->
                        Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                        InlineToolCardView(toolCard, onOpenToolCategory = onOpenToolCategory)
                    }
                    if (!response.exploreRefs.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                            items(response.exploreRefs, key = { it.amapPoiId }) { ref ->
                                ExploreRefCardView(ref, onClick = { onExploreRef(ref) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreRefCardView(ref: ExploreRef, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(Dimens.RadiusMD),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.width(180.dp),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
            Text(
                text = ref.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceXS))
            Row {
                ref.rating?.let {
                    Text(
                        text = "★ ${String.format("%.1f", it)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                ref.pricePerPerson?.let {
                    Text(
                        text = "¥${it.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineToolCardView(toolCard: InlineToolCard, onOpenToolCategory: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(Dimens.RadiusMD),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
            Text(
                text = toolCard.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = toolCard.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (toolCard.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.SpaceXS))
                toolCard.items.forEach { item ->
                    Text(
                        text = "- $item",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpaceXS))
            // v0.3.13: Tools now has real content (DESIGN.md ADR-117), so
            // this deep-links to the matching category detail screen instead
            // of rendering as a dead label the way it did in v0.3.12.
            TextButton(onClick = { onOpenToolCategory(toolCard.categoryId) }) {
                Text(
                    text = toolCard.nextAction,
                    style = MaterialTheme.typography.labelMedium,
                    color = when (toolCard.tone) {
                        InlineToolCardTone.Warning -> MaterialTheme.colorScheme.error
                        InlineToolCardTone.Success -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    },
                )
            }
        }
    }
}

@Composable
private fun ButlerComposer(
    state: ButlerUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onSuggestion: (String) -> Unit,
    onMicClick: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.SpaceXS,
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Bottom padding clears the v0.3.10 floating overlay nav bar
        // (DESIGN.md ADR-114) — Chat is a top-level destination, so the
        // send/camera/mic controls must never render underneath it.
        Column(
            modifier = Modifier.padding(
                start = Dimens.SpaceLG,
                end = Dimens.SpaceLG,
                top = Dimens.SpaceLG,
                bottom = Dimens.BottomNavContentClearance,
            ),
        ) {
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
            }
            // v0.3.11: a horizontally-scrollable single row instead of a
            // wrapping FlowRow — operator feedback was that the suggestion
            // row took up too large a share of the composer, crowding out
            // the input box (DESIGN.md ADR-115).
            if (state.suggestions.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                    items(state.suggestions.take(5)) { suggestion ->
                        AssistChip(
                            onClick = { onSuggestion(suggestion) },
                            label = { Text(suggestion) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM),
            ) {
                // Camera/Mic move inside the text field as leading/trailing icons.
                OutlinedTextField(
                    value = state.input,
                    onValueChange = onInputChange,
                    enabled = !state.sending,
                    placeholder = {
                        Text(
                            if (state.isTranscribing) "Transcribing..."
                            else if (state.isRecording) "Recording... tap mic to stop"
                            else "Ask VisePanda...",
                        )
                    },
                    minLines = 2,
                    maxLines = 6,
                    shape = RoundedCornerShape(Dimens.RadiusXL),
                    leadingIcon = {
                        // Camera stays a visual-only placeholder: sending an
                        // image to Chat needs a vision-capable backend
                        // contract (multimodal message format, provider
                        // capability routing) that doesn't exist yet — see
                        // Issue #71's follow-up. Voice input below needed no
                        // backend contract change, so it's wired for real.
                        IconButton(onClick = {}, enabled = false) {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = "Camera (needs backend vision support)")
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = onMicClick, enabled = !state.sending && !state.isTranscribing) {
                            Icon(
                                Icons.Filled.Mic,
                                contentDescription = if (state.isRecording) "Stop recording" else "Voice input",
                                tint = if (state.isRecording) MaterialTheme.colorScheme.error else LocalContentColor.current,
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = onSend,
                    enabled = state.input.isNotBlank() && !state.sending,
                ) {
                    Text("Send")
                }
            }
        }
    }
}
