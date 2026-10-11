package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.BrailleTranslator
import com.example.data.SampleBrailleLibrary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Braille Vision", appName)
  }

  @Test
  fun `braille translation roundtrip test`() {
    val original = "hello world"
    val braille = BrailleTranslator.textToBraille(original)
    val backToText = BrailleTranslator.brailleToText(braille)
    assertEquals(original, backToText)
  }

  @Test
  fun `sample braille library contains books with audio summaries`() {
    val books = SampleBrailleLibrary.sampleBooks
    assertTrue(books.isNotEmpty())
    val firstBook = books.first()
    assertNotNull(firstBook.summary)
    assertTrue(firstBook.brailleUnicode.isNotBlank())
    assertTrue(firstBook.sampleQuestions.isNotEmpty())
  }

  @Test
  fun `hands-free voice manager simulates speech input and triggers callback without button press`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val voiceManager = com.example.audio.VoiceRecognitionManager(context)
    var receivedCommand: String? = null
    voiceManager.onVoiceCommandReceived = { cmd, _ ->
      receivedCommand = cmd
    }
    voiceManager.simulateVoiceInput("read")
    assertEquals("read", receivedCommand)
    assertTrue(voiceManager.isContinuousListening)
  }

  @Test
  fun `optical braille scanner analyzes bitmap and produces dynamic braille document`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val scanner = com.example.ai.OpticalBrailleScanner(context)
    val testBitmap = android.graphics.Bitmap.createBitmap(200, 200, android.graphics.Bitmap.Config.ARGB_8888)
    val document = scanner.scanImage(testBitmap)
    assertNotNull(document)
    assertTrue(document.title.isNotBlank())
    assertTrue(document.translatedText.isNotBlank())
    assertTrue(document.brailleUnicode.isNotBlank())
    assertTrue(document.summary.isNotBlank())
  }

  @Test
  fun `braille script contraction and voice summary test`() {
    // Test UEB contractions: ⠮ (the), ⠱ (wh), ⠹ (th)
    val brailleScriptSnippet = "⠮ ⠇⠁⠍⠃ ⠺⠁⠎ ⠎⠥⠗⠑ ⠞⠕ ⠛⠕"
    val decoded = BrailleTranslator.brailleToText(brailleScriptSnippet)
    assertTrue(decoded.contains("the lamb was sure to go"))
  }
}

