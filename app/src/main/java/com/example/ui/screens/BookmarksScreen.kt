package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MangalPathContent
import com.example.data.MangalPathVerse
import com.example.data.UserPreferences
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    userPreferences: UserPreferences,
    onNavigateToVerse: (chapterNumber: Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var bookmarkedIds by remember { mutableStateOf(userPreferences.getBookmarkedVerseIds()) }

    val bookmarkedItems = remember(bookmarkedIds) {
        val list = mutableListOf<Pair<Int, MangalPathVerse>>()
        MangalPathContent.chapters.forEach { ch ->
            ch.verses.forEach { v ->
                if (bookmarkedIds.contains(v.id)) {
                    list.add(ch.chapterNumber to v)
                }
            }
        }
        list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "सहेजी गई चौपाइयां (Bookmarks)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "पीछे जाएं")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DevotionalCrimson,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = DevotionalParchment
    ) { innerPadding ->
        if (bookmarkedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = DevotionalBorder,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "कोई चौपाई सहेजी नहीं गई है",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DevotionalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "पाठ पढ़ते समय किसी भी चौपाई के पास बने बुकमार्क आइकन पर स्पर्श करें।",
                        fontSize = 13.sp,
                        color = DevotionalTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(bookmarkedItems) { (chNum, verse) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToVerse(chNum) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DevotionalParchmentCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = DevotionalCrimson.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "अध्याय $chNum" + if (verse.verseNumber != null) " • चौपाई ${verse.verseNumber}" else "",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DevotionalCrimson,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            val shareText = "॥ माँ जीण शक्ति मंगल पाठ ॥\n" + verse.lines.joinToString("\n")
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, shareText)
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "साझा करें"))
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "साझा करें", tint = DevotionalTextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            userPreferences.toggleBookmark(verse.id)
                                            bookmarkedIds = userPreferences.getBookmarkedVerseIds()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "हटाएं", tint = DevotionalCrimson, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            verse.lines.forEach { line ->
                                Text(
                                    text = line,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DevotionalTextDark,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
