package com.example.data

data class BrailleDocument(
    val id: String,
    val title: String,
    val brailleUnicode: String,
    val translatedText: String,
    val summary: String,
    val keyPoints: List<String> = emptyList(),
    val sampleQuestions: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class GuidanceDirection(val instruction: String, val audioAnnouncement: String) {
    MOVE_LEFT("Move camera left", "Move camera left"),
    MOVE_RIGHT("Move camera right", "Move camera right"),
    MOVE_UP("Move camera up", "Move camera up"),
    MOVE_DOWN("Move camera down", "Move camera down"),
    MOVE_CLOSER("Move closer to the Braille page", "Move closer"),
    MOVE_BACK("Move slightly further back", "Move further"),
    HOLD_STEADY("Hold steady...", "Hold steady"),
    PERFECTLY_CENTERED("Document is perfectly centered!", "Document is perfectly centered")
}

data class CameraGuidance(
    val direction: GuidanceDirection = GuidanceDirection.HOLD_STEADY,
    val horizontalOffset: Float = 0f, // -1 (far left) to +1 (far right)
    val verticalOffset: Float = 0f,   // -1 (too high) to +1 (too low)
    val brightness: Float = 0.5f,
    val isCentered: Boolean = false,
    val stabilityCount: Int = 0
)

enum class AppScanState {
    READY,
    GUIDING,
    CENTERED_ALERT,
    PROCESSING_SCAN,
    READING_OUTPUT,
    ERROR
}

enum class ReadingPlaybackMode {
    FULL_DOCUMENT,
    SUMMARY_ONLY
}

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
