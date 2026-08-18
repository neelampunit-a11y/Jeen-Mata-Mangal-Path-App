package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DevotionalAudioHelper
import com.example.audio.DevotionalTtsManager
import com.example.data.MangalPathChapter
import com.example.data.MangalPathContent
import com.example.data.UserPreferences
import com.example.ui.components.DevotionalHeader
import com.example.ui.components.InteractivePoojaThaliView
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userPreferences: UserPreferences,
    ttsManager: DevotionalTtsManager,
    onNavigateToChapter: (chapterNumber: Int) -> Unit,
    onNavigateToCompletePath: () -> Unit,
    onNavigateToJapMala: () -> Unit,
    onNavigateToAarti: (tab: Int) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToVidhi: () -> Unit,
    onNavigateToBookmarks: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showPoojaThaliDialog by remember { mutableStateOf(false) }

    val lastReadCh = userPreferences.lastReadChapter

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "माँ जीण शक्ति मंगल पाठ",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToBookmarks) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "बुकमार्क्स",
                            tint = DevotionalGold
                        )
                    }
                    IconButton(onClick = { showPoojaThaliDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "दिव्य आरती थाल",
                            tint = DevotionalGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DevotionalCrimson,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = DevotionalParchment
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Devotional Sacred Header with Bell & Shankh
            item {
                DevotionalHeader(
                    onBellClick = {},
                    onShankhClick = {},
                    onFlowerClick = { showPoojaThaliDialog = true }
                )
            }

            // Quick Devotional CTA & Continue Reading Card
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_action_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = DevotionalParchmentCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "॥ मंगल पाठ प्रारंभ ॥",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DevotionalCrimson
                                    )
                                    Text(
                                        text = "अंतिम पढ़ा गया: अध्याय $lastReadCh",
                                        fontSize = 12.sp,
                                        color = DevotionalTextSecondary
                                    )
                                }

                                Button(
                                    onClick = onNavigateToCompletePath,
                                    colors = ButtonDefaults.buttonColors(containerColor = DevotionalCrimson),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("सम्पूर्ण पाठ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Daily Sacred Verse Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DevotionalGoldLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                text = "॥ नित्य मंगल महामंत्र ॥",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DevotionalCrimson
                            )
                            IconButton(
                                onClick = {
                                    val shareText = "॥ माँ जीण शक्ति मंगल पाठ ॥\nजीण जीण भज बारम्बारा, हर संकट का हो निस्तारा ।\nनाम जपे माँ खुश हो जावे, संकट हर लेती है सारा ।।\n\nजय जय जीण माँ भवानी"
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "महामंत्र साझा करें"))
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "साझा करें", tint = DevotionalCrimson, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "जीण जीण भज बारम्बारा, हर संकट का हो निस्तारा ।\nनाम जपे माँ खुश हो जावे, संकट हर लेती है सारा ।।",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DevotionalTextDark,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // 4 Grid Quick Feature Buttons (Jap Mala, Aarti, History, Vidhi)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "भक्ति साधन व आराधना",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DevotionalCrimson,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Shri Jeen Chalisa
                        QuickNavCard(
                            title = "श्री जीण चालीसा",
                            subtitle = "सम्पूर्ण चालीसा पाठ",
                            icon = Icons.Default.MenuBook,
                            iconColor = DevotionalCrimson,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToAarti(1) }
                        )

                        // Aarti
                        QuickNavCard(
                            title = "माँ जीण आरती",
                            subtitle = "ॐ जय जीण माता",
                            icon = Icons.Default.AutoAwesome,
                            iconColor = DevotionalAmber,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToAarti(0) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Jap Mala
                        QuickNavCard(
                            title = "नाम जपमाला",
                            subtitle = "१०८ मनके काउंटर",
                            icon = Icons.Default.Adjust,
                            iconColor = DevotionalSaffron,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToJapMala
                        )

                        // Mandir Itihas
                        QuickNavCard(
                            title = "काजल शिखर धाम",
                            subtitle = "इतिहास व महिमा",
                            icon = Icons.Default.TempleHindu,
                            iconColor = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToHistory
                        )
                    }
                }
            }

            // All 9 Chapters Header & List
            item {
                PaddingValues(horizontal = 16.dp, vertical = 8.dp).let {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "मंगल पाठ के ९ अध्याय (Ch. 1-9)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DevotionalCrimson
                            )
                            Text(
                                text = "कुल १७५ चौपाई",
                                fontSize = 12.sp,
                                color = DevotionalTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            itemsIndexed(MangalPathContent.chapters) { idx, chapter ->
                ChapterRowCard(
                    chapter = chapter,
                    isLastRead = chapter.chapterNumber == lastReadCh,
                    onClick = { onNavigateToChapter(chapter.chapterNumber) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Interactive Pooja Thali Modal Bottom Sheet
        if (showPoojaThaliDialog) {
            ModalBottomSheet(
                onDismissRequest = { showPoojaThaliDialog = false },
                containerColor = DevotionalNightBg
            ) {
                InteractivePoojaThaliView(
                    onClose = { showPoojaThaliDialog = false }
                )
            }
        }
    }
}

@Composable
fun QuickNavCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DevotionalParchmentCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DevotionalTextDark
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = DevotionalTextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ChapterRowCard(
    chapter: MangalPathChapter,
    isLastRead: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick)
            .testTag("chapter_row_${chapter.chapterNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLastRead) DevotionalGoldLight else DevotionalParchmentCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Chapter Number Circle Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(DevotionalCrimson, DevotionalSaffron)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${chapter.chapterNumber}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = chapter.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DevotionalTextDark
                        )
                        if (isLastRead) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = DevotionalCrimson.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "चालू",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DevotionalCrimson,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = chapter.subtitle,
                        fontSize = 12.sp,
                        color = DevotionalTextSecondary,
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "पढ़ें",
                tint = DevotionalSaffron
            )
        }
    }
}
