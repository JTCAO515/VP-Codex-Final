package space.go2china.visepanda.ui.translate

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import android.Manifest
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import space.go2china.visepanda.R
import space.go2china.visepanda.data.model.Phrase
import space.go2china.visepanda.data.model.SupportedLanguage
import space.go2china.visepanda.data.model.SupportedLanguages
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

    // File setup for Camera Capture & Voice recording
    val photoFile = remember {
        File(context.cacheDir, "camera_capture.jpg").apply {
            if (exists()) delete()
        }
    }
    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "space.go2china.visepanda.fileprovider",
            photoFile
        )
    }
    val audioFile = remember {
        File(context.cacheDir, "voice_record.mp4")
    }

    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }

    fun startRecording() {
        try {
            if (audioFile.exists()) audioFile.delete()
            val recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
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
            viewModel.setPermissionError(null)
        } catch (e: Exception) {
            viewModel.setPermissionError("Recording failed to start: ${e.message}")
        }
    }

    fun stopRecordingAndSend() {
        val recorder = mediaRecorder ?: return
        try {
            recorder.stop()
            recorder.release()
        } catch (e: Exception) {
            // Recording too short or release error
        } finally {
            mediaRecorder = null
            viewModel.setRecordingState(false)
        }
        
        if (audioFile.exists() && audioFile.length() > 0) {
            try {
                val audioBytes = audioFile.readBytes()
                val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
                viewModel.performStt(base64Audio, "audio/mpeg", "zh")
            } catch (e: Exception) {
                viewModel.setPermissionError("Audio conversion failed")
            }
        }
    }

    // Auto-stop recording if it reaches 30 seconds
    LaunchedEffect(state.isRecording) {
        if (state.isRecording) {
            var seconds = 0
            while (seconds < 30 && state.isRecording) {
                kotlinx.coroutines.delay(1000)
                seconds++
            }
            if (state.isRecording) {
                stopRecordingAndSend()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaRecorder?.let {
                try {
                     it.stop()
                     it.release()
                } catch (e: Exception) {}
            }
        }
    }

    // Launchers
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            try {
                val base64Image = compressImageToBase64(context, photoUri)
                viewModel.performOcr(base64Image)
            } catch (e: Exception) {
                viewModel.setPermissionError("Image processing failed: ${e.message}")
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(photoUri)
        } else {
            viewModel.setPermissionError(context.getString(R.string.translate_perm_camera_explain))
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecording()
        } else {
            viewModel.setPermissionError(context.getString(R.string.translate_perm_mic_explain))
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showBigCardPhrase by remember { mutableStateOf<Phrase?>(null) }
    var showBigCardLanguageCode by remember { mutableStateOf("zh") }

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

    fun localeForLanguageCode(code: String): Locale = when (code) {
        "zh" -> Locale.SIMPLIFIED_CHINESE
        "ja" -> Locale.JAPANESE
        "ko" -> Locale.KOREAN
        "fr" -> Locale.FRENCH
        "es" -> Locale("es")
        "ar" -> Locale("ar")
        else -> Locale.US
    }

    fun localSpeak(text: String, languageCode: String) {
        if (ttsReady) {
            ttsEngine?.setLanguage(localeForLanguageCode(languageCode))
            ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_${text.hashCode()}")
        } else {
            Toast.makeText(context, context.getString(R.string.translate_speak_not_ready), Toast.LENGTH_SHORT).show()
        }
    }

    // Backend Qwen TTS playback: the ViewModel resolves an audioUrl and this
    // plays it via MediaPlayer, releasing the player when done or on error.
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    LaunchedEffect(state.ttsAudioUrl) {
        val url = state.ttsAudioUrl ?: return@LaunchedEffect
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setOnPreparedListener { it.start() }
            setOnCompletionListener { it.release() }
            setOnErrorListener { player, _, _ -> player.release(); true }
            try {
                setDataSource(url)
                prepareAsync()
            } catch (e: Exception) {
                release()
            }
        }
        viewModel.clearTtsAudioUrl()
    }

    // Backend TTS failed (offline / upstream error) — fall back to the
    // device's local system TTS so the "speak" affordance never dead-ends.
    LaunchedEffect(state.ttsFallbackText) {
        val text = state.ttsFallbackText ?: return@LaunchedEffect
        localSpeak(text, state.ttsFallbackLanguageCode ?: "zh")
        viewModel.clearTtsFallback()
    }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
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
                        onPhraseClick = { phrase, languageCode ->
                            showBigCardPhrase = phrase
                            showBigCardLanguageCode = languageCode
                        },
                        photoUri = photoUri,
                        takePictureLauncher = takePictureLauncher,
                        cameraPermissionLauncher = cameraPermissionLauncher,
                        micPermissionLauncher = micPermissionLauncher,
                        startRecording = ::startRecording,
                        stopRecordingAndSend = ::stopRecordingAndSend
                    )
                } else {
                    PhrasebookTabContent(
                        phrases = state.phrases,
                        onPhraseClick = { phrase, languageCode ->
                            showBigCardPhrase = phrase
                            showBigCardLanguageCode = languageCode
                        },
                        viewModel = viewModel,
                    )
                }
            }
        }
    }

    // Big Card Presentation (Street Handoff Dialog — Show to local people)
    showBigCardPhrase?.let { phrase ->
        Dialog(onDismissRequest = { showBigCardPhrase = null }) {
            Surface(
                shape = RoundedCornerShape(Dimens.RadiusLG),
                border = BorderStroke(2.dp, Ink),
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.SpaceMD)
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.SpaceXL),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.translate_show_local),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkSoft
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpaceLG))
                    Text(
                        text = phrase.chinese,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        textAlign = TextAlign.Center,
                        lineHeight = 42.sp
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpaceMD))
                    Text(
                        text = phrase.pinyin,
                        fontSize = 18.sp,
                        color = InkSoft,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpaceXL))
                    Text(
                        text = phrase.english,
                        fontSize = 16.sp,
                        color = Ink.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpaceXL))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = { viewModel.speak(phrase.chinese, showBigCardLanguageCode) },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Ink, CircleShape),
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speak phrase",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        IconButton(
                            onClick = { showBigCardPhrase = null },
                            modifier = Modifier
                                .size(56.dp)
                                .border(1.dp, Ink, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Ink
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TranslatorTabContent(
    state: TranslateUiState,
    viewModel: TranslateViewModel,
    onPhraseClick: (Phrase, String) -> Unit,
    photoUri: Uri,
    takePictureLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    cameraPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    micPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    startRecording: () -> Unit,
    stopRecordingAndSend: () -> Unit,
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimens.SpaceLG),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)
    ) {
        item {
            LanguageSelectorRow(
                fromLanguage = state.fromLanguage,
                toLanguage = state.toLanguage,
                onFromChange = viewModel::setFromLanguage,
                onToChange = viewModel::setToLanguage,
                onSwap = viewModel::swapLanguages,
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                shape = RoundedCornerShape(Dimens.RadiusLG),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
                    Text(
                        text = SupportedLanguages.byCode(state.fromLanguage).displayName,
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
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onPhraseClick(
                                Phrase(
                                    category = "Translation",
                                    english = state.input,
                                    chinese = state.translationResult.translation,
                                    pinyin = state.translationResult.pinyin
                                ),
                                state.toLanguage,
                            )
                        }
                ) {
                    Column(modifier = Modifier.padding(Dimens.SpaceMD)) {
                        Text(
                            text = SupportedLanguages.byCode(state.toLanguage).displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                        Text(
                            text = state.translationResult.translation,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Ink
                        )

                        if (state.toLanguage == "zh" && state.translationResult.pinyin.isNotEmpty()) {
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
                                onClick = { viewModel.speak(state.translationResult.translation, state.toLanguage) },
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

        if (state.isProcessing) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(Dimens.RadiusLG),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.SpaceMD),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpaceSM))
                        Text(
                            text = stringResource(R.string.translate_status_processing),
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkSoft
                        )
                    }
                }
            }
        }

        if (state.permissionError != null) {
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
                            imageVector = Icons.Default.Info,
                            contentDescription = "Permission Alert",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpaceSM))
                        Text(
                            text = state.permissionError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        if (state.errorMessage != null) {
            item {
                val errorText = when (state.errorMessage) {
                    "ocr_failed" -> stringResource(R.string.translate_error_ocr_failed)
                    "stt_failed" -> stringResource(R.string.translate_error_stt_failed)
                    else -> stringResource(R.string.translate_error_unavailable)
                }
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
                            text = errorText,
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
            TranslateCard(
                title = stringResource(R.string.translate_camera_title),
                icon = Icons.Default.PhotoCamera,
                onClick = {
                    val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.CAMERA
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        takePictureLauncher.launch(photoUri)
                    } else {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                }
            )
        }

        item {
            TranslateCard(
                title = if (state.isRecording) {
                    stringResource(R.string.translate_status_recording)
                } else {
                    stringResource(R.string.translate_voice_title)
                },
                icon = if (state.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                containerColor = if (state.isRecording) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                onClick = {
                    if (state.isRecording) {
                        stopRecordingAndSend()
                    } else {
                        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.RECORD_AUDIO
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            startRecording()
                        } else {
                            micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun LanguageSelectorRow(
    fromLanguage: String,
    toLanguage: String,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit,
    onSwap: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LanguageDropdown(
            modifier = Modifier.weight(1f),
            selectedCode = fromLanguage,
            onSelect = onFromChange,
        )
        IconButton(onClick = onSwap) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Swap language direction",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        LanguageDropdown(
            modifier = Modifier.weight(1f),
            selectedCode = toLanguage,
            onSelect = onToChange,
        )
    }
}

@Composable
private fun LanguageDropdown(
    selectedCode: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(SupportedLanguages.byCode(selectedCode).displayName)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SupportedLanguages.all.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.displayName) },
                    onClick = {
                        onSelect(language.code)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranslateCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surface
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = InkSoft
        ),
        border = CardDefaults.outlinedCardBorder(),
        shape = RoundedCornerShape(Dimens.RadiusLG),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        onClick = onClick
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
                    tint = InkSoft
                )
                Spacer(modifier = Modifier.width(Dimens.SpaceSM))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go",
                tint = InkSoft
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhrasebookTabContent(
    phrases: List<Phrase>,
    onPhraseClick: (Phrase, String) -> Unit,
    viewModel: TranslateViewModel,
) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPhraseClick(phrase, "zh") }
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
                            onClick = { viewModel.speak(phrase.chinese, "zh") },
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

private fun compressImageToBase64(context: Context, uri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(uri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return ""
    
    val width = originalBitmap.width
    val height = originalBitmap.height
    val maxSide = 1200
    
    val (newWidth, newHeight) = if (width > height) {
        if (width > maxSide) {
            maxSide to (height * maxSide / width)
        } else {
            width to height
        }
    } else {
        if (height > maxSide) {
            (width * maxSide / height) to maxSide
        } else {
            width to height
        }
    }
    
    val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
    val outputStream = ByteArrayOutputStream()
    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}
