package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiBrailleService
import com.example.audio.AudioAccessibilityManager
import com.example.audio.VoiceRecognitionManager
import com.example.data.AppScanState
import com.example.data.BrailleDocument
import com.example.data.CameraGuidance
import com.example.data.ChatMessage
import com.example.data.GuidanceDirection
import com.example.data.ReadingPlaybackMode
import com.example.data.SampleBrailleLibrary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class BrailleVisionViewModel(application: Application) : AndroidViewModel(application) {

    val audioManager = AudioAccessibilityManager(application)
    val voiceManager = VoiceRecognitionManager(application)
    private val geminiService = GeminiBrailleService(application)

    private val _scanState = MutableStateFlow(AppScanState.READY)
    val scanState: StateFlow<AppScanState> = _scanState.asStateFlow()

    private val _guidance = MutableStateFlow(CameraGuidance())
    val guidance: StateFlow<CameraGuidance> = _guidance.asStateFlow()

    private val _currentDocument = MutableStateFlow<BrailleDocument?>(null)
    val currentDocument: StateFlow<BrailleDocument?> = _currentDocument.asStateFlow()

    private val _playbackMode = MutableStateFlow(ReadingPlaybackMode.SUMMARY_ONLY)
    val playbackMode: StateFlow<ReadingPlaybackMode> = _playbackMode.asStateFlow()

    private val _isReadingAudio = MutableStateFlow(false)
    val isReadingAudio: StateFlow<Boolean> = _isReadingAudio.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isProcessingQuestion = MutableStateFlow(false)
    val isProcessingQuestion: StateFlow<Boolean> = _isProcessingQuestion.asStateFlow()

    private val _isCameraActive = MutableStateFlow(true)
    val isCameraActive: StateFlow<Boolean> = _isCameraActive.asStateFlow()

    private val _statusAnnouncement = MutableStateFlow("Braille Vision Ready")
    val statusAnnouncement: StateFlow<String> = _statusAnnouncement.asStateFlow()

    private var lastGuidanceSpokenTime = 0L
    private var lastSpokenDirection: GuidanceDirection? = null
    private var voiceListeningJob: Job? = null
    private var isAppStartedAnnounced = false

    init {
        // Collect TTS speaking state
        viewModelScope.launch {
            audioManager.isSpeaking.collect { speaking ->
                _isReadingAudio.value = speaking
            }
        }

        // Continuous hands-free audio loop:
        // Automatically mute the microphone while the app is speaking to prevent feedback,
        // and immediately resume microphone listening the instant the app finishes speaking!
        audioManager.onSpeechStarted = {
            voiceManager.pauseListeningForTts()
        }
        audioManager.onSpeechFinished = {
            voiceManager.resumeListeningAfterTts()
            audioManager.playListeningTone()
        }

        // Handle voice commands from speech recognizer with candidate phrases
        voiceManager.onVoiceCommandReceived = { recognizedText, candidates ->
            handleVoiceCommand(recognizedText, candidates)
        }

        // Initial launch greeting triggered once TTS is ready
        viewModelScope.launch {
            audioManager.isTtsReady.collect { isReady ->
                if (isReady && !isAppStartedAnnounced) {
                    isAppStartedAnnounced = true
                    _statusAnnouncement.value = "Braille Vision started. Always-on voice listening active."
                    audioManager.speak(
                        "Braille Vision started. Always on voice assistant is active. Say read to begin scanning, or ask any question.",
                        flush = true
                    ) {
                        startListeningForCommands()
                    }
                }
            }
        }
    }

    fun triggerStartupGreetingIfNeeded() {
        if (!isAppStartedAnnounced && audioManager.isTtsReady.value) {
            isAppStartedAnnounced = true
            _statusAnnouncement.value = "Braille Vision started. Always-on voice listening active."
            audioManager.speak(
                "Braille Vision started. Always on voice assistant is active. Say read to begin scanning, or ask any question.",
                flush = true
            ) {
                startListeningForCommands()
            }
        }
    }

    fun startListeningForCommands() {
        audioManager.playListeningTone()
        val started = voiceManager.startListening()
        if (started) {
            _statusAnnouncement.value = "Listening continuously... Say 'read' or ask a question."
        } else {
            _statusAnnouncement.value = "Voice active. Say 'read' or ask any question."
        }
    }

    fun stopListening() {
        voiceManager.stopListening()
    }

    fun handleVoiceCommand(rawText: String, candidateMatches: List<String> = emptyList()) {
        val allPhrases = (listOf(rawText) + candidateMatches).map { it.lowercase().trim() }.distinct()
        _statusAnnouncement.value = "Heard: \"$rawText\""

        val isScanCommand = allPhrases.any { phrase ->
            phrase.contains("read") || phrase.contains("red") || phrase.contains("reed") ||
            phrase.contains("scan") || phrase.contains("skan") || phrase.contains("start") ||
            phrase.contains("begin") || phrase.contains("capture") || phrase.contains("camera") ||
            phrase.contains("photo") || phrase.contains("picture") || phrase.contains("snap")
        }

        val isSummaryCommand = allPhrases.any { phrase ->
            phrase.contains("summary") || phrase.contains("summarize") || phrase.contains("summarise") ||
            phrase.contains("short") || phrase.contains("brief") || phrase.contains("overview") ||
            phrase.contains("headphone")
        }

        val isFullReadingCommand = allPhrases.any { phrase ->
            phrase.contains("full") || phrase.contains("complete") || phrase.contains("everything") ||
            phrase.contains("entire") || phrase.contains("all of it") || phrase.contains("whole") ||
            phrase.contains("read all") || phrase.contains("full text")
        }

        val isStopCommand = allPhrases.any { phrase ->
            phrase.contains("stop") || phrase.contains("pause") || phrase.contains("wait") ||
            phrase.contains("quiet") || phrase.contains("silence") || phrase.contains("halt")
        }

        val isPlayCommand = allPhrases.any { phrase ->
            phrase.contains("resume") || phrase.contains("play") || phrase.contains("continue") ||
            phrase.contains("again") || phrase.contains("repeat") || phrase.contains("replay")
        }

        val isSpeedCommand = allPhrases.any { phrase ->
            phrase.contains("speed") || phrase.contains("faster") || phrase.contains("slower") || phrase.contains("rate")
        }

        val isSampleCommand = allPhrases.any { phrase ->
            phrase.contains("sample") || phrase.contains("demo") || phrase.contains("example")
        }

        val isHelpCommand = allPhrases.any { phrase ->
            phrase.contains("help") || phrase.contains("command") || phrase.contains("what can i say") ||
            phrase.contains("option")
        }

        val isDismissCommand = allPhrases.any { phrase ->
            phrase == "no" || phrase.startsWith("no ") || phrase.contains("nothing") ||
            phrase.contains("that's all") || phrase.contains("that is all") || phrase.contains("bye") ||
            phrase.contains("cancel")
        }

        when {
            isScanCommand -> {
                audioManager.speak("Beginning Braille scan. Position camera over the book, or hold still.", flush = true) {
                    startCameraGuidance()
                }
            }
            isSampleCommand -> {
                val sampleDoc = SampleBrailleLibrary.sampleBooks.random()
                loadSampleBrailleBook(sampleDoc)
            }
            isStopCommand -> {
                audioManager.stopSpeaking()
                _statusAnnouncement.value = "Audio paused."
                audioManager.speak("Audio paused. Say resume to continue, or ask a question.", flush = true) {
                    startListeningForCommands()
                }
            }
            isPlayCommand -> {
                playCurrentDocumentAudio()
            }
            isSummaryCommand -> {
                setPlaybackMode(ReadingPlaybackMode.SUMMARY_ONLY)
            }
            isFullReadingCommand -> {
                setPlaybackMode(ReadingPlaybackMode.FULL_DOCUMENT)
            }
            isSpeedCommand -> {
                val nextRate = audioManager.cycleSpeechRate()
                audioManager.speak("Speech rate set to ${nextRate}x. What would you like to do next?", flush = true) {
                    startListeningForCommands()
                }
            }
            isDismissCommand -> {
                audioManager.speak("Understood. I am listening whenever you are ready. Say read to scan another book, or ask a question.", flush = true) {
                    startListeningForCommands()
                }
            }
            isHelpCommand -> {
                audioManager.speak("You can say read to scan, summarize, full reading, pause, resume, change speed, or speak any question about the book. What would you like to do?", flush = true) {
                    startListeningForCommands()
                }
            }
            // If we have a scanned document, answer question directly
            _currentDocument.value != null -> {
                askQuestion(rawText)
            }
            else -> {
                audioManager.speak("I heard $rawText. Say read to scan a Braille book, or say sample book.", flush = true) {
                    startListeningForCommands()
                }
            }
        }
    }

    fun startCameraGuidance() {
        _scanState.value = AppScanState.GUIDING
        _isCameraActive.value = true
        _statusAnnouncement.value = "Camera guidance active. Aligning Braille book..."
        lastGuidanceSpokenTime = 0L
        lastSpokenDirection = null
    }

    fun onCameraGuidanceFrame(newGuidance: CameraGuidance, currentBitmapProvider: (() -> Bitmap?)? = null) {
        if (_scanState.value != AppScanState.GUIDING) return

        _guidance.value = newGuidance

        if (newGuidance.isCentered) {
            // Centered alert triggered!
            _scanState.value = AppScanState.CENTERED_ALERT
            _statusAnnouncement.value = "Document is perfectly centered!"
            audioManager.playCenteredChime()
            audioManager.speak("Document is perfectly centered. Reading now.")

            // Automatically process frame
            viewModelScope.launch {
                delay(600)
                val bitmap = currentBitmapProvider?.invoke()
                processBrailleImage(bitmap)
            }
            return
        }

        // Speak guidance instructions throttled (every 2.5s) to assist visually impaired user
        val now = System.currentTimeMillis()
        if (now - lastGuidanceSpokenTime > 2500L && newGuidance.direction != lastSpokenDirection) {
            lastGuidanceSpokenTime = now
            lastSpokenDirection = newGuidance.direction
            audioManager.playGuidanceTick()
            audioManager.speak(newGuidance.direction.audioAnnouncement)
            _statusAnnouncement.value = newGuidance.direction.instruction
        }
    }

    fun triggerManualCapture(bitmap: Bitmap?) {
        _scanState.value = AppScanState.CENTERED_ALERT
        _statusAnnouncement.value = "Document captured. Processing Braille..."
        audioManager.playCenteredChime()
        audioManager.speak("Braille document captured. Decoding tactile dots.")
        processBrailleImage(bitmap)
    }

    fun loadSampleBrailleBook(book: BrailleDocument) {
        _currentDocument.value = book
        _scanState.value = AppScanState.READING_OUTPUT
        _statusAnnouncement.value = "Loaded: ${book.title}"
        audioManager.playScanDoneTone()
        playCurrentDocumentAudio()
    }

    private fun processBrailleImage(bitmap: Bitmap?) {
        _scanState.value = AppScanState.PROCESSING_SCAN
        _statusAnnouncement.value = "Translating Braille to clear speech..."

        viewModelScope.launch {
            val result = geminiService.processBrailleImage(bitmap)

            result.onSuccess { doc ->
                _currentDocument.value = doc
                _scanState.value = AppScanState.READING_OUTPUT
                _playbackMode.value = ReadingPlaybackMode.SUMMARY_ONLY
                _statusAnnouncement.value = "Scan Complete: ${doc.title}"
                audioManager.playScanDoneTone()

                // Add welcoming message to chat
                _chatMessages.value = listOf(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        isUser = false,
                        text = "I've scanned '${doc.title}'.\n\nSummary:\n${doc.summary}\n\nYou can ask any question using your voice."
                    )
                )

                // Instantly speak the rich voice summary for visually impaired reader
                playCurrentDocumentAudio()
            }.onFailure {
                _scanState.value = AppScanState.ERROR
                _statusAnnouncement.value = "Could not decode Braille. Please try again."
                audioManager.speak("Could not clearly read the Braille dots. Please reposition and say read again.")
            }
        }
    }

    fun setPlaybackMode(mode: ReadingPlaybackMode) {
        _playbackMode.value = mode
        playCurrentDocumentAudio()
    }

    fun togglePlayPause() {
        if (_isReadingAudio.value) {
            audioManager.stopSpeaking()
            _statusAnnouncement.value = "Reading paused"
        } else {
            playCurrentDocumentAudio()
        }
    }

    fun playCurrentDocumentAudio() {
        val doc = _currentDocument.value ?: return
        val textToSpeak = when (_playbackMode.value) {
            ReadingPlaybackMode.SUMMARY_ONLY -> {
                "Summary of ${doc.title}. ${doc.summary}. Say 'full reading' to hear everything, or ask any question."
            }
            ReadingPlaybackMode.FULL_DOCUMENT -> {
                "Reading ${doc.title}. ${doc.translatedText}. Reading complete. Say 'summary' for the audio summary, or ask any question."
            }
        }
        _statusAnnouncement.value = "Reading ${doc.title}..."
        audioManager.speak(textToSpeak, flush = true) {
            startListeningForCommands()
        }
    }

    fun askQuestion(question: String) {
        val doc = _currentDocument.value ?: return
        if (question.isBlank()) return

        val userMsg = ChatMessage(id = UUID.randomUUID().toString(), isUser = true, text = question)
        _chatMessages.value = _chatMessages.value + userMsg

        _isProcessingQuestion.value = true
        _statusAnnouncement.value = "Answering: \"$question\""
        audioManager.speak("Thinking about your question...", flush = true)

        viewModelScope.launch {
            val answer = geminiService.answerQuestion(doc, question)
            val assistantMsg = ChatMessage(id = UUID.randomUUID().toString(), isUser = false, text = answer)
            _chatMessages.value = _chatMessages.value + assistantMsg
            _isProcessingQuestion.value = false
            _statusAnnouncement.value = "Answer ready"
            
            // Speak answer, then automatically prompt "Do you have any other question?" and open mic to listen!
            val spokenResponse = "$answer. Do you have any other question?"
            audioManager.speak(spokenResponse, flush = true) {
                startListeningForCommands()
            }
        }
    }

    fun resetToScanner() {
        audioManager.stopSpeaking()
        _scanState.value = AppScanState.READY
        _statusAnnouncement.value = "Say read to scan another Braille page"
        audioManager.speak("Ready. Say read to scan another Braille page.")
    }

    override fun onCleared() {
        super.onCleared()
        audioManager.release()
        voiceManager.release()
    }
}
