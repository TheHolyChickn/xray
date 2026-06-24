package com.github.theholychickn.xray.client.dialog

import com.github.theholychickn.xray.client.episode.Episodes
import net.minecraft.util.Util


/**
 * Drives the thought log display state.
 *
 * Reads the thought log from [Episodes.active] each update, so switching episodes
 * (and calling [reset]) immediately changes what is displayed without any other wiring.
 *
 * Call [update] once per render frame (from [ThoughtGui]). Call [reset] whenever
 * the active episode changes or the log needs to replay from the beginning.
 */

object DialogManager {

    /** All entries that have activated so far, newest at front. */
    private val shownEntries = ArrayDeque<ThoughtEntry>()

    /** Index into [ThoughtLog.ENTRIES] for the next entry to activate. */
    private var nextIndex = 0

    /** Absolute time (ms) when the current entry first appeared. -1 = not yet started. */
    private var currentEntryStartMs = -1L

    /** If positive, don't activate the next entry until this time has passed. */
    private var waitUntilMs = -1L

    // ── public API ────────────────────────────────────────────────────────────────────

    /** Returns entries to render, newest first. Never mutate the returned list. */
    fun getShownEntries(): List<ThoughtEntry> = shownEntries

    /** Reset the log back to the beginning (e.g. on world re-enter). */
    fun reset() {
        shownEntries.clear()
        nextIndex = 0
        currentEntryStartMs = -1L
        waitUntilMs = -1L
    }

    /**
     * Advances the dialog state. Call this once per render frame (from the HUD callback).
     * Reads the thought log from [Episodes.active]; no-ops if no episode is active
     * or the active episode has no thought log entries
     */
    fun update() {
        val entries = Episodes.active?.thoughtLog
        if (entries.isNullOrEmpty()) return

        val now = Util.getMillis()

        // still inside a between-entry wait
        if (waitUntilMs >= 0L) {
            if (now < waitUntilMs) return
            waitUntilMs = -1L
        }

        // all entries have been shown
        if (nextIndex >= entries.size) return

        val entry = entries[nextIndex]

        // First tick of this entry: add it to the front of the shown list
        if (currentEntryStartMs < 0L) {
            shownEntries.addFirst(entry)
            currentEntryStartMs = now
            return
        }

        // check if the entry's display window has elapsed
        if (now - currentEntryStartMs >= entry.displayMs) {
            if (entry.waitAfterMs > 0L) {
                waitUntilMs = now + entry.waitAfterMs
            }
            nextIndex++
            currentEntryStartMs = -1L
        }
    }
}