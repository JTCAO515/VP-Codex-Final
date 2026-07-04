package space.go2china.visepanda.ui.translate

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import space.go2china.visepanda.R
import space.go2china.visepanda.ui.theme.Dimens
import space.go2china.visepanda.ui.theme.Ink
import space.go2china.visepanda.ui.theme.Paper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TranslateViewModel = viewModel()
) {
    val context = LocalContext.current
    val inputText by viewModel.inputText.collectAsState()
    val isEnToZh by viewModel.isEnToZh.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isTtsReady by viewModel.isTtsReady.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Text, 1: Phrases
    var showBigCardPhrase by remember { mutableStateOf<Phrase?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.translate_title),
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Paper
                )
            )
        },
        containerColor = Paper,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.SpaceLG)
        ) {
            // Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpaceMD)
                    .background(Color(0xFFEBE8E2), RoundedCornerShape(Dimens.RadiusPill))
                    .padding(Dimens.SpaceXS),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabItem(
                    label = stringResource(R.string.translate_tab_text),
                    isSelected = activeTab == 0,
                    modifier = Modifier.weight(1f)
                ) { activeTab = 0 }
                
                TabItem(
                    label = stringResource(R.string.translate_tab_phrases),
                    isSelected = activeTab == 1,
                    modifier = Modifier.weight(1f)
                ) { activeTab = 1 }
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceSM))

            if (activeTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    // Language Switch Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Ink, RoundedCornerShape(Dimens.RadiusMD))
                            .background(Color.White)
                            .padding(vertical = Dimens.SpaceSM, horizontal = Dimens.SpaceLG),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEnToZh) "English" else "中文",
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        IconButton(onClick = { viewModel.toggleLanguageDirection() }) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch Languages",
                                tint = Ink
                            )
                        }
                        Text(
                            text = if (isEnToZh) "中文" else "English",
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimens.SpaceMD))

                    // Input Box
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { viewModel.setInputText(it) },
                        placeholder = {
                            Text(
                                text = if (isEnToZh) stringResource(R.string.translate_hint_en) else stringResource(R.string.translate_hint_zh),
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(Dimens.RadiusMD),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Ink,
                            unfocusedBorderColor = Ink.copy(alpha = 0.5f),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink
                        ),
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(Dimens.SpaceMD))

                    // Translation output / UI state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (val state = uiState) {
                            is TranslateUiState.Idle -> {
                                Text(
                                    text = stringResource(R.string.translate_placeholder_realtime),
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxWidth()
                                )
                            }
                            is TranslateUiState.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = Ink
                                )
                            }
                            is TranslateUiState.Success -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Ink, RoundedCornerShape(Dimens.RadiusMD))
                                        .background(Color(0xFFF9F7F3))
                                        .padding(Dimens.SpaceLG)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.translate_header_translation),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink.copy(alpha = 0.6f)
                                        )
                                        if (isEnToZh && isTtsReady) {
                                            IconButton(
                                                onClick = { viewModel.speakChinese(state.translatedText) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VolumeUp,
                                                    contentDescription = "Speak translation",
                                                    tint = Ink,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                                    Text(
                                        text = state.translatedText,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Ink
                                    )
                                }
                            }
                            is TranslateUiState.Error -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFFD32F2F), RoundedCornerShape(Dimens.RadiusMD))
                                        .background(Color(0xFFFFEBEE))
                                        .padding(Dimens.SpaceLG)
                                ) {
                                    Text(
                                        text = stringResource(R.string.translate_error_header),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F)
                                    )
                                    Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                                    Text(
                                        text = state.message,
                                        fontSize = 14.sp,
                                        color = Color(0xFFC62828)
                                    )
                                }
                            }
                        }
                    }

                    // Media Coming Soon section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Dimens.BottomNavContentClearance)
                    ) {
                        Text(
                            text = stringResource(R.string.translate_media_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Ink.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = Dimens.SpaceSM)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)
                        ) {
                            ComingSoonMediaCard(
                                title = stringResource(R.string.translate_camera_title),
                                description = stringResource(R.string.translate_camera_desc),
                                icon = Icons.Default.PhotoCamera,
                                modifier = Modifier.weight(1f)
                            ) {
                                Toast.makeText(
                                    context,
                                    "Camera translation is coming soon!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            ComingSoonMediaCard(
                                title = stringResource(R.string.translate_voice_title),
                                description = stringResource(R.string.translate_voice_desc),
                                icon = Icons.Default.Mic,
                                modifier = Modifier.weight(1f)
                            ) {
                                Toast.makeText(
                                    context,
                                    "Voice translation is coming soon!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            } else {
                // Phrase Book Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(bottom = Dimens.BottomNavContentClearance),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)
                ) {
                    items(PhraseDictionary.categories) { category ->
                        PhraseCategoryCard(
                            category = category,
                            onPhraseClick = { phrase ->
                                showBigCardPhrase = phrase
                            },
                            onSpeakClick = { text ->
                                viewModel.speakChinese(text)
                            }
                        )
                    }
                }
            }
        }
    }

    // Big Card Phrase Presentation (Street Handoff Dialog)
    showBigCardPhrase?.let { phrase ->
        Dialog(onDismissRequest = { showBigCardPhrase = null }) {
            Surface(
                shape = RoundedCornerShape(Dimens.RadiusLG),
                border = BorderStroke(2.dp, Ink),
                color = Paper,
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
                        color = Ink.copy(alpha = 0.5f)
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
                        color = Color.DarkGray,
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
                            onClick = { viewModel.speakChinese(phrase.chinese) },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Ink, CircleShape)
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
fun TabItem(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadiusPill))
            .background(if (isSelected) Ink else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = Dimens.SpaceSM),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Ink,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ComingSoonMediaCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .border(1.dp, Ink.copy(alpha = 0.3f), RoundedCornerShape(Dimens.RadiusMD))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(Dimens.SpaceMD)
            .alpha(0.6f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Ink,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(Dimens.SpaceSM))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Ink
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            fontSize = 10.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Dimens.SpaceXS))
        Surface(
            color = Color(0xFFECEFF1),
            shape = RoundedCornerShape(Dimens.RadiusSM)
        ) {
            Text(
                text = stringResource(R.string.translate_coming_soon),
                fontSize = 9.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PhraseCategoryCard(
    category: PhraseCategory,
    onPhraseClick: (Phrase) -> Unit,
    onSpeakClick: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Ink, RoundedCornerShape(Dimens.RadiusMD))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(Dimens.SpaceLG),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = category.icon,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = Dimens.SpaceSM)
                )
                Text(
                    text = category.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Ink
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Toggle category",
                tint = Ink
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpaceLG, bottom = Dimens.SpaceLG)
            ) {
                category.phrases.forEach { phrase ->
                    HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPhraseClick(phrase) }
                            .padding(vertical = Dimens.SpaceMD),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = phrase.english,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = phrase.chinese,
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = phrase.pinyin,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        IconButton(onClick = { onSpeakClick(phrase.chinese) }) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speak phrase",
                                tint = Ink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
