package com.github.theholychickn.xray.client

import com.github.theholychickn.xray.client.dialog.DialogManager
import com.github.theholychickn.xray.client.episode.Episodes
import com.github.theholychickn.xray.config.ConfigManager
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor

/**
 * Renders Hyperion's thought log as a persistent HUD overlay in the top-left corner.
 *
 * Layout:
 * ```
 * ┌──────────────────────────────┐  ← GUI_Y
 * │ newest thought               │  ← line 0  (most recently added)
 * │ previous thought             │  ← line 1
 * │ …                            │
 * │ oldest visible thought       │  ← line MAX_LINES-1
 * └──────────────────────────────┘
 * ```
 *
 * New entries appear at the top and push older content downward.
 * Lines exceeding [MAX_LINES] are silently dropped from the bottom.
 *
 * Text wrapping is automatic: entries whose text (or wrapped lines) would exceed
 * [GUI_WIDTH] are word-wrapped. Explicit `\n` characters are also respected.
 */
object ThoughtGui {

    // ── layout ────────────────────────────────────────────────────────────────────────

    /** Maximum number of visible lines. Increase here if you need more room. */
    const val MAX_LINES = 15

    /** Distance from the right edge of the screen in GUI pixels. */
    const val GUI_MARGIN_RIGHT = 5

    /** Distance from the top edge of the screen in GUI pixels. */
    const val GUI_MARGIN_TOP = 5

    /** Width of the overlay box in GUI pixels, including internal padding. */
    const val GUI_WIDTH = 260

    /** Height of one line in GUI pixels. Minecraft's default font is 8px + 1px line gap. */
    const val LINE_HEIGHT = 9

    /** Internal padding on all four sides of the box. */
    const val PADDING = 4

    // ── colors (ARGB) ─────────────────────────────────────────────────────────────────

    private val COLOR_TEXT       = 0xFFD0D0D0.toInt()   // light gray, fully opaque
    private val COLOR_BACKGROUND = 0xCC000000.toInt()   // black, ~80% opacity
    private val COLOR_BORDER     = 0xFF2A2A2A.toInt()   // very dark gray, fully opaque

    // ── render ────────────────────────────────────────────────────────────────────────

    /**
     * Called by [HyperionModClient] every frame via HudRenderCallback.
     * No-ops if the active episode does not have [shouldRenderThoughts] set.
     */
    fun render(graphics: GuiGraphicsExtractor, deltaTracker: DeltaTracker) {
        val episode = Episodes.active ?: return
        if (!episode.shouldRenderThoughts) return

        val client = Minecraft.getInstance()
        if (client.player == null) return

        DialogManager.update()

        val font         = client.font
        val maxTextWidth = GUI_WIDTH - PADDING * 2

        // Collect wrapped lines from shown entries (newest first) up to MAX_LINES
        val lines = mutableListOf<String>()
        for (entry in DialogManager.getShownEntries()) {
            for (line in wrapText(entry.text, font, maxTextWidth)) {
                lines.add(line)
                if (lines.size >= MAX_LINES) break
            }
            if (lines.size >= MAX_LINES) break
        }

        if (lines.isEmpty()) return

        val screenWidth = client.window.guiScaledWidth
        val boxX        = screenWidth - GUI_WIDTH - GUI_MARGIN_RIGHT
        val boxY        = GUI_MARGIN_TOP
        val boxHeight   = lines.size * LINE_HEIGHT + PADDING * 2

        // Background
        graphics.fill(boxX, boxY, boxX + GUI_WIDTH, boxY + boxHeight, COLOR_BACKGROUND)

        // 1-pixel border
        graphics.outline(boxX, boxY, GUI_WIDTH, boxHeight, COLOR_BORDER)

        // Text lines — index 0 is the newest, rendered at the top
        lines.forEachIndexed { i, line ->
            graphics.text(
                font,
                line,
                boxX + PADDING,
                boxY + PADDING + i * LINE_HEIGHT,
                COLOR_TEXT,
                false
            )
        }
    }

    // ── word wrap ─────────────────────────────────────────────────────────────────────

    /**
     * Wraps [text] to fit within [maxWidth] GUI pixels using [font] metrics.
     * Respects explicit `\n` line breaks first, then word-wraps each paragraph.
     * Words wider than [maxWidth] on their own are emitted as-is on a single line.
     */
    private fun wrapText(text: String, font: Font, maxWidth: Int): List<String> {
        val result = mutableListOf<String>()
        for (paragraph in text.split("\n")) {
            if (paragraph.isEmpty()) { result.add(""); continue }
            var currentLine = ""
            for (word in paragraph.split(" ")) {
                val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (font.width(candidate) <= maxWidth) {
                    currentLine = candidate
                } else {
                    if (currentLine.isNotEmpty()) result.add(currentLine)
                    currentLine = word
                }
            }
            if (currentLine.isNotEmpty()) result.add(currentLine)
        }
        return result
    }
}