package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.audio.DevotionalTtsManager
import com.example.data.UserPreferences
import com.example.ui.components.JapMalaScreen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

sealed class AppScreen {
    object Home : AppScreen()
    data class Reader(val chapterNumber: Int = 1, val isCompletePathMode: Boolean = false) : AppScreen()
    object JapMala : AppScreen()
    data class AartiChalisa(val initialTab: Int = 0) : AppScreen()
    object History : AppScreen()
    object Vidhi : AppScreen()
    object Bookmarks : AppScreen()
}

class MainActivity : ComponentActivity() {

    private lateinit var userPreferences: UserPreferences
    private lateinit var ttsManager: DevotionalTtsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        userPreferences = UserPreferences(this)
        ttsManager = DevotionalTtsManager(this)

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

                    when (val screen = currentScreen) {
                        is AppScreen.Home -> {
                            HomeScreen(
                                userPreferences = userPreferences,
                                ttsManager = ttsManager,
                                onNavigateToChapter = { chNum ->
                                    currentScreen = AppScreen.Reader(chapterNumber = chNum, isCompletePathMode = false)
                                },
                                onNavigateToCompletePath = {
                                    currentScreen = AppScreen.Reader(chapterNumber = 1, isCompletePathMode = true)
                                },
                                onNavigateToJapMala = {
                                    currentScreen = AppScreen.JapMala
                                },
                                onNavigateToAarti = { tab ->
                                    currentScreen = AppScreen.AartiChalisa(initialTab = tab)
                                },
                                onNavigateToHistory = {
                                    currentScreen = AppScreen.History
                                },
                                onNavigateToVidhi = {
                                    currentScreen = AppScreen.Vidhi
                                },
                                onNavigateToBookmarks = {
                                    currentScreen = AppScreen.Bookmarks
                                }
                            )
                        }

                        is AppScreen.Reader -> {
                            MangalPathReaderScreen(
                                initialChapterNumber = screen.chapterNumber,
                                isCompletePathMode = screen.isCompletePathMode,
                                userPreferences = userPreferences,
                                ttsManager = ttsManager,
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }

                        is AppScreen.JapMala -> {
                            JapMalaScreen(
                                userPreferences = userPreferences,
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }

                        is AppScreen.AartiChalisa -> {
                            AartiChalisaScreen(
                                initialTab = screen.initialTab,
                                userPreferences = userPreferences,
                                ttsManager = ttsManager,
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }

                        is AppScreen.History -> {
                            TempleHistoryScreen(
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }

                        is AppScreen.Vidhi -> {
                            PoojaVidhiScreen(
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }

                        is AppScreen.Bookmarks -> {
                            BookmarksScreen(
                                userPreferences = userPreferences,
                                onNavigateToVerse = { chNum ->
                                    currentScreen = AppScreen.Reader(chapterNumber = chNum, isCompletePathMode = false)
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
