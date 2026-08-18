package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class DevotionalTtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentSpeakingIndex = MutableStateFlow(-1)
    val currentSpeakingIndex: StateFlow<Int> = _currentSpeakingIndex

    private var speechQueue: List<Pair<Int, String>> = emptyList()
    private var queuePointer = 0
    private var speechRate = 0.9f // slightly gentle, calm pace for devotional recitation

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val hindiLocale = Locale("hi", "IN")
            val result = tts?.setLanguage(hindiLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setSpeechRate(speechRate)
            tts?.setPitch(1.0f)
            isInitialized = true

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isPlaying.value = true
                    val idx = utteranceId?.toIntOrNull()
                    if (idx != null) {
                        _currentSpeakingIndex.value = idx
                    }
                }

                override fun onDone(utteranceId: String?) {
                    queuePointer++
                    if (queuePointer < speechQueue.size) {
                        speakNext()
                    } else {
                        _isPlaying.value = false
                        _currentSpeakingIndex.value = -1
                    }
                }

                override fun onError(utteranceId: String?) {
                    _isPlaying.value = false
                    _currentSpeakingIndex.value = -1
                }
            })
        }
    }

    fun startRecitation(items: List<Pair<Int, String>>, startIndex: Int = 0) {
        if (!isInitialized) return
        stop()
        speechQueue = items
        queuePointer = startIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
        if (speechQueue.isNotEmpty()) {
            speakNext()
        }
    }

    private fun speakNext() {
        if (queuePointer < speechQueue.size) {
            val (verseId, text) = speechQueue[queuePointer]
            val params = android.os.Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, verseId.toString())
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, verseId.toString())
        }
    }

    fun pauseOrResume() {
        if (_isPlaying.value) {
            tts?.stop()
            _isPlaying.value = false
        } else if (speechQueue.isNotEmpty() && queuePointer < speechQueue.size) {
            speakNext()
        }
    }

    fun stop() {
        tts?.stop()
        _isPlaying.value = false
        _currentSpeakingIndex.value = -1
    }

    fun setSpeed(rate: Float) {
        speechRate = rate
        tts?.setSpeechRate(rate)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
