package com.example.data

data class MangalPathVerse(
    val id: Int,
    val verseNumber: Int? = null,
    val type: VerseType,
    val title: String? = null,
    val lines: List<String>,
    val explanation: String? = null
)

enum class VerseType {
    SHLOK,
    OPENING_CHAUPAI,
    CHAUPAI,
    DOHA,
    INTERLUDE_CHAUPAI,
    JAIKARA,
    CLOSING_DOHA,
    EPILOGUE
}

data class MangalPathChapter(
    val chapterNumber: Int,
    val title: String,
    val subtitle: String,
    val summary: String,
    val verses: List<MangalPathVerse>
)

enum class ReadingTheme(val displayName: String) {
    DIVINE_GOLD("स्वर्णिम (दिव्य)"),
    SACRED_RED("सिन्दूरी (पारंपरिक)"),
    PARCHMENT("पोथी (प्राचीन पाण्डुलिपि)"),
    NIGHT("रात्रि ध्यान (डार्क)")
}

data class BookmarkItem(
    val chapterNumber: Int,
    val verseId: Int,
    val verseText: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)
