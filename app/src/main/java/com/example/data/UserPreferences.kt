package com.example.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("jeen_mata_prefs", Context.MODE_PRIVATE)

    var lastReadChapter: Int
        get() = prefs.getInt("last_read_chapter", 1)
        set(value) = prefs.edit().putInt("last_read_chapter", value).apply()

    var fontSizeSp: Float
        get() = prefs.getFloat("font_size_sp", 18f)
        set(value) = prefs.edit().putFloat("font_size_sp", value).apply()

    var readingTheme: ReadingTheme
        get() {
            val name = prefs.getString("reading_theme", ReadingTheme.DIVINE_GOLD.name)
            return try {
                ReadingTheme.valueOf(name ?: ReadingTheme.DIVINE_GOLD.name)
            } catch (e: Exception) {
                ReadingTheme.DIVINE_GOLD
            }
        }
        set(value) = prefs.edit().putString("reading_theme", value.name).apply()

    var totalJapCount: Int
        get() = prefs.getInt("total_jap_count", 0)
        set(value) = prefs.edit().putInt("total_jap_count", value).apply()

    var todayJapCount: Int
        get() = prefs.getInt("today_jap_count", 0)
        set(value) = prefs.edit().putInt("today_jap_count", value).apply()

    var lastJapDate: String
        get() = prefs.getString("last_jap_date", "") ?: ""
        set(value) = prefs.edit().putString("last_jap_date", value).apply()

    fun getBookmarkedVerseIds(): Set<Int> {
        val stringSet = prefs.getStringSet("bookmarked_verse_ids", emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun toggleBookmark(verseId: Int): Boolean {
        val current = getBookmarkedVerseIds().toMutableSet()
        val isNowBookmarked = if (current.contains(verseId)) {
            current.remove(verseId)
            false
        } else {
            current.add(verseId)
            true
        }
        prefs.edit().putStringSet("bookmarked_verse_ids", current.map { it.toString() }.toSet()).apply()
        return isNowBookmarked
    }

    fun isBookmarked(verseId: Int): Boolean {
        return getBookmarkedVerseIds().contains(verseId)
    }
}
