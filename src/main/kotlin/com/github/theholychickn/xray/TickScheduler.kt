package com.github.theholychickn.xray

import java.util.PriorityQueue


/**
 * Singleton tick-based action scheduler for the integrated server.
 *
 * ## Setup
 * Registered once in [HyperionMod.onInitialize]:
 * ```kotlin
 * ServerTickEvents.END_SERVER_TICK.register { _ -> TickScheduler.tick() }
 * ```
 *
 * ## Usage in action chains
 * You do not call this directly. [HyperionAction.wait] and [HyperionAction.build]
 * route through this automatically:
 * ```kotlin
 * HyperionAction.Builder(entity).send {
 *     teleport(0, 64, 0)
 *     wait(20)           // 1-second pause (20 server ticks)
 *     say("Arrived.")
 *     wait(40)
 *     fly()
 * }.build().invoke()
 * ```
 *
 * ## Thread safety
 * [tick] and [schedule] are @Synchronized. In practice both are called from the
 * server thread ([tick] via ServerTickEvents, [schedule] via keybind → server.execute),
 * but the lock prevents rare races during world load/unload when threads overlap.
 *
 * **Important:** keybind lambdas fire on the client tick thread. Always wrap
 * `build().invoke()` in `server.execute { }` when calling entity actions:
 * ```kotlin
 * KeyMapping("...", ...) to { client ->
 *     val server = client.singleplayerServer ?: return@to
 *     server.execute { myCommand.invoke() }
 * }
 * ```
 */
object TickScheduler {

    /**
     * A single scheduled entry. [id] is an insertion-order tie-breaker so entries
     * with the same [triggerTick] fire in the order they were scheduled.
     */
    private data class Entry(
        val triggerTick: Long,
        val id: Long,
        val action: () -> Unit
    ) : Comparable<Entry> {
        override fun compareTo(other: Entry): Int {
            val byTick = triggerTick.compareTo(other.triggerTick)
            return if (byTick != 0) byTick else id.compareTo(other.id)
        }
    }


    private val queue = PriorityQueue<Entry>()
    private var tick = 0L
    private var nextId = 0L

    // ── public API ────────────────────────────────────────────────────────────────────

    /**
     * Advances the clock by one tick and fires all actions whose delay has elapsed.
     * Must be called exactly once per server tick via [ServerTickEvents.END_SERVER_TICK].
     */
    @Synchronized
    fun tick() {
        tick++
        while (queue.isNotEmpty() && queue.peek().triggerTick <= tick) {
            queue.poll().action()
        }
    }

    /**
     * Schedules [action] to fire after [delayTicks] server ticks.
     * [delayTicks] == 0 runs the action immediately on the calling thread.
     */
    @Synchronized
    fun schedule(delayTicks: Int, action: () -> Unit) {
        if (delayTicks <= 0) {
            action()
        } else {
            queue.add(Entry(tick + delayTicks, nextId++, action))
        }
    }

    /** Cancels all pending actions. */
    @Synchronized
    fun clear() = queue.clear()
}