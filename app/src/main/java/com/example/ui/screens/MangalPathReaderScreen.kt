package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DevotionalTtsManager
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ReaderPalette(
    val bg: Color,
    val surface: Color,
    val text: Color,
    val textSecondary: Color,
    val accent: Color,
    val verseHighlight: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangalPathReaderScreen(
    initialChapterNumber: Int = 1,
    isCompletePathMode: Boolean = false,
    userPreferences: UserPreferences,
    ttsManager: DevotionalTtsManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var currentChapterIndex by remember { mutableStateOf((initialChapterNumber - 1).coerceIn(0, 8)) }
    var completePathActive by remember { mutableStateOf(isCompletePathMode) }
    var fontSize by remember { mutableStateOf(userPreferences.fontSizeSp) }
    var currentTheme by remember { mutableStateOf(userPreferences.readingTheme) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showChapterPickerSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Auto-scroll state
    var isAutoScrolling by remember { mutableStateOf(false) }
    var autoScrollSpeedMs by remember { mutableStateOf(3500L) } // interval between scrolls

    // TTS state
    val isTtsPlaying by ttsManager.isPlaying.collectAsState()
    val speakingVerseId by ttsManager.currentSpeakingIndex.collectAsState()

    var bookmarkedVerses by remember { mutableStateOf(userPreferences.getBookmarkedVerseIds()) }

    // Save last read chapter
    LaunchedEffect(currentChapterIndex) {
        userPreferences.lastReadChapter = currentChapterIndex + 1
    }

    // Auto-scroll effect
    LaunchedEffect(isAutoScrolling, autoScrollSpeedMs) {
        while (isAutoScrolling) {
            delay(autoScrollSpeedMs)
            val currentFirst = listState.firstVisibleItemIndex
            listState.animateScrollToItem((currentFirst + 1).coerceAtMost(300))
        }
    }

    // Theming Colors based on selected reading theme
    val palette = when (currentTheme) {
        ReadingTheme.DIVINE_GOLD -> ReaderPalette(
            bg = DevotionalParchment,
            surface = DevotionalParchmentCard,
            text = DevotionalTextDark,
            textSecondary = DevotionalTextSecondary,
            accent = DevotionalCrimson,
            verseHighlight = DevotionalGoldLight
        )
        ReadingTheme.SACRED_RED -> ReaderPalette(
            bg = Color(0xFFFFF7F5),
            surface = Color(0xFFFFEDE8),
            text = Color(0xFF3E1B1B),
            textSecondary = Color(0xFF7A3838),
            accent = DevotionalCrimson,
            verseHighlight = Color(0xFFFFDCD6)
        )
        ReadingTheme.PARCHMENT -> ReaderPalette(
            bg = Color(0xFFFBF6EB),
            surface = Color(0xFFF3EAD7),
            text = Color(0xFF2C221E),
            textSecondary = Color(0xFF5D4A41),
            accent = Color(0xFF8D4004),
            verseHighlight = Color(0xFFE8DBBF)
        )
        ReadingTheme.NIGHT -> ReaderPalette(
            bg = DevotionalNightBg,
            surface = DevotionalNightCard,
            text = DevotionalNightText,
            textSecondary = DevotionalNightSecondaryText,
            accent = DevotionalGold,
            verseHighlight = DevotionalNightSurface
        )
    }
    val bgColor = palette.bg
    val surfaceColor = palette.surface
    val textColor = palette.text
    val textSecondaryColor = palette.textSecondary
    val accentColor = palette.accent
    val verseHighlightColor = palette.verseHighlight

    val currentChapter = MangalPathContent.chapters[currentChapterIndex]

    // Flatten verses if in Complete Path Mode
    val displayedVersesWithChapter = remember(completePathActive, currentChapterIndex, searchQuery) {
        if (searchQuery.isNotBlank()) {
            // Search filter across all chapters
            val results = mutableListOf<Pair<Int, MangalPathVerse>>()
            MangalPathContent.chapters.forEach { ch ->
                ch.verses.forEach { v ->
                    if (v.lines.any { it.contains(searchQuery, ignoreCase = true) } ||
                        (v.title?.contains(searchQuery, ignoreCase = true) == true)
                    ) {
                        results.add(ch.chapterNumber to v)
                    }
                }
            }
            results
        } else if (completePathActive) {
            val list = mutableListOf<Pair<Int, MangalPathVerse>>()
            MangalPathContent.chapters.forEach { ch ->
                ch.verses.forEach { v ->
                    list.add(ch.chapterNumber to v)
                }
            }
            list
        } else {
            currentChapter.verses.map { currentChapter.chapterNumber to it }
        }
    }

    fun startTtsForCurrentScreen() {
        val ttsItems = displayedVersesWithChapter.map { (_, verse) ->
            val text = buildString {
                if (verse.title != null) append("${verse.title}. ")
                verse.lines.forEach { append("$it ") }
            }
            verse.id to text
        }
        ttsManager.startRecitation(ttsItems)
    }

    fun shareVerse(verse: MangalPathVerse) {
        val shareText = buildString {
            append("॥ माँ जीण शक्ति मंगल पाठ ॥\n\n")
            if (verse.title != null) append("【 ${verse.title} 】\n")
            verse.lines.forEach { append("$it\n") }
            append("\nजय जय जीण माँ • जय जय भंवरा वाली माँ")
        }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "पाठ की पंक्तियां साझा करें"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (completePathActive) "सम्पूर्ण मंगल पाठ" else currentChapter.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (completePathActive) "अध्याय १ से ९ (अविराम)" else currentChapter.subtitle,
                            fontSize = 11.sp,
                            color = DevotionalGoldLight.copy(alpha = 0.85f),
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        ttsManager.stop()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "पीछे जाएं")
                    }
                },
                actions = {
                    // Search toggle
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "खोजें",
                            tint = DevotionalGold
                        )
                    }
                    // Chapter selector
                    IconButton(onClick = { showChapterPickerSheet = true }) {
                        Icon(Icons.Default.MenuBook, contentDescription = "अध्याय चुनें", tint = DevotionalGold)
                    }
                    // Settings (Font, Theme, Auto-scroll)
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "पाठ सेटिंग्स", tint = DevotionalGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DevotionalCrimson,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // Floating Audio & Recitation Control Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reader_bottom_bar"),
                color = DevotionalCrimsonDark,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Auto-Scroll Toggle Button
                    FilterChip(
                        selected = isAutoScrolling,
                        onClick = { isAutoScrolling = !isAutoScrolling },
                        label = {
                            Text(
                                text = if (isAutoScrolling) "स्क्रॉल जारी" else "ऑटो स्क्रॉल",
                                fontSize = 11.sp,
                                color = if (isAutoScrolling) DevotionalCrimson else Color.White
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isAutoScrolling) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isAutoScrolling) DevotionalCrimson else DevotionalGold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DevotionalGold,
                            containerColor = Color.White.copy(alpha = 0.15f)
                        )
                    )

                    // Continuous / Single Chapter Toggle
                    FilterChip(
                        selected = completePathActive,
                        onClick = {
                            completePathActive = !completePathActive
                            ttsManager.stop()
                        },
                        label = {
                            Text(
                                text = if (completePathActive) "सम्पूर्ण पाठ" else "अध्याय वार",
                                fontSize = 11.sp,
                                color = if (completePathActive) DevotionalCrimson else Color.White
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DevotionalGold,
                            containerColor = Color.White.copy(alpha = 0.15f)
                        )
                    )

                    // Audio Recitation TTS Button
                    Button(
                        onClick = {
                            if (isTtsPlaying) {
                                ttsManager.pauseOrResume()
                            } else {
                                startTtsForCurrentScreen()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTtsPlaying) DevotionalAmber else DevotionalSaffron,
                            contentColor = if (isTtsPlaying) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isTtsPlaying) Icons.Default.Pause else Icons.Default.VolumeUp,
                            contentDescription = "सुनें",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isTtsPlaying) "विराम" else "पाठ सुनें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = bgColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar (if active)
            AnimatedVisibility(visible = isSearchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("चौपाई या शब्द खोजें (उदा. भंवरावाली, औरंगजेब)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "साफ करें")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Chapter navigation tabs (when not searching and in single chapter mode)
            if (!completePathActive && searchQuery.isEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = currentChapterIndex,
                    containerColor = surfaceColor,
                    contentColor = accentColor,
                    edgePadding = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MangalPathContent.chapters.forEachIndexed { idx, ch ->
                        Tab(
                            selected = currentChapterIndex == idx,
                            onClick = {
                                currentChapterIndex = idx
                                ttsManager.stop()
                            },
                            text = {
                                Text(
                                    text = "अध्याय ${idx + 1}",
                                    fontWeight = if (currentChapterIndex == idx) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }

            // Verses List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Opening Shlok if on Chapter 1
                if ((currentChapterIndex == 0 || completePathActive) && searchQuery.isEmpty()) {
                    item {
                        OpeningShlokCard(
                            shlok = MangalPathContent.openingShlok,
                            fontSize = fontSize,
                            textColor = textColor,
                            surfaceColor = surfaceColor,
                            accentColor = accentColor,
                            onShare = { shareVerse(MangalPathContent.openingShlok) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Chapter Header Card (if single chapter mode)
                if (!completePathActive && searchQuery.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "॥ ${currentChapter.title} ॥",
                                    fontSize = (fontSize + 3).sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accentColor,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentChapter.summary,
                                    fontSize = (fontSize - 4).coerceAtLeast(12f).sp,
                                    color = textSecondaryColor,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Verses
                itemsIndexed(displayedVersesWithChapter) { index, (chNum, verse) ->
                    val isSpeaking = speakingVerseId == verse.id
                    val isBookmarked = bookmarkedVerses.contains(verse.id)

                    VerseItemCard(
                        verse = verse,
                        chapterNumber = chNum,
                        showChapterTag = completePathActive || searchQuery.isNotEmpty(),
                        fontSize = fontSize,
                        textColor = textColor,
                        textSecondaryColor = textSecondaryColor,
                        surfaceColor = if (isSpeaking) verseHighlightColor else surfaceColor,
                        accentColor = accentColor,
                        isBookmarked = isBookmarked,
                        isSpeaking = isSpeaking,
                        onBookmarkToggle = {
                            val nowBookmarked = userPreferences.toggleBookmark(verse.id)
                            bookmarkedVerses = userPreferences.getBookmarkedVerseIds()
                        },
                        onShare = { shareVerse(verse) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Bottom Prev/Next Chapter Navigation (in single chapter mode)
                if (!completePathActive && searchQuery.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (currentChapterIndex > 0) {
                                OutlinedButton(
                                    onClick = {
                                        currentChapterIndex--
                                        ttsManager.stop()
                                        coroutineScope.launch { listState.scrollToItem(0) }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("अध्याय ${currentChapterIndex}")
                                }
                            } else {
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            if (currentChapterIndex < 8) {
                                Button(
                                    onClick = {
                                        currentChapterIndex++
                                        ttsManager.stop()
                                        coroutineScope.launch { listState.scrollToItem(0) }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DevotionalCrimson),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("अध्याय ${currentChapterIndex + 2}")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }

    // Settings Bottom Sheet (Font size, Theme, Speed)
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            containerColor = surfaceColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "पाठ पठन सेटिंग्स",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Font Size Slider
                Text(
                    text = "अक्षर आकार (Font Size): ${fontSize.toInt()} sp",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Slider(
                    value = fontSize,
                    onValueChange = {
                        fontSize = it
                        userPreferences.fontSizeSp = it
                    },
                    valueRange = 14f..26f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = DevotionalCrimson,
                        activeTrackColor = DevotionalSaffron
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Reading Theme Selector
                Text(
                    text = "पठन पृष्ठ थीम",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReadingTheme.values().forEach { theme ->
                        val isSelected = currentTheme == theme
                        OutlinedButton(
                            onClick = {
                                currentTheme = theme
                                userPreferences.readingTheme = theme
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) DevotionalCrimson.copy(alpha = 0.15f) else Color.Transparent,
                                contentColor = if (isSelected) DevotionalCrimson else textColor
                            ),
                            border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(DevotionalCrimson, DevotionalSaffron))) else null,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = theme.displayName.substringBefore(" "),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto Scroll Speed
                Text(
                    text = "ऑटो स्क्रॉल गति",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "धीमी" to 5000L,
                        "मध्यम" to 3500L,
                        "तेज़" to 2000L
                    ).forEach { (label, speed) ->
                        val isSel = autoScrollSpeedMs == speed
                        FilterChip(
                            selected = isSel,
                            onClick = { autoScrollSpeedMs = speed },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Chapter Picker Bottom Sheet
    if (showChapterPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showChapterPickerSheet = false },
            containerColor = surfaceColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "अध्याय चुनें",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(MangalPathContent.chapters) { idx, ch ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentChapterIndex = idx
                                    completePathActive = false
                                    ttsManager.stop()
                                    showChapterPickerSheet = false
                                    coroutineScope.launch { listState.scrollToItem(0) }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (currentChapterIndex == idx && !completePathActive)
                                    DevotionalCrimson.copy(alpha = 0.12f)
                                else
                                    surfaceColor
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ch.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                    Text(
                                        text = ch.subtitle,
                                        fontSize = 12.sp,
                                        color = textSecondaryColor,
                                        maxLines = 1
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = accentColor
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun OpeningShlokCard(
    shlok: MangalPathVerse,
    fontSize: Float,
    textColor: Color,
    surfaceColor: Color,
    accentColor: Color,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("opening_shlok_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "॥ श्री गणेशाय नमः • श्री शारदायै नमः ॥",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DevotionalSaffron
                )
                IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "साझा करें",
                        tint = DevotionalSaffron,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "॥ श्लोक ॥",
                fontSize = (fontSize + 1).sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(8.dp))

            shlok.lines.forEach { line ->
                Text(
                    text = line,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = textColor,
                    lineHeight = (fontSize * 1.55f).sp
                )
            }
        }
    }
}

@Composable
fun VerseItemCard(
    verse: MangalPathVerse,
    chapterNumber: Int,
    showChapterTag: Boolean,
    fontSize: Float,
    textColor: Color,
    textSecondaryColor: Color,
    surfaceColor: Color,
    accentColor: Color,
    isBookmarked: Boolean,
    isSpeaking: Boolean,
    onBookmarkToggle: () -> Unit,
    onShare: () -> Unit
) {
    val isDohaOrJaikara = verse.type == VerseType.DOHA ||
            verse.type == VerseType.CLOSING_DOHA ||
            verse.type == VerseType.JAIKARA ||
            verse.type == VerseType.EPILOGUE

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("verse_card_${verse.id}")
            .then(
                if (isSpeaking) Modifier.border(1.5.dp, DevotionalGold, RoundedCornerShape(16.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDohaOrJaikara) 2.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Header strip (Verse type / Chapter tag / actions)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showChapterTag) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DevotionalCrimson.copy(alpha = 0.1f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "अध्याय $chapterNumber",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DevotionalCrimson,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (verse.title != null) {
                        Text(
                            text = "॥ ${verse.title} ॥",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DevotionalSaffron
                        )
                    } else if (verse.verseNumber != null) {
                        Text(
                            text = "चौपाई ${verse.verseNumber}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondaryColor
                        )
                    }
                }

                Row {
                    IconButton(onClick = onBookmarkToggle, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "बुकमार्क",
                            tint = if (isBookmarked) DevotionalCrimson else textSecondaryColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "साझा करें",
                            tint = textSecondaryColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Verse Lines
            when (verse.type) {
                VerseType.JAIKARA -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        verse.lines.forEach { line ->
                            Text(
                                text = line,
                                fontSize = (fontSize + 1).sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DevotionalCrimson,
                                textAlign = TextAlign.Center,
                                lineHeight = (fontSize * 1.5f).sp
                            )
                        }
                    }
                }
                VerseType.EPILOGUE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        verse.lines.forEach { line ->
                            Text(
                                text = line,
                                fontSize = (fontSize + 2).sp,
                                fontWeight = FontWeight.Bold,
                                color = DevotionalSaffron,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                VerseType.DOHA, VerseType.CLOSING_DOHA -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        verse.lines.forEach { line ->
                            Text(
                                text = line,
                                fontSize = (fontSize + 0.5f).sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                textAlign = TextAlign.Center,
                                lineHeight = (fontSize * 1.55f).sp
                            )
                        }
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        verse.lines.forEach { line ->
                            Text(
                                text = line,
                                fontSize = fontSize.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor,
                                lineHeight = (fontSize * 1.52f).sp
                            )
                        }
                    }
                }
            }
        }
    }
}
