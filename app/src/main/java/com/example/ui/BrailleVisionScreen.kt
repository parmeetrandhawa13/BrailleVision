package com.example.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.camera.CameraPreviewView
import com.example.data.AppScanState
import com.example.data.ReadingPlaybackMode
import com.example.data.SampleBrailleLibrary
import com.example.ui.components.AudioWaveIndicator
import com.example.ui.components.BrailleDisplayCard
import com.example.ui.components.GuidanceOverlay
import com.example.ui.theme.BrailleAlertOrange
import com.example.ui.theme.BrailleAmberBright
import com.example.ui.theme.BrailleAmberContainer
import com.example.ui.theme.BrailleAmberPrimary
import com.example.ui.theme.BrailleBlack
import com.example.ui.theme.BrailleBorder
import com.example.ui.theme.BrailleCardSurface
import com.example.ui.theme.BrailleCyanBright
import com.example.ui.theme.BrailleCyanContainer
import com.example.ui.theme.BrailleDarkSurface
import com.example.ui.theme.BrailleSuccessGreen
import com.example.ui.theme.BrailleTextHighContrast
import com.example.ui.theme.BrailleTextSubtle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrailleVisionScreen(
    viewModel: BrailleVisionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scanState by viewModel.scanState.collectAsState()
    val guidance by viewModel.guidance.collectAsState()
    val document by viewModel.currentDocument.collectAsState()
    val isReadingAudio by viewModel.isReadingAudio.collectAsState()
    val playbackMode by viewModel.playbackMode.collectAsState()
    val isListeningVoice by viewModel.voiceManager.isListening.collectAsState()
    val statusAnnouncement by viewModel.statusAnnouncement.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isProcessingQuestion by viewModel.isProcessingQuestion.collectAsState()
    val speechRate by viewModel.audioManager.speechRate.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showVoiceCommandSheet by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val camGranted = permissions[Manifest.permission.CAMERA] ?: hasCameraPermission
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: hasAudioPermission
        hasCameraPermission = camGranted
        hasAudioPermission = audioGranted
        if (audioGranted) {
            viewModel.voiceManager.initRecognizer()
        }
        viewModel.triggerStartupGreetingIfNeeded()
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: arrayListOf()
            val text = matches.firstOrNull()?.trim()
            if (!text.isNullOrBlank()) {
                viewModel.handleVoiceCommand(text, matches)
            }
        }
        viewModel.voiceManager.setListeningState(false)
    }

    val requestVoiceActivation: () -> Unit = {
        if (!hasAudioPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        } else {
            showVoiceCommandSheet = true
            viewModel.startListeningForCommands()
            if (!viewModel.voiceManager.isAvailable()) {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Say 'Read' to scan Braille, or ask a question...")
                }
                try {
                    speechRecognizerLauncher.launch(intent)
                } catch (e: Exception) {
                    // System speech dialog not installed; VoiceCommandSheet provides direct action buttons
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasAudioPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        } else {
            viewModel.voiceManager.initRecognizer()
            viewModel.triggerStartupGreetingIfNeeded()
        }
    }

    var currentBitmapProvider by remember { mutableStateOf<(() -> Bitmap?)?>(null) }
    var userTypedQuestion by remember { mutableStateOf("") }
    var showQuestionPanel by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("braille_vision_screen"),
        containerColor = BrailleBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BRAILLE VISION",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = BrailleAmberBright
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        AudioWaveIndicator(
                            isActive = isReadingAudio || isListeningVoice,
                            waveColor = if (isListeningVoice) BrailleCyanBright else BrailleAmberBright
                        )
                    }
                },
                actions = {
                    // Speech rate cycle button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrailleAmberContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrailleAmberBright),
                        modifier = Modifier
                            .clickable { viewModel.audioManager.cycleSpeechRate() }
                            .padding(end = 8.dp)
                            .semantics { contentDescription = "Speech rate ${speechRate}x. Double tap to change." }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = null,
                                tint = BrailleAmberBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${speechRate}x",
                                color = BrailleAmberBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Reset / New scan button
                    if (scanState == AppScanState.READING_OUTPUT) {
                        IconButton(
                            onClick = { viewModel.resetToScanner() },
                            modifier = Modifier.testTag("reset_scan_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Scan another Braille document",
                                tint = BrailleAmberBright
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrailleDarkSurface,
                    titleContentColor = BrailleAmberBright
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // High-visibility Accessible Status & Voice Feedback Bar
            Surface(
                color = if (isListeningVoice) BrailleCyanContainer else BrailleDarkSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isListeningVoice) BrailleCyanBright else BrailleBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("audio_status_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isListeningVoice) Icons.Default.Mic else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = if (isListeningVoice) BrailleCyanBright else BrailleAmberBright,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isListeningVoice) "Hands-Free Voice Active: Listening... (Say 'read' or ask any question)" else statusAnnouncement,
                        color = BrailleTextHighContrast,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (isReadingAudio) {
                        Text(
                            text = "SPEAKING",
                            color = BrailleAmberBright,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Main Content Area based on scanState
            when (scanState) {
                AppScanState.READY, AppScanState.GUIDING, AppScanState.CENTERED_ALERT, AppScanState.PROCESSING_SCAN -> {
                    CameraAndGuidanceView(
                        scanState = scanState,
                        guidance = guidance,
                        hasCameraPermission = hasCameraPermission,
                        onGuidanceUpdate = { g ->
                            viewModel.onCameraGuidanceFrame(g, currentBitmapProvider)
                        },
                        onBitmapReady = { provider ->
                            currentBitmapProvider = provider
                        },
                        onStartScan = {
                            viewModel.startCameraGuidance()
                        },
                        onCaptureNow = {
                            viewModel.triggerManualCapture(currentBitmapProvider?.invoke())
                        },
                        onLoadSample = { sampleDoc ->
                            viewModel.loadSampleBrailleBook(sampleDoc)
                        },
                        onToggleVoiceMic = {
                            requestVoiceActivation()
                        },
                        isListeningVoice = isListeningVoice,
                        onRequestPermissions = {
                            permissionLauncher.launch(
                                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                            )
                        }
                    )
                }

                AppScanState.READING_OUTPUT, AppScanState.ERROR -> {
                    document?.let { doc ->
                        ReadingAndQaView(
                            document = doc,
                            playbackMode = playbackMode,
                            isPlaying = isReadingAudio,
                            isListeningVoice = isListeningVoice,
                            chatMessages = chatMessages,
                            isProcessingQuestion = isProcessingQuestion,
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onReplay = { viewModel.playCurrentDocumentAudio() },
                            onModeSelected = { mode -> viewModel.setPlaybackMode(mode) },
                            onVoiceQuestion = {
                                requestVoiceActivation()
                            },
                            onSendQuestion = { q -> viewModel.askQuestion(q) },
                            onNewScan = { viewModel.resetToScanner() }
                        )
                    } ?: run {
                        // In case doc is null, button to restart
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Button(
                                onClick = { viewModel.resetToScanner() },
                                colors = ButtonDefaults.buttonColors(containerColor = BrailleAmberPrimary)
                            ) {
                                Text("Restart Braille Scanner", color = BrailleBlack)
                            }
                        }
                    }
                }
            }
        }

        if (showVoiceCommandSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showVoiceCommandSheet = false
                    viewModel.stopListening()
                },
                containerColor = BrailleDarkSurface,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Microphone",
                            tint = BrailleCyanBright,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "VOICE ASSISTANT UNLOCKED",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = BrailleCyanBright,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Say a voice command into your microphone, or tap any action below:",
                        color = BrailleTextHighContrast,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            showVoiceCommandSheet = false
                            viewModel.handleVoiceCommand("read")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrailleAmberPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("voice_command_read_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BrailleBlack)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Say \"READ\" — Scan Braille Book", color = BrailleBlack, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            showVoiceCommandSheet = false
                            viewModel.handleVoiceCommand("summarize")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrailleCardSurface),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, BrailleCyanBright),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("voice_command_summarize_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Headphones, contentDescription = null, tint = BrailleCyanBright)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Say \"SUMMARIZE\" — Audio Summary", color = BrailleTextHighContrast, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            showVoiceCommandSheet = false
                            viewModel.handleVoiceCommand("full document")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrailleCardSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrailleBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("voice_command_full_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = BrailleAmberBright)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Say \"FULL READING\" — Complete Document", color = BrailleTextHighContrast, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            showVoiceCommandSheet = false
                            viewModel.handleVoiceCommand("stop")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrailleCardSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrailleBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("voice_command_stop_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Pause, contentDescription = null, tint = BrailleAlertOrange)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Say \"STOP\" — Pause Audio Reading", color = BrailleTextHighContrast, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            showVoiceCommandSheet = false
                            viewModel.stopListening()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Close Voice Assistant", color = BrailleTextSubtle)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CameraAndGuidanceView(
    scanState: AppScanState,
    guidance: com.example.data.CameraGuidance,
    hasCameraPermission: Boolean,
    onGuidanceUpdate: (com.example.data.CameraGuidance) -> Unit,
    onBitmapReady: ((() -> Bitmap?) -> Unit),
    onStartScan: () -> Unit,
    onCaptureNow: () -> Unit,
    onLoadSample: (com.example.data.BrailleDocument) -> Unit,
    onToggleVoiceMic: () -> Unit,
    isListeningVoice: Boolean,
    onRequestPermissions: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPreviewView(
                guidance = guidance,
                onGuidanceUpdate = onGuidanceUpdate,
                onBitmapReady = onBitmapReady,
                modifier = Modifier.fillMaxSize()
            )

            // Live Camera Alignment Reticle & Direction Audio Overlay
            GuidanceOverlay(guidance = guidance)
        } else {
            // Permission request placeholder with high tactile button
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = BrailleAmberBright,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Required",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrailleTextHighContrast
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Braille Vision needs access to your camera to scan and decode Braille books and tactile documents.",
                    color = BrailleTextSubtle,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRequestPermissions,
                    colors = ButtonDefaults.buttonColors(containerColor = BrailleAmberPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("grant_permission_button")
                ) {
                    Text("Grant Camera & Mic Access", color = BrailleBlack, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Processing progress overlay
        if (scanState == AppScanState.PROCESSING_SCAN) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = BrailleAmberBright,
                        strokeWidth = 5.dp,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Decoding Braille Dots...",
                        color = BrailleAmberBright,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Converting tactile book to clear voice audio",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Bottom Accessible Control Panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(BrailleDarkSurface.copy(alpha = 0.95f))
                .border(1.dp, BrailleBorder)
                .padding(16.dp)
        ) {
            // Quick Sample Braille Books Selector (so user can also test directly)
            Text(
                text = "TRY SAMPLE BRAILLE BOOK:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrailleTextSubtle,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(SampleBrailleLibrary.sampleBooks) { book ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrailleCardSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrailleAmberBright.copy(alpha = 0.6f)),
                        modifier = Modifier.clickable { onLoadSample(book) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⠃⠕⠕⠅ ",
                                color = BrailleAmberBright,
                                fontSize = 14.sp
                            )
                            Text(
                                text = book.title,
                                color = BrailleTextHighContrast,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Giant Tactile Action Buttons (Designed for visually impaired touch targets)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Command Mic Button
                Surface(
                    shape = CircleShape,
                    color = if (isListeningVoice) BrailleCyanBright else BrailleCardSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        2.5.dp,
                        if (isListeningVoice) Color.White else BrailleCyanBright
                    ),
                    modifier = Modifier
                        .size(64.dp)
                        .clickable { onToggleVoiceMic() }
                        .semantics { contentDescription = "Microphone voice command button. Double tap to speak." }
                        .testTag("voice_command_mic_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isListeningVoice) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Voice Input",
                            tint = if (isListeningVoice) BrailleBlack else BrailleCyanBright,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Primary Large Tactile SCAN Button
                Button(
                    onClick = {
                        if (scanState == AppScanState.GUIDING) {
                            onCaptureNow()
                        } else {
                            onStartScan()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (guidance.isCentered) BrailleSuccessGreen else BrailleAmberPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .testTag("primary_scan_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (scanState == AppScanState.GUIDING) Icons.Default.CenterFocusStrong else Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = BrailleBlack,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (scanState == AppScanState.GUIDING) {
                                if (guidance.isCentered) "READ NOW (CENTERED)" else "CAPTURE PAGE"
                            } else {
                                "SAY 'READ' OR TAP"
                            },
                            color = BrailleBlack,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadingAndQaView(
    document: com.example.data.BrailleDocument,
    playbackMode: ReadingPlaybackMode,
    isPlaying: Boolean,
    isListeningVoice: Boolean,
    chatMessages: List<com.example.data.ChatMessage>,
    isProcessingQuestion: Boolean,
    onTogglePlayPause: () -> Unit,
    onReplay: () -> Unit,
    onModeSelected: (ReadingPlaybackMode) -> Unit,
    onVoiceQuestion: () -> Unit,
    onSendQuestion: (String) -> Unit,
    onNewScan: () -> Unit
) {
    var questionInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector: Headphones Summary vs Full Reading
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (playbackMode == ReadingPlaybackMode.SUMMARY_ONLY) BrailleCyanContainer else BrailleCardSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        if (playbackMode == ReadingPlaybackMode.SUMMARY_ONLY) BrailleCyanBright else BrailleBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clickable { onModeSelected(ReadingPlaybackMode.SUMMARY_ONLY) }
                        .semantics { contentDescription = "Summary through headphones mode" }
                        .testTag("mode_summary_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = if (playbackMode == ReadingPlaybackMode.SUMMARY_ONLY) BrailleCyanBright else BrailleTextSubtle,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Summary (Audio)",
                            color = if (playbackMode == ReadingPlaybackMode.SUMMARY_ONLY) BrailleCyanBright else BrailleTextHighContrast,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (playbackMode == ReadingPlaybackMode.FULL_DOCUMENT) BrailleAmberContainer else BrailleCardSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        if (playbackMode == ReadingPlaybackMode.FULL_DOCUMENT) BrailleAmberBright else BrailleBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clickable { onModeSelected(ReadingPlaybackMode.FULL_DOCUMENT) }
                        .semantics { contentDescription = "Full document reading mode" }
                        .testTag("mode_full_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (playbackMode == ReadingPlaybackMode.FULL_DOCUMENT) BrailleAmberBright else BrailleTextSubtle,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Full Book Text",
                            color = if (playbackMode == ReadingPlaybackMode.FULL_DOCUMENT) BrailleAmberBright else BrailleTextHighContrast,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Tactile Audio Playback Controls
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = BrailleDarkSurface,
                border = androidx.compose.foundation.BorderStroke(2.dp, BrailleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Replay button
                    OutlinedButton(
                        onClick = onReplay,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, BrailleAmberBright),
                        modifier = Modifier
                            .height(52.dp)
                            .semantics { contentDescription = "Replay audio" }
                            .testTag("replay_audio_button")
                    ) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = null, tint = BrailleAmberBright)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Replay", color = BrailleAmberBright, fontWeight = FontWeight.Bold)
                    }

                    // Large Play / Pause button
                    Button(
                        onClick = onTogglePlayPause,
                        colors = ButtonDefaults.buttonColors(containerColor = BrailleAmberPrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .padding(horizontal = 8.dp)
                            .semantics { contentDescription = if (isPlaying) "Pause audio" else "Play audio" }
                            .testTag("play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = BrailleBlack,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPlaying) "PAUSE" else "PLAY AUDIO",
                            color = BrailleBlack,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Scan Another
                    OutlinedButton(
                        onClick = onNewScan,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, BrailleCyanBright),
                        modifier = Modifier
                            .height(52.dp)
                            .semantics { contentDescription = "Scan new page" }
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = BrailleCyanBright)
                    }
                }
            }
        }

        // Braille Display Card (Embossed Braille Dots + Translation + Summary)
        item {
            BrailleDisplayCard(
                document = document,
                isSummaryMode = (playbackMode == ReadingPlaybackMode.SUMMARY_ONLY),
                isPlaying = isPlaying
            )
        }

        // Voice Q&A Section: "Ask questions using natural language voice commands"
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = BrailleDarkSurface,
                border = androidx.compose.foundation.BorderStroke(2.dp, BrailleCyanBright),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_qa_section")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QuestionAnswer,
                                contentDescription = null,
                                tint = BrailleCyanBright,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ASK QUESTIONS ABOUT BOOK",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrailleCyanBright,
                                letterSpacing = 1.sp
                            )
                        }

                        if (isProcessingQuestion) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = BrailleCyanBright,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tap the microphone or say any question about the content. The app answers using natural voice.",
                        fontSize = 13.sp,
                        color = BrailleTextSubtle,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Suggested Questions Chips
                    if (document.sampleQuestions.isNotEmpty()) {
                        Text(
                            text = "SUGGESTED QUESTIONS:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrailleAmberBright,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        document.sampleQuestions.forEach { sampleQ ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BrailleCardSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BrailleBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { onSendQuestion(sampleQ) }
                            ) {
                                Text(
                                    text = "“$sampleQ”",
                                    color = BrailleTextHighContrast,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Conversation history
                    chatMessages.forEach { msg ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (msg.isUser) BrailleCyanContainer else BrailleCardSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (msg.isUser) BrailleCyanBright else BrailleBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (msg.isUser) "YOU ASKED" else "BRAILLE VISION ANSWER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (msg.isUser) BrailleCyanBright else BrailleAmberBright,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.text,
                                    color = BrailleTextHighContrast,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Voice Q&A Mic Bar + Typed input fallback
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isListeningVoice) BrailleCyanBright else BrailleCyanContainer,
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                if (isListeningVoice) Color.White else BrailleCyanBright
                            ),
                            modifier = Modifier
                                .size(56.dp)
                                .clickable { onVoiceQuestion() }
                                .semantics { contentDescription = "Voice question input. Double tap to ask a question." }
                                .testTag("voice_qa_mic_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isListeningVoice) Icons.Default.Mic else Icons.Default.MicOff,
                                    contentDescription = null,
                                    tint = if (isListeningVoice) BrailleBlack else BrailleCyanBright,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = questionInput,
                            onValueChange = { questionInput = it },
                            placeholder = { Text("Or type question...", color = BrailleTextSubtle, fontSize = 14.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BrailleTextHighContrast,
                                unfocusedTextColor = BrailleTextHighContrast,
                                focusedBorderColor = BrailleCyanBright,
                                unfocusedBorderColor = BrailleBorder,
                                focusedContainerColor = BrailleCardSurface,
                                unfocusedContainerColor = BrailleCardSurface
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("qa_text_input")
                        )

                        Button(
                            onClick = {
                                if (questionInput.isNotBlank()) {
                                    onSendQuestion(questionInput)
                                    questionInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrailleCyanBright),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("qa_send_button")
                        ) {
                            Text("Ask", color = BrailleBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
