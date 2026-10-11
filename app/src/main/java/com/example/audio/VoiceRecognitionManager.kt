package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceRecognitionManager(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _lastRecognizedSpeech = MutableStateFlow<String?>(null)
    val lastRecognizedSpeech: StateFlow<String?> = _lastRecognizedSpeech.asStateFlow()

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    var onVoiceCommandReceived: ((String, List<String>) -> Unit)? = null
    var onListeningStarted: (() -> Unit)? = null

    var isContinuousListening: Boolean = true
    private var isMutedByTts: Boolean = false
    private var isDestroyed: Boolean = false

    private val restartRunnable = Runnable {
        if (isContinuousListening && !isMutedByTts && !isDestroyed) {
            startListeningInternal()
        }
    }

    init {
        mainHandler.post {
            initRecognizer()
        }
    }

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun initRecognizer() {
        if (isDestroyed) return
        try {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                return
            }
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
            } catch (e: Exception) {}
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        onListeningStarted?.invoke()
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _rmsDb.value = rmsdB
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        // If permissions missing, stop to prevent spinning
                        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                            isContinuousListening = false
                            return
                        }
                        // If recognizer is busy or client error, recreate it cleanly
                        if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || 
                            error == SpeechRecognizer.ERROR_CLIENT ||
                            error == SpeechRecognizer.ERROR_SERVER_DISCONNECTED) {
                            scheduleReinitAndRestart(800L)
                            return
                        }
                        // Continuous listening: when speech times out or no speech detected, resume cleanly
                        if (isContinuousListening && !isMutedByTts && !isDestroyed) {
                            scheduleRestart(750L)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: arrayListOf()
                        val topMatch = matches.firstOrNull()?.trim()
                        if (!topMatch.isNullOrBlank()) {
                            _lastRecognizedSpeech.value = topMatch
                            onVoiceCommandReceived?.invoke(topMatch, matches)
                            // If command didn't trigger TTS to mute us, continue listening loop after a pause
                            mainHandler.postDelayed({
                                if (isContinuousListening && !isMutedByTts && !_isListening.value) {
                                    startListeningInternal()
                                }
                            }, 1200L)
                        } else {
                            if (isContinuousListening && !isMutedByTts) {
                                scheduleRestart(600L)
                            }
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull()
                        if (!partial.isNullOrBlank()) {
                            _lastRecognizedSpeech.value = partial
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } catch (e: Exception) {
            // SpeechRecognizer initialization catch
        }
    }

    fun startListening(): Boolean {
        isContinuousListening = true
        isMutedByTts = false
        mainHandler.removeCallbacks(restartRunnable)
        return startListeningInternal()
    }

    private fun startListeningInternal(): Boolean {
        if (isDestroyed || isMutedByTts) return false
        return try {
            if (speechRecognizer == null) {
                initRecognizer()
            }
            if (speechRecognizer == null) {
                _isListening.value = false
                return false
            }
            try {
                speechRecognizer?.cancel()
            } catch (e: Exception) {}

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                // Prevent cutting off speech too early:
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1800L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500L)
            }
            speechRecognizer?.startListening(intent)
            _isListening.value = true
            true
        } catch (e: Exception) {
            _isListening.value = false
            if (isContinuousListening && !isMutedByTts) {
                scheduleReinitAndRestart(700L)
            }
            false
        }
    }

    fun pauseListeningForTts() {
        isMutedByTts = true
        mainHandler.removeCallbacks(restartRunnable)
        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {}
        _isListening.value = false
    }

    fun resumeListeningAfterTts() {
        isMutedByTts = false
        if (isContinuousListening && !isDestroyed) {
            scheduleRestart(400L)
        }
    }

    private fun scheduleRestart(delayMs: Long) {
        mainHandler.removeCallbacks(restartRunnable)
        mainHandler.postDelayed(restartRunnable, delayMs)
    }

    private fun scheduleReinitAndRestart(delayMs: Long) {
        mainHandler.removeCallbacks(restartRunnable)
        mainHandler.postDelayed({
            if (!isDestroyed && isContinuousListening && !isMutedByTts) {
                initRecognizer()
                startListeningInternal()
            }
        }, delayMs)
    }

    fun stopListening() {
        isContinuousListening = false
        mainHandler.removeCallbacks(restartRunnable)
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {}
        _isListening.value = false
    }

    fun setListeningState(listening: Boolean) {
        _isListening.value = listening
    }

    fun simulateVoiceInput(command: String) {
        _lastRecognizedSpeech.value = command
        onVoiceCommandReceived?.invoke(command, listOf(command))
        _isListening.value = false
    }

    fun cancel() {
        mainHandler.removeCallbacks(restartRunnable)
        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {}
        _isListening.value = false
    }

    fun release() {
        isDestroyed = true
        isContinuousListening = false
        mainHandler.removeCallbacks(restartRunnable)
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {}
        speechRecognizer = null
    }
}
