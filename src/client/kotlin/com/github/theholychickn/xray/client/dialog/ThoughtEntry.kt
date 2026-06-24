package com.github.theholychickn.xray.client.dialog

/**
 * A single entry in Hyperion's thought log.
 *
 * @param text        The text to display. Supports `\n` for explicit line breaks.
 *                    Lines that exceed the GUI width are word-wrapped automatically.
 * @param displayMs   How long this entry stays at the top of the log before the next
 *                    one activates and pushes it down. Default: 3 seconds.
 * @param waitAfterMs Additional delay after [displayMs] before the next entry activates.
 *                    Use this to simulate computation pauses (e.g. awaiting OS response).
 */
data class ThoughtEntry(
    val text: String,
    val displayMs: Long = 3000L,
    val waitAfterMs: Long = 0L
)