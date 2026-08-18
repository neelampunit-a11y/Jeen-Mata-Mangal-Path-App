package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sin

object DevotionalAudioHelper {

    /**
     * Plays a resonant divine temple bell sound synthesized in real-time.
     */
    suspend fun playTempleBell() = withContext(Dispatchers.Default) {
        val sampleRate = 44100
        val durationSeconds = 2.2
        val numSamples = (sampleRate * durationSeconds).toInt()
        val buffer = ShortArray(numSamples)

        val fundamentalFreq = 659.25 // E5 bell tone
        val partials = listOf(
            Pair(fundamentalFreq, 0.45),
            Pair(fundamentalFreq * 1.5, 0.25),
            Pair(fundamentalFreq * 2.0, 0.18),
            Pair(fundamentalFreq * 2.76, 0.10),
            Pair(fundamentalFreq * 4.07, 0.05)
        )

        for (i in 0 until numSamples) {
            val time = i.toDouble() / sampleRate
            val decay = kotlin.math.exp(-time * 2.8)
            var sample = 0.0

            for ((freq, amplitude) in partials) {
                sample += amplitude * sin(2.0 * Math.PI * freq * time)
            }

            val finalSample = (sample * decay * Short.MAX_VALUE * 0.85).toInt()
            buffer[i] = finalSample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        playBuffer(buffer, sampleRate)
    }

    /**
     * Plays a resonant deep sacred Shankh sound synthesized in real-time.
     */
    suspend fun playShankhSound() = withContext(Dispatchers.Default) {
        val sampleRate = 44100
        val durationSeconds = 3.0
        val numSamples = (sampleRate * durationSeconds).toInt()
        val buffer = ShortArray(numSamples)

        val shankhFreq = 220.0 // A3 deep resonant tone
        for (i in 0 until numSamples) {
            val time = i.toDouble() / sampleRate
            // Attack, sustain, and release envelope
            val envelope = when {
                time < 0.4 -> time / 0.4 // gentle swell
                time > 2.2 -> (3.0 - time) / 0.8 // smooth fade
                else -> 1.0
            }

            // Rich harmonics with slight pitch modulation
            val pitchMod = 1.0 + 0.015 * sin(2.0 * Math.PI * 4.5 * time)
            val freq = shankhFreq * pitchMod
            val wave = 0.6 * sin(2.0 * Math.PI * freq * time) +
                    0.25 * sin(2.0 * Math.PI * freq * 2.0 * time) +
                    0.15 * sin(2.0 * Math.PI * freq * 3.0 * time)

            val finalSample = (wave * envelope * Short.MAX_VALUE * 0.8).toInt()
            buffer[i] = finalSample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        playBuffer(buffer, sampleRate)
    }

    private fun playBuffer(buffer: ShortArray, sampleRate: Int) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onPeriodicNotification(track: AudioTrack?) {}
                override fun onMarkerReached(t: AudioTrack?) {
                    try {
                        t?.stop()
                        t?.release()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
            track.notificationMarkerPosition = buffer.size
            track.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
