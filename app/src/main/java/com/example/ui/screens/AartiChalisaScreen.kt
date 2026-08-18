package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DevotionalTtsManager
import com.example.data.AdditionalPrayers
import com.example.data.PrayerItem
import com.example.data.UserPreferences
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AartiChalisaScreen(
    initialTab: Int = 0,
    userPreferences: UserPreferences,
    ttsManager: DevotionalTtsManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(initialTab) } // 0: Aarti, 1: Chalisa
    var fontSize by remember { mutableStateOf(userPreferences.fontSizeSp) }
    val isTtsPlaying by ttsManager.isPlaying.collectAsState()

    val currentPrayer: PrayerItem = if (selectedTab == 0) AdditionalPrayers.aarti else AdditionalPrayers.chalisa

    fun startRecitation() {
        val lines = currentPrayer.verses.filter { it.isNotBlank() }
        val ttsItems = lines.mapIndexed { idx, text ->
            idx to text
        }
        ttsManager.startRecitation(ttsItems)
    }

    fun sharePrayer() {
        val fullText = buildString {
            append("॥ ${currentPrayer.title} ॥\n")
            append("${currentPrayer.subtitle}\n\n")
            currentPrayer.verses.forEach { append("$it\n") }
            append("\nजय जय जीण माँ भवानी")
        }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, fullText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "आरती/चालीसा साझा करें"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedTab == 0) "माँ जीण भवानी की आरती" else "श्री जीण माता चालीसा",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                    IconButton(onClick = { sharePrayer() }) {
                        Icon(Icons.Default.Share, contentDescription = "साझा करें", tint = DevotionalGold)
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
            Surface(
                color = DevotionalCrimsonDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            if (fontSize > 14f) {
                                fontSize -= 2f
                                userPreferences.fontSizeSp = fontSize
                            }
                        }) {
                            Text("A-", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text("${fontSize.toInt()}sp", color = DevotionalGoldLight, fontSize = 12.sp)
                        IconButton(onClick = {
                            if (fontSize < 26f) {
                                fontSize += 2f
                                userPreferences.fontSizeSp = fontSize
                            }
                        }) {
                            Text("A+", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            if (isTtsPlaying) ttsManager.pauseOrResume()
                            else startRecitation()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTtsPlaying) DevotionalAmber else DevotionalSaffron,
                            contentColor = if (isTtsPlaying) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isTtsPlaying) Icons.Default.Pause else Icons.Default.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isTtsPlaying) "विराम" else if (selectedTab == 0) "आरती सुनें" else "चालीसा सुनें")
                    }
                }
            }
        },
        containerColor = DevotionalParchment
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DevotionalParchmentCard,
                contentColor = DevotionalCrimson
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        ttsManager.stop()
                    },
                    text = { Text("माँ जीण आरती", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        ttsManager.stop()
                    },
                    text = { Text("श्री जीण चालीसा", fontWeight = FontWeight.Bold) }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = DevotionalParchmentCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "॥ ${currentPrayer.title} ॥",
                                fontSize = (fontSize + 2).sp,
                                fontWeight = FontWeight.Bold,
                                color = DevotionalCrimson,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentPrayer.subtitle,
                                fontSize = (fontSize - 2).sp,
                                color = DevotionalSaffron,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                items(currentPrayer.verses) { line ->
                    if (line.isBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                    } else if (line.startsWith("॥")) {
                        Text(
                            text = line,
                            fontSize = (fontSize + 1).sp,
                            fontWeight = FontWeight.Bold,
                            color = DevotionalSaffron,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        )
                    } else {
                        Text(
                            text = line,
                            fontSize = fontSize.sp,
                            fontWeight = FontWeight.Medium,
                            color = DevotionalTextDark,
                            textAlign = TextAlign.Center,
                            lineHeight = (fontSize * 1.55f).sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(
                        text = "॥ जय जय भंवरा वाली माँ ॥",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DevotionalCrimson,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
