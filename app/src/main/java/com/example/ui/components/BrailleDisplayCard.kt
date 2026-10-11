package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BrailleDocument
import com.example.ui.theme.BrailleAmberBright
import com.example.ui.theme.BrailleAmberContainer
import com.example.ui.theme.BrailleBorder
import com.example.ui.theme.BrailleCardSurface
import com.example.ui.theme.BrailleCyanBright
import com.example.ui.theme.BrailleCyanContainer
import com.example.ui.theme.BrailleDarkSurface
import com.example.ui.theme.BrailleTextHighContrast
import com.example.ui.theme.BrailleTextSubtle

@Composable
fun BrailleDisplayCard(
    document: BrailleDocument,
    isSummaryMode: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    var showFullTextWhenSummary by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("braille_display_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrailleCardSurface),
        border = androidx.compose.foundation.BorderStroke(2.dp, BrailleBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Title & Mode badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "Book icon",
                        tint = BrailleAmberBright,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = document.title,
                        color = BrailleTextHighContrast,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSummaryMode) BrailleCyanContainer else BrailleAmberContainer,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSummaryMode) BrailleCyanBright else BrailleAmberBright
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSummaryMode) Icons.Default.Hearing else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isSummaryMode) BrailleCyanBright else BrailleAmberBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSummaryMode) "Headphones Summary" else "Full Reading",
                            color = if (isSummaryMode) BrailleCyanBright else BrailleAmberBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Braille Dots Representation Box
            if (document.brailleUnicode.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, BrailleAmberBright.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "EMBOSSED BRAILLE DOTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrailleAmberBright,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = document.brailleUnicode,
                            fontSize = 22.sp,
                            color = BrailleAmberBright,
                            letterSpacing = 2.sp,
                            lineHeight = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Executive Summary Section (Always visible, highlights spoken overview that covers everything)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BrailleDarkSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isSummaryMode) BrailleCyanBright else BrailleBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = BrailleCyanBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EXECUTIVE VOICE SUMMARY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrailleCyanBright,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrailleCyanContainer
                        ) {
                            Text(
                                text = "COVERS ALL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrailleCyanBright,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = document.summary,
                        fontSize = 16.sp,
                        color = BrailleTextHighContrast,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Key bullet points
                    if (document.keyPoints.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "KEY TAKEAWAYS:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrailleAmberBright,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        document.keyPoints.forEach { point ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "• ",
                                    color = BrailleAmberBright,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = point,
                                    color = BrailleTextHighContrast.copy(alpha = 0.95f),
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Full Translated Text Section
            if (!isSummaryMode) {
                // If in full mode, show full translated text expanded
                Text(
                    text = "TRANSLATED FULL TEXT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrailleAmberBright,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = document.translatedText,
                    fontSize = 16.sp,
                    color = BrailleTextHighContrast,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.Normal
                )
            } else {
                // If in summary mode, allow expanding the full translated text easily
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrailleBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFullTextWhenSummary = !showFullTextWhenSummary }
                        .semantics {
                            contentDescription = if (showFullTextWhenSummary) "Collapse full transcript" else "Expand full transcript"
                        }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = BrailleAmberBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "FULL SCRIPT TRANSCRIPT",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrailleAmberBright
                                )
                            }
                            Icon(
                                imageVector = if (showFullTextWhenSummary) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = BrailleTextSubtle
                            )
                        }

                        if (showFullTextWhenSummary) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = document.translatedText,
                                fontSize = 15.sp,
                                color = BrailleTextHighContrast,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
