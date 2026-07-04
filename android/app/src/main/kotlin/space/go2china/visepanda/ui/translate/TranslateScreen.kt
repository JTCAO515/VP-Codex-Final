package space.go2china.visepanda.ui.translate

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import space.go2china.visepanda.R
import space.go2china.visepanda.data.model.Phrase
import space.go2china.visepanda.ui.theme.Dimens
import space.go2china.visepanda.ui.theme.Ink
import space.go2china.visepanda.ui.theme.InkSoft
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TranslateViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // TTS Setup
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.translate_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.day_detail_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.translate_tab_translator)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.translate_tab_phrasebook)) }
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (selectedTab == 0) {
                    TranslatorTabContent(
                        state = state,
                        viewModel = viewModel,
                        ttsEngine = ttsEngine,
                        ttsReady = ttsReady
                    )
                } else {
                    PhrasebookTabContent(
                        phrases = state.phrases,
                        ttsEngine = ttsEngine,
                        ttsReady = ttsReady
                    )
                }
            }
        }
    }
}

@Composable
private fun TranslatorTabContent(
    state: TranslateUiState,
    viewModel: TranslateViewModel,
    ttsEngine: TextToSpeech?,
    ttsReady: Boolean,
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimens.SpaceLG),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                shape = RoundedCornerShape(Dimens.RadiusLG),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
                    Text(
                        text = if (state.translateToChinese) "English" else "中文",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpaceXS))
                    OutlinedTextField(
                        value = state.input,
                        onValueChange = viewModel::updateInput,
                        placeholder = { Text(stringResource(R.string.translate_input_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        trailingIcon = {
                            if (state.input.isNotEmpty()) {
                                IconButton(onClick = viewModel::clearInput) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::toggleDirection) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap language direction",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = stringResource(
                        if (state.translateToChinese) R.string.translate_direction_en_zh
                        else R.string.translate_direction_zh_en
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSoft
                )

                Button(
                    onClick = viewModel::translate,
                    enabled = state.input.isNotBlank() && !state.translating,
                    shape = RoundedCornerShape(Dimens.RadiusPill)
                ) {
                    if (state.translating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpaceXS))
                        Text(stringResource(R.string.translate_btn_translating))
                    } else {
                        Text(stringResource(R.string.translate_btn_translate))
                    }
                }
            }
        }

        if (state.translationResult != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(Dimens.RadiusLG),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
                        Text(
                            text = if (state.translateToChinese) "中文" else "English",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                        Text(
                            text = state.translationResult.translation,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Ink
                        )
                        
                        if (state.translateToChinese && state.translationResult.pinyin.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(Dimens.SpaceXS))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${stringResource(R.string.translate_pinyin)}: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = InkSoft
                                )
                                Text(
                                    text = state.translationResult.pinyin,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = InkSoft
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimens.SpaceMD))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("translation", state.translationResult.translation))
                                Toast.makeText(context, context.getString(R.string.translate_copy_success), Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                Spacer(modifier = Modifier.width(Dimens.SpaceXS))
                                Text("Copy")
                            }

                            Spacer(modifier = Modifier.width(Dimens.SpaceSM))

                            TextButton(
                                onClick = {
                                    if (ttsReady) {
                                        val loc = if (state.translateToChinese) Locale.SIMPLIFIED_CHINESE else Locale.US
                                        ttsEngine?.setLanguage(loc)
                                        ttsEngine?.speak(
                                            state.translationResult.translation,
                                            TextToSpeech.QUEUE_FLUSH,
                                            null,
                                            "translate_result_tts"
                                        )
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.translate_speak_not_ready), Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = ttsReady
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Speak")
                                Spacer(modifier = Modifier.width(Dimens.SpaceXS))
                                Text("Speak")
                            }
                        }
                    }
                }
            }
        }

        if (state.errorMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(Dimens.RadiusLG),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.SpaceMD),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpaceSM))
                        Text(
                            text = stringResource(R.string.translate_error_unavailable),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(Dimens.SpaceSM))
            Text(
                text = "Other Translation Modes",
                style = MaterialTheme.typography.titleSmall,
                color = InkSoft
            )
        }

        item {
            PlaceholderTranslateCard(
                title = stringResource(R.string.translate_camera_title),
                icon = Icons.Default.PhotoCamera
            )
        }

        item {
            PlaceholderTranslateCard(
                title = stringResource(R.string.translate_voice_title),
                icon = Icons.Default.Mic
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderTranslateCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = InkSoft.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(Dimens.RadiusLG),
        modifier = Modifier.fillMaxWidth(),
        enabled = false,
        onClick = {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceMD),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = InkSoft.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(Dimens.SpaceSM))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.translate_coming_soon),
                    style = MaterialTheme.typography.labelSmall,
                    color = InkSoft.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhrasebookTabContent(
    phrases: List<Phrase>,
    ttsEngine: TextToSpeech?,
    ttsReady: Boolean,
) {
    val context = LocalContext.current
    val groupedPhrases = remember(phrases) { phrases.groupBy { it.category } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimens.SpaceLG),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)
    ) {
        groupedPhrases.forEach { (category, categoryPhrases) ->
            stickyHeader {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = Dimens.SpaceSM)
                    )
                }
            }

            items(categoryPhrases) { phrase ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    shape = RoundedCornerShape(Dimens.RadiusMD),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.SpaceMD),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = phrase.english,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = phrase.chinese,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = phrase.pinyin,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSoft
                            )
                        }

                        IconButton(
                            onClick = {
                                if (ttsReady) {
                                    ttsEngine?.setLanguage(Locale.SIMPLIFIED_CHINESE)
                                    ttsEngine?.speak(
                                        phrase.chinese,
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        "phrase_speak_${phrase.chinese}"
                                    )
                                } else {
                                    Toast.makeText(context, context.getString(R.string.translate_speak_not_ready), Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = ttsReady
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Play pronunciation",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
