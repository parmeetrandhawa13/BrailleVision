package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class AudioAccessibilityManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var toneGenerator: ToneGenerator? = null
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private var onSpeechCompletedCallback: (() -> Unit)? = null
    private var pendingSpeech: Triple<String, Boolean, (() -> Unit)?>? = null

    var onSpeechStarted: (() -> Unit)? = null
    var onSpeechFinished: (() -> Unit)? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 90)
        } catch (e: Exception) {
            // ToneGenerator fallback
        }

        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                var langResult = tts?.setLanguage(Locale.US)
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    langResult = tts?.setLanguage(Locale.getDefault())
                    if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.ENGLISH)
                    }
                }

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                tts?.setAudioAttributes(audioAttributes)

                tts?.setSpeechRate(_speechRate.value)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        onSpeechStarted?.invoke()
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        val callback = onSpeechCompletedCallback
                        onSpeechCompletedCallback = null
                        callback?.invoke()
                        onSpeechFinished?.invoke()
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        val callback = onSpeechCompletedCallback
                        onSpeechCompletedCallback = null
                        callback?.invoke()
                        onSpeechFinished?.invoke()
                    }
                })
                _isTtsReady.value = true

                // Execute any queued speech
                pendingSpeech?.let { (pText, pFlush, pCallback) ->
                    pendingSpeech = null
                    executeSpeak(pText, pFlush, pCallback)
                }
            }
        }
    }

    fun announceWelcome(onComplete: (() -> Unit)? = null) {
        speak("Braille Vision started. Say read to begin.", flush = true, onComplete = onComplete)
    }

    fun speak(text: String, flush: Boolean = true, onComplete: (() -> Unit)? = null) {
        if (text.isBlank()) return
        if (!_isTtsReady.value) {
            // Queue speech until TTS finishes initializing
            pendingSpeech = Triple(text, flush, onComplete)
            return
        }
        executeSpeak(text, flush, onComplete)
    }

    private fun executeSpeak(text: String, flush: Boolean, onComplete: (() -> Unit)?) {
        onSpeechCompletedCallback = onComplete
        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val utteranceId = "utterance_${System.currentTimeMillis()}"
        tts?.speak(text, queueMode, null, utteranceId)
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
        val callback = onSpeechCompletedCallback
        onSpeechCompletedCallback = null
        onSpeechFinished?.invoke()
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate.coerceIn(0.5f, 2.0f)
        tts?.setSpeechRate(_speechRate.value)
    }

    fun cycleSpeechRate(): Float {
        val nextRate = when (_speechRate.value) {
            1.0f -> 1.25f
            1.25f -> 1.5f
            1.5f -> 0.85f
            else -> 1.0f
        }
        setSpeechRate(nextRate)
        speak("Speech rate set to ${nextRate}x")
        return nextRate
    }

    fun playCenteredChime() {
        vibrateSuccessDoublePulse()
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
        } catch (e: Exception) {
            // Ignore tone error
        }
    }

    fun playListeningTone() {
        vibrateShort()
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 100)
        } catch (e: Exception) {
            // Ignore tone error
        }
    }

    fun playScanDoneTone() {
        vibrateSuccessDoublePulse()
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun playGuidanceTick() {
        vibrateLight()
    }

    fun vibrateShort() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun vibrateLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, 80))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun vibrateSuccessDoublePulse() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 80, 100, 120)
                val amplitudes = intArrayOf(0, 200, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 80, 100, 120), -1)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            toneGenerator?.release()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
