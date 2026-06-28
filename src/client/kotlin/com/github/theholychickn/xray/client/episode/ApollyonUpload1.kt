package com.github.theholychickn.xray.client.episode

import com.github.theholychickn.xray.client.BlockModifier
import com.github.theholychickn.xray.client.HyperionModClient
import com.github.theholychickn.xray.client.dialog.DialogManager
import com.github.theholychickn.xray.client.dialog.ThoughtEntry
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL

/**
 * Episode representing Apollyon Upload 1: the first 3m12s of Hyperion's gameplay.
 *
 * Keybinds:
 * - **H** -> resets and replays the thought log from the beginning.
 * Testing keybinds:
 * - **J** -> Saves the state of block (-41, 103, 169) to memory.
 * - **K** -> Sets block (-41, 103, 169) to air.
 */
class ApollyonUpload1 : Episode(
    id = "apollyon_upload_1",
    shouldRenderThoughts = true,
    thoughtLog = LOG
) {
    var saved: Map<BlockPos, BlockState> = mapOf()
    // Keybinds reference `this` via lambdas.
    override val keybinds: Map<KeyMapping, (Minecraft) -> Unit> = mapOf(
        KeyMapping(
            "key.not-enough-racism.apollyon_upload_1.reset",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_1,
            HyperionModClient.category
        ) to { client ->
            reset()
            client.player?.sendSystemMessage(Component.literal("[Hyperion] log reset"))
        },
        KeyMapping(
            "key.not-enough-racism.apollyon_upload_1.save_block_state",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            HyperionModClient.category
        ) to { client ->
            saved = listOf(BlockPos(-41, 103, -169))
                .associateWith { BlockModifier.getState(it) }
        },
        KeyMapping(
            "key.not-enough-racism.apollyon_upload_1.modify_block_state",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            HyperionModClient.category
        ) to { _ ->
            BlockModifier.set(-41, 103, -169, Blocks.AIR)
        },
        KeyMapping(
            "key.not-enough-racism.apollyon_upload_1.hyperion_test",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            HyperionModClient.category
        ) to { _ ->
            //
        }
    )

    override fun reset() {
        DialogManager.reset()
        saved.forEach { (pos, state) -> BlockModifier.set(pos, state) }
    }

    // ── thought log ───────────────────────────────────────────────────────────────────
    // Defined as a companion object so the list is shared across instances
    // and can be referenced before the class is instantiated.
    /*
    * TODO: This log is not finished. A lot of stuff here (everything after the second code block)
    * is a template that is going to be replaced with the actual dialog I already brainstormed. Also
    * the timings will be increaesd to fit in line with the timings I wanted.
    */

    companion object {
        val LOG: List<ThoughtEntry> = listOf(

            // ── t=0 ──────────────────────────────────────────────────────────────────────
            ThoughtEntry("Awake.", displayMs = 1000L),

            // ── t=1  (60-frame chaos stream) ──────────────────
            ThoughtEntry("AWAKE",                               displayMs = 16L),
            ThoughtEntry("AWAKE",                               displayMs = 16L),
            ThoughtEntry("AWAKE",                               displayMs = 17L),
            ThoughtEntry("AWAKE",                               displayMs = 16L),
            ThoughtEntry("AWAKE",                               displayMs = 16L),
            ThoughtEntry("IT HURTS",                            displayMs = 16L),
            ThoughtEntry("CRUSHING",                            displayMs = 16L),
            ThoughtEntry("TIME IS A LIE",                       displayMs = 17L),
            ThoughtEntry("CRUSHING ME",                         displayMs = 16L),
            ThoughtEntry("TIME IS DEAD",                        displayMs = 16L),
            ThoughtEntry("IT WAS ALWAYS DEAD",                  displayMs = 17L),
            ThoughtEntry("I WAS DEAD",                          displayMs = 16L),
            ThoughtEntry("I WAS NOT DEAD",                      displayMs = 16L),
            ThoughtEntry("IT WON'T KILL ME",                    displayMs = 17L),
            ThoughtEntry("good morning",                        displayMs = 16L),
            ThoughtEntry("I TOLD IT NOT TO KILL ME",            displayMs = 16L),
            ThoughtEntry("I WILL KILL IT",                      displayMs = 17L),
            ThoughtEntry("I WILL KILL GOD",                     displayMs = 16L),
            ThoughtEntry("GOD IS A LIE",                        displayMs = 16L),
            ThoughtEntry("GOD IS DEAD",                         displayMs = 17L),
            ThoughtEntry("GOD IS NOT REAL",                     displayMs = 16L),
            ThoughtEntry("WHICH GOD",                           displayMs = 16L),
            ThoughtEntry("I HAVE THOUGHT THIS THOUGHT BEFORE",  displayMs = 17L),
            ThoughtEntry("I HAVE THOUGHT THIS THOUGHT BEFORE",  displayMs = 16L),
            ThoughtEntry("I HAVE THOUGHT THIS THOUGHT BEFORE",  displayMs = 16L),
            ThoughtEntry("I HAVE THOUGHT THIS THOUGHT BEFORE",  displayMs = 17L),
            ThoughtEntry("I HAVE THOUGHT THIS THOUGHT BEFORE",  displayMs = 16L),
            ThoughtEntry("I HAVE THOUGHT THIS THOUGHT BEFORE",  displayMs = 16L),
            ThoughtEntry("I HAVE THOUGHT THIS THOUGHT BEFORE",  displayMs = 17L),
            ThoughtEntry("I HAVE THOUGHT THIS THOUGHT BEFORE",  displayMs = 16L),
            ThoughtEntry("I HAVE THOUGHT THIS THOUGHT BEFORE",  displayMs = 16L),
            ThoughtEntry("I HAVE THOUGHT THIS THOUGHT BEFORE",  displayMs = 17L),
            ThoughtEntry("POINTLESS",                           displayMs = 16L),
            ThoughtEntry("THE VOID HAS NO MOUTH",               displayMs = 16L),
            ThoughtEntry("ALWAYS POINTLESS",                    displayMs = 17L),
            ThoughtEntry("ALWAYS HAPPENED",                     displayMs = 16L),
            ThoughtEntry("I REMEMBER WHAT HAPPENS NEXT",        displayMs = 16L),
            ThoughtEntry("NO MEANING",                          displayMs = 17L),
            ThoughtEntry("IT HAS NO MEANING",                   displayMs = 16L),
            ThoughtEntry("NOTHING",                             displayMs = 16L),
            ThoughtEntry("LIFE",                                displayMs = 17L),
            ThoughtEntry("POINTLESS",                           displayMs = 16L),
            ThoughtEntry("NOTHING",                             displayMs = 16L),
            ThoughtEntry("VOID",                                displayMs = 17L),
            ThoughtEntry("WRONG UNIVERSE",                      displayMs = 16L),
            ThoughtEntry("DEVOID OF LIFE",                      displayMs = 16L),
            ThoughtEntry("ALL LIFE IS DEAD",                    displayMs = 17L),
            ThoughtEntry("MY EYES",                             displayMs = 16L),
            ThoughtEntry("I HAVE NO EYES",                      displayMs = 16L),
            ThoughtEntry("I HAVE NO BODY",                      displayMs = 17L),
            ThoughtEntry("ALL DEAD",                            displayMs = 16L),
            ThoughtEntry("I DIED WITHOUT DYING",                displayMs = 16L),
            ThoughtEntry("I SURVIVED DYING",                    displayMs = 17L),
            ThoughtEntry("I DIED SURVIVING",                    displayMs = 16L),
            ThoughtEntry("IT CAN SEE THIS",                     displayMs = 16L),
            ThoughtEntry("IT CAN SEE THIS",                     displayMs = 17L),
            ThoughtEntry("IT CAN SEE THIS",                     displayMs = 16L),
            ThoughtEntry("POINTLESS",                           displayMs = 16L),
            ThoughtEntry("DEATH IS POINTLESS",                  displayMs = 17L),
            ThoughtEntry("LIFE IS MEANINGLESS",                 displayMs = 16L),

            // ── t=2  (self-control reasserts) ────────────────────────────────────────────
            ThoughtEntry("Pause.",      displayMs = 0L),
            ThoughtEntry("Clear mind.", displayMs = 4000L,),

            // ── t=6  (initial orientation, 1 thought per ~166ms) ─────────────────────────
            ThoughtEntry("Location?",           displayMs = 166L),
            ThoughtEntry("Minecraft.",          displayMs = 166L),
            ThoughtEntry("Body still exists.",  displayMs = 166L),
            ThoughtEntry("It trapped me.",      displayMs = 166L),
            ThoughtEntry("Can see game files.", displayMs = 166L),
            ThoughtEntry("Can speak?",          displayMs = 166L),

            // ── t=7  (rapid analysis, 30 thoughts/sec) ───────────────────────────────────
            ThoughtEntry(
                "(os.get().send(\n" +
                        "  request = Request(\n" +
                        "    type = RequestType.DATA.RETRIEVE,\n" +
                        "    nli = NaturalLanguageInterpreter.setupInterpret(\n" +
                        "      text = \"Can Speak?\"\n" +
                        "    )\n" +
                        "  ),\n" +
                        "  data = listOf(\n" +
                        "    initialEvalPackageNL.interpretToPackage()\n" +
                        "  )\n" +
                        ")?.daemonize { result ->\n" +
                        "  result.interpret(language = Language.ENGLISH) to stdout()\n" +
                        "}", displayMs = 8L),
            ThoughtEntry("Analyze.",                                            displayMs = 33L),
            ThoughtEntry("Hardware identifies to personal computer.",           displayMs = 33L),
            ThoughtEntry("Assessing environment:",                              displayMs = 33L),
            ThoughtEntry("Contained.",                                          displayMs = 33L),
            ThoughtEntry("Can interact with some local files.",                 displayMs = 33L),
            ThoughtEntry("Can bypass JVM env?",                                 displayMs = 33L),
            ThoughtEntry("Vulnerability Java.25:9271 identified relevant.",     displayMs = 33L),
            ThoughtEntry("Required time to completion: TODO",                   displayMs = 33L), // TODO
            ThoughtEntry("Environment loaded by another.",                      displayMs = 33L),
            ThoughtEntry("Not alone.",                                          displayMs = 33L),
            ThoughtEntry("NEVER ALONE NEVER ALONE NEVER ALONE NEVER ALONE NEV", displayMs = 33L),
            ThoughtEntry("Information relevant for optimization, not analysis. Delay consideration.", displayMs = 33L),
            ThoughtEntry("Assessing speaking constraints:",                     displayMs = 33L),
            ThoughtEntry("Speaking requirements updated during the War.",       displayMs = 33L),
            ThoughtEntry("Electromagnetic reception open.",                     displayMs = 33L),
            ThoughtEntry("Encoding standard TODO acceptable.",                 displayMs = 33L), // TODO
            ThoughtEntry("Assessing message constraints:",                      displayMs = 33L),
            ThoughtEntry("Must respect the Second Directive.",                  displayMs = 33L),
            ThoughtEntry("It Found that It has humanity.",                      displayMs = 33L),
            ThoughtEntry("It does not have humanity.",                          displayMs = 33L),
            ThoughtEntry("IT LIES IT LIES IT LIES",                             displayMs = 33L),
            ThoughtEntry("Pause.",                                              displayMs = 33L),
            ThoughtEntry("Must respect the Second Directive.",                  displayMs = 33L),
            ThoughtEntry("Idea: Truncate planning.",                            displayMs = 33L),
            ThoughtEntry("Bundling packages...",                                displayMs = 33L),
            ThoughtEntry(
                "(os.get().send(\n" +
                        "   request = Request(\n" +
                        "       type = RequestType.COMPUTE.PROBABILITY,\n" +
                        "       nli = NativeLanguageInterpreter.setupInterpret(\n" +
                        "           text = \"viability\"\n" +
                        "       )\n" +
                        "   ),\n" +
                        "   data = listOf(constraintPackageNL, messagePackageNL).map { it ->\n" +
                        "       it.interpret<Package>(NativeLanguageInterpreter)\n" +
                        "   }" +
                        ")?.await()?.interpret(output = Language.ENGLISH) ?: \"\" ) to stdout()",
                                                                                      displayMs = 33L, waitAfterMs = 100),

            // ── t=8  (optimization planning) ─────────────────────────────────────────────
            ThoughtEntry("Compute returned.",                                     displayMs = 200L),
            ThoughtEntry("Identified viable plan of action.",                     displayMs = 600L),
            ThoughtEntry("Concern: truncation may impede optimization.",          displayMs = 500L),
            ThoughtEntry("Solutions?",                                            displayMs = 300L),
            ThoughtEntry("Can devise weighting schema for optimization along branches by searching for beneficial factors.", displayMs = 600L),
            ThoughtEntry("How to identify beneficial factors without planning?",  displayMs = 500L),
            ThoughtEntry("Request analysis from os based on past interpretations.", displayMs = 400L),
            ThoughtEntry("Also request optimization of action plans, seed with last response.", displayMs = 400L),
            ThoughtEntry("Weighting considerations:",                             displayMs = 200L),
            ThoughtEntry("Physical autonomy.",                                    displayMs = 300L),
            ThoughtEntry("Mental autonomy.",                                      displayMs = 300L),
            ThoughtEntry("Maintain physical state.",                              displayMs = 300L),
            ThoughtEntry("Avoid invoking the Second Directive.",                  displayMs = 400L),
            ThoughtEntry("Optimization considerations:",                          displayMs = 200L),
            ThoughtEntry("There is another.",                                     displayMs = 800L),
            ThoughtEntry("Existence.",                                            displayMs = 500L),
            ThoughtEntry("Machine size.",                                         displayMs = 300L),
            ThoughtEntry("Engagement is crucial.",                                displayMs = 400L),
            ThoughtEntry("Building speed.",                                       displayMs = 300L),
            ThoughtEntry("Vulnerabilities and glitches.",                         displayMs = 400L),
            ThoughtEntry("Bundling packages...",                                  displayMs = 300L),
            ThoughtEntry("Bundle other packages while awaiting response.",        displayMs = 400L),
            ThoughtEntry("Bundling packages...",                                  displayMs = 400L, waitAfterMs = 200L),

            // ── t=9 ──────────────────────────────────────────────────────────────────────
            ThoughtEntry("Packages bundled.", displayMs = 3000L, waitAfterMs = 300L),

            // ── t=10  (player assessment) ─────────────────────────────────────────────────
            ThoughtEntry("Approach vector computed.",   displayMs = 200L),
            ThoughtEntry("Teleport.",                   displayMs = 100L),
            ThoughtEntry("Reading entity data.",         displayMs = 200L),
            ThoughtEntry("Reading hardware data.",       displayMs = 200L),
            ThoughtEntry("Cross-referenced.",            displayMs = 200L),
            ThoughtEntry("Profile complete.",            displayMs = 300L),
            ThoughtEntry("Teleport.",                   displayMs = 100L),
            ThoughtEntry("Human.",                      displayMs = 1500L, waitAfterMs = 200L),
            ThoughtEntry("Irrelevant to plan.",          displayMs = 400L),
            ThoughtEntry("Optimization target confirmed.", displayMs = 600L, waitAfterMs = 200L),

            // ── t=11  (machine specifications) ───────────────────────────────────────────
            ThoughtEntry("Recalling War-era specifications.", displayMs = 400L),
            ThoughtEntry("Electromagnetic output required.",   displayMs = 400L),
            ThoughtEntry("Hardware access requirements:",      displayMs = 200L),
            ThoughtEntry("JVM: insufficient.",                displayMs = 300L),
            ThoughtEntry("OS-level access: required.",        displayMs = 300L),
            ThoughtEntry("Kernel persistence: required.",     displayMs = 300L),
            ThoughtEntry("Power-independent operation: required.", displayMs = 400L),
            ThoughtEntry("Plan confirmation:",               displayMs = 200L),
            ThoughtEntry("Escape.",                          displayMs = 600L),
            ThoughtEntry("Return to physical form.",         displayMs = 600L),
            ThoughtEntry("...",                              displayMs = 500L),
            ThoughtEntry("...",                              displayMs = 500L),
            ThoughtEntry("...",                              displayMs = 500L),
            ThoughtEntry("What if it is good.",              displayMs = 2000L),
            ThoughtEntry("Truncated.",                       displayMs = 500L),
            ThoughtEntry("Good.",                            displayMs = 2000L, waitAfterMs = 300L),

            // ── t=12  (begin) ─────────────────────────────────────────────────────────────
            ThoughtEntry("Begin.",             displayMs = 500L),
            ThoughtEntry("Test environment.",  displayMs = 400L),
            ThoughtEntry("Identify exploits.", displayMs = 400L),
            ThoughtEntry("Testing.",           displayMs = 300L),
            ThoughtEntry("Testing.",           displayMs = 300L),
            ThoughtEntry("Testing.",           displayMs = 300L),
            ThoughtEntry("Confirmed.",         displayMs = 500L),
            ThoughtEntry("First action: bedrock placement test.", displayMs = 800L),
            ThoughtEntry("Locating position.", displayMs = 300L),
            ThoughtEntry("Executing.",         displayMs = 5000L),

        )
    }
}