package com.lnkranch.yaga.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.lnkranch.yaga.domain.FretDot
import com.lnkranch.yaga.theory.FretPosition
import com.lnkranch.yaga.theory.FretboardLocator
import com.lnkranch.yaga.theory.IntervalRole
import com.lnkranch.yaga.theory.intervalRole

// ---------------------------------------------------------------------------
// Defaults
// ---------------------------------------------------------------------------

object FretboardDefaults {
    val colorScheme: (Int?) -> Color = { interval ->
        when (intervalRole(interval)) {
            IntervalRole.Root      -> Color(0xFF2E7D32)
            IntervalRole.Third     -> Color(0xFF8B2020)
            IntervalRole.Seventh   -> Color(0xFF1A3A5C)
            null                   -> Color(0xFF616161)   // non-chord tone
            else                   -> Color(0xFF4A4A4A)   // fifth or other extension
        }
    }
}

// ---------------------------------------------------------------------------
// Inlay positions (standard guitar markers)
// ---------------------------------------------------------------------------

private val INLAY_FRETS = setOf(3, 5, 7, 9, 12, 15, 17, 19, 21)
private val DOUBLE_INLAY_FRETS = setOf(12)

// ---------------------------------------------------------------------------
// Rendering constants
// ---------------------------------------------------------------------------

private const val OPEN_ZONE_WIDTH_RATIO = 0.13f
private const val STRING_BASE_STROKE = 1.5f
private const val STRING_THICKNESS_SCALE = 0.4f
private const val STRETCH_FRET_ALPHA = 0.6f
private const val NUT_STROKE_WIDTH = 12f
private const val POSITION_LABEL_THRESHOLD = 2
private const val INLAY_RADIUS_RATIO = 0.12f
private const val DOUBLE_INLAY_OFFSET_RATIO = 0.6f
private const val NOTE_DOT_RADIUS_RATIO = 0.32f
private const val DOT_TEXT_SIZE_RATIO = 0.95f
private const val HINT_RADIUS_RATIO = 0.28f
private const val HINT_ALPHA_STRETCH = 0.40f
private const val HINT_ALPHA_NORMAL = 0.70f

// ---------------------------------------------------------------------------
// Composable
// ---------------------------------------------------------------------------

@Composable
fun FretboardDisplay(
    dots: List<FretDot>,
    playingPosition: Int,
    tappable: Boolean,
    errorDot: FretPosition? = null,
    colorScheme: (intervalFromRoot: Int?) -> Color = FretboardDefaults.colorScheme,
    onFretTap: ((string: Int, fret: Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val windowStart = playingPosition - 1
    val windowEnd   = windowStart + FretboardLocator.FRET_WINDOW_SIZE - 1

    val dotMap = dots.associateBy { it.position.string to it.position.fret }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().aspectRatio(1.8f)) {
            val density = LocalDensity.current
            val widthPx  = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }

            // ----------------------------------------------------------------
            // Geometry
            // ----------------------------------------------------------------
            //
            // Position 1 (windowStart == 0) has a special layout:
            //   [open zone] | NUT | fret-1 | fret-2 | fret-3 | fret-4 | fret-5 |
            //
            // The nut is the x-origin of the strings. Open-string note dots are
            // drawn in the narrow zone to the left of the nut. There are exactly
            // 5 regular fret columns (frets 1–5) so fret 1 appears in the FIRST
            // slot after the nut, which is visually correct.
            //
            // All other positions:
            //   | fret-N | fret-N+1 | … | fret-N+5 |
            // with 6 equal columns across the full width, no open zone.
            // ----------------------------------------------------------------

            val numStrings  = FretboardLocator.NUM_STRINGS
            val hasOpenZone = windowStart == 0

            // Width of the zone to the left of the nut (open strings only).
            val openZoneW    = if (hasOpenZone) widthPx * OPEN_ZONE_WIDTH_RATIO else 0f
            // X where the nut lives and the string lines begin.
            val nutX         = openZoneW
            // Width available for the regular fret columns.
            val fretboardW   = widthPx - nutX
            // Number of fret columns displayed in the main fretboard area.
            val numFretCols  = if (hasOpenZone) FretboardLocator.FRET_WINDOW_SIZE - 1 else FretboardLocator.FRET_WINDOW_SIZE
            val fretColW     = fretboardW / numFretCols.toFloat()

            val stringSpacing = heightPx / (numStrings - 1).toFloat()

            // String y (string 0 = high E = top).
            fun stringY(s: Int): Float = s * stringSpacing

            // X-center of the cell for the given fret number.
            // Returns null if the fret falls outside the visible range.
            fun fretCenterX(fret: Int): Float? = when {
                fret == 0 && hasOpenZone -> openZoneW / 2f
                fret == 0               -> null
                hasOpenZone             -> {
                    // Frets 1–5 occupy columns 1–5 relative to the nut.
                    val col = fret           // fret 1 → col 1, etc.
                    if (col < 1 || col > numFretCols) null
                    else nutX + (col - 0.5f) * fretColW
                }
                else -> {
                    val col = fret - windowStart   // 0-based column index
                    if (col < 0 || col >= numFretCols) null
                    else nutX + (col + 0.5f) * fretColW
                }
            }

            // Stretch frets (outer edge of the 6-fret window) are drawn at
            // reduced alpha. Not applicable at position 1.
            fun isStretchFret(fret: Int): Boolean =
                !hasOpenZone && (fret == windowStart || fret == windowEnd)

            // Map a canvas tap offset to (string, fret).
            fun tapToCell(offset: Offset): Pair<Int, Int> {
                // Equal bands: each string owns 1/numStrings of the height.
                // Rounding to nearest string-position would give edge strings (0 and 5)
                // only half the tap area of interior strings.
                val row = (offset.y / heightPx * numStrings).toInt().coerceIn(0, numStrings - 1)
                val fret = if (hasOpenZone) {
                    if (offset.x < nutX) {
                        0   // open zone tapped
                    } else {
                        val col = ((offset.x - nutX) / fretColW).toInt().coerceIn(0, numFretCols - 1)
                        col + 1   // frets 1–5
                    }
                } else {
                    val col = (offset.x / fretColW).toInt().coerceIn(0, numFretCols - 1)
                    col + windowStart
                }
                return row to fret
            }

            // ----------------------------------------------------------------
            // Pointer input
            // ----------------------------------------------------------------

            val canvasMod: Modifier = if (tappable && onFretTap != null) {
                Modifier
                    .fillMaxSize()
                    .pointerInput(widthPx, heightPx, windowStart) {
                        detectTapGestures { offset ->
                            val (string, fret) = tapToCell(offset)
                            onFretTap(string, fret)
                        }
                    }
            } else {
                Modifier.fillMaxSize()
            }

            Canvas(modifier = canvasMod) {

                // ------------------------------------------------------------
                // 1. String lines — start at the nut, end at the right edge.
                // ------------------------------------------------------------
                for (s in 0 until numStrings) {
                    val y = stringY(s)
                    drawLine(
                        color       = Color(0xFF424242),
                        start       = Offset(nutX, y),
                        end         = Offset(widthPx, y),
                        strokeWidth = STRING_BASE_STROKE + s * STRING_THICKNESS_SCALE,
                    )
                }

                // ------------------------------------------------------------
                // 2. Fret lines — n+1 lines for n columns, placed at nutX + n*fretColW.
                //    The n=0 line coincides with the nut.
                //    For non-open-zone layouts, the outer columns are stretch frets.
                // ------------------------------------------------------------
                for (n in 0..numFretCols) {
                    val x     = nutX + n * fretColW
                    val alpha = if (!hasOpenZone && (n == 0 || n == numFretCols)) STRETCH_FRET_ALPHA else 1f
                    drawLine(
                        color       = Color(0xFF757575).copy(alpha = alpha),
                        start       = Offset(x, 0f),
                        end         = Offset(x, heightPx),
                        strokeWidth = STRING_BASE_STROKE,
                    )
                }

                // ------------------------------------------------------------
                // 3. Nut (position 1) or position label (higher positions).
                // ------------------------------------------------------------
                if (hasOpenZone) {
                    drawLine(
                        color       = Color.Black,
                        start       = Offset(nutX, 0f),
                        end         = Offset(nutX, heightPx),
                        strokeWidth = NUT_STROKE_WIDTH,
                        cap         = StrokeCap.Butt,
                    )
                } else if (windowStart > POSITION_LABEL_THRESHOLD) {
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            color       = android.graphics.Color.argb(180, 255, 255, 255)
                            textSize    = heightPx * 0.12f
                            textAlign   = android.graphics.Paint.Align.LEFT
                            isAntiAlias = true
                        }
                        canvas.nativeCanvas.drawText(
                            "fr $windowStart",
                            nutX + 8f,
                            heightPx * 0.18f,
                            paint,
                        )
                    }
                }

                // ------------------------------------------------------------
                // 4. Inlay dots.
                // ------------------------------------------------------------
                val inlayRadius = fretColW * INLAY_RADIUS_RATIO
                val inlayY      = (stringY(2) + stringY(3)) / 2f
                val inlayRange  = if (hasOpenZone) 1..FretboardLocator.FRET_WINDOW_SIZE - 1 else windowStart..windowEnd

                for (fret in inlayRange) {
                    if (fret !in INLAY_FRETS) continue
                    val cx = fretCenterX(fret) ?: continue
                    if (fret in DOUBLE_INLAY_FRETS) {
                        val off = stringSpacing * DOUBLE_INLAY_OFFSET_RATIO
                        drawCircle(Color(0xFF555555), inlayRadius, Offset(cx, inlayY - off))
                        drawCircle(Color(0xFF555555), inlayRadius, Offset(cx, inlayY + off))
                    } else {
                        drawCircle(Color(0xFF555555), inlayRadius, Offset(cx, inlayY))
                    }
                }

                // ------------------------------------------------------------
                // 5. Note dots.
                // ------------------------------------------------------------
                val dotRadius  = minOf(fretColW, stringSpacing) * NOTE_DOT_RADIUS_RATIO
                val dotRange   = if (hasOpenZone) 0..FretboardLocator.FRET_WINDOW_SIZE - 1 else windowStart..windowEnd

                for (fret in dotRange) {
                    val cx = fretCenterX(fret) ?: continue
                    val stretchAlpha = if (isStretchFret(fret)) STRETCH_FRET_ALPHA else 1f

                    for (string in 0 until numStrings) {
                        val dot = dotMap[string to fret] ?: continue
                        val center = Offset(cx, stringY(string))
                        drawCircle(
                            color  = colorScheme(dot.intervalFromRoot).copy(alpha = stretchAlpha),
                            radius = dotRadius,
                            center = center,
                        )
                        drawIntoCanvas { canvas ->
                            val textPaint = android.graphics.Paint().apply {
                                color          = android.graphics.Color.WHITE
                                textSize       = dotRadius * DOT_TEXT_SIZE_RATIO
                                textAlign      = android.graphics.Paint.Align.CENTER
                                isAntiAlias    = true
                                isFakeBoldText = true
                                alpha          = (stretchAlpha * 255).toInt()
                            }
                            val textY = center.y - (textPaint.descent() + textPaint.ascent()) / 2f
                            canvas.nativeCanvas.drawText(dot.label, center.x, textY, textPaint)
                        }
                    }
                }

                // ------------------------------------------------------------
                // 6. Error dot (mistap position feedback — fretboard mode only).
                // ------------------------------------------------------------
                if (errorDot != null) {
                    val cx = fretCenterX(errorDot.fret)
                    if (cx != null) {
                        drawCircle(
                            color  = Color(0xFFE53935),
                            radius = dotRadius,
                            center = Offset(cx, stringY(errorDot.string)),
                        )
                    }
                }

                // ------------------------------------------------------------
                // 7. Tap target hints (when tappable).
                // ------------------------------------------------------------
                if (tappable) {
                    val hintRadius = minOf(fretColW, stringSpacing) * HINT_RADIUS_RATIO
                    val hintRange  = if (hasOpenZone) 0..FretboardLocator.FRET_WINDOW_SIZE - 1 else windowStart..windowEnd

                    for (fret in hintRange) {
                        val cx = fretCenterX(fret) ?: continue
                        val hintAlpha = if (isStretchFret(fret)) HINT_ALPHA_STRETCH else HINT_ALPHA_NORMAL

                        for (string in 0 until numStrings) {
                            if (dotMap.containsKey(string to fret)) continue
                            drawCircle(
                                color  = Color(0xFF757575).copy(alpha = hintAlpha),
                                radius = hintRadius,
                                center = Offset(cx, stringY(string)),
                                style  = Stroke(width = STRING_BASE_STROKE),
                            )
                        }
                    }
                }
            }
        }

        Text(
            text     = "Position $playingPosition",
            style    = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
