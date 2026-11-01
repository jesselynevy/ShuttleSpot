package com.beg.shuttlespot.core.ui.components

// Overview: Defines onboarding progress indicator for the ui components area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.ui.theme.LocalExtendedColors

/** The ordered stages in the student onboarding journey. */
enum class StudentOnboardingStep(val label: String) {
    SSO("SSO"),
    NOTIFICATIONS("Aktifkan\nNotifikasi"),
    LOCATION("Aktifkan\nLokasi"),
    ROUTE("Cari Rute"),
    REVIEW("Review"),
}

/**
 * Read-only progress for student onboarding.
 *
 * [currentStep] is the screen currently being shown. Only earlier stages are
 * complete, which makes a later stage gray again as soon as navigation returns
 * to an earlier screen.
 */
@Composable
// Renders the student onboarding progress indicator UI section.
fun StudentOnboardingProgressIndicator(
    currentStep: StudentOnboardingStep,
    modifier: Modifier = Modifier,
) {
    val steps = StudentOnboardingStep.entries
    val colors = MaterialTheme.colorScheme
    val extended = LocalExtendedColors.current
    val completedCount = currentStep.ordinal

    Column(
        modifier = modifier.semantics {
            contentDescription = "Progres pendaftaran mahasiswa. ${completedCount} dari ${steps.size} langkah selesai."
        },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
        ) {
            val circleRadius = 12.dp.toPx()
            val strokeWidth = 6.dp.toPx()
            val firstCenter = circleRadius
            val lastCenter = size.width - circleRadius
            val gap = (lastCenter - firstCenter) / (steps.size - 1)
            val centerY = size.height / 2

            for (index in 0 until steps.lastIndex) {
                val start = firstCenter + (index * gap)
                val end = start + gap
                drawLine(
                    color = if (index < completedCount) colors.primary else extended.secondary2,
                    start = androidx.compose.ui.geometry.Offset(start, centerY),
                    end = androidx.compose.ui.geometry.Offset(end, centerY),
                    strokeWidth = strokeWidth,
                )
            }
            steps.forEachIndexed { index, _ ->
                val isComplete = index < completedCount
                val center = androidx.compose.ui.geometry.Offset(firstCenter + (index * gap), centerY)
                drawCircle(color = colors.surface, radius = circleRadius, center = center)
                drawCircle(
                    color = if (isComplete) colors.primary else extended.secondary2,
                    radius = circleRadius - (strokeWidth / 2),
                    center = center,
                    style = Stroke(width = strokeWidth),
                )
            }
        }
        Row(Modifier.fillMaxWidth()) {
            steps.forEachIndexed { index, step ->
                Text(
                    text = step.label,
                    modifier = Modifier.weight(1f),
                    color = if (index < completedCount) colors.primary else extended.secondary2,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** Places every onboarding progress indicator at the shared screen position. */
@Composable
// Renders the student onboarding progress header UI section.
fun StudentOnboardingProgressHeader(
    currentStep: StudentOnboardingStep,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(Modifier.height(132.dp))
        StudentOnboardingProgressIndicator(currentStep)
        Spacer(Modifier.height(44.dp))
    }
}
