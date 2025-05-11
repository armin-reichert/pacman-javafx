/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.inClosedRange;
import static java.util.Objects.requireNonNull;

/**
 * Animation played when level is complete. Consists of the following steps:
 * <pre>
 *     Time (sec)         Action
 *     0.0                Start animation
 *     1.5                Hide ghosts
 *     2.0                Start n flashing cycles, each cycle takes 1/3 sec
 *     2.0 + n * 1/3 sec  Wait for 1 sec, then run the action specified for finishing the animation
 * </pre>
 * After each flashing cycle, the flashing index is incremented. This is used by the Tengen play scene renderer to
 * draw a different map color for each flashing cycle (only for the non-ARCADE maps starting at level 28)
 */
public class FlashingMazeAnimation {

    private final GameLevel level;

    private static final int TICK_HIDE_GHOSTS = 90;
    private static final int TICK_START_FLASHING_CYCLES = 120;

    private static final int TICKS_AFTER_FLASHING = 60;
    private static final int TICKS_ONE_FLASHING_CYCLE = 20;

    private final int tickEndFlashing;
    private final int tickEndAnimation;

    private int tick;
    private int flashingIndex;
    private boolean running;
    private Runnable actionOnFinished;

    public FlashingMazeAnimation(GameLevel level) {
        this.level = requireNonNull(level);
        tick = 0;
        flashingIndex = 0;
        running = false;
        tickEndFlashing = TICK_START_FLASHING_CYCLES + level.data().numFlashes() * TICKS_ONE_FLASHING_CYCLE - 1;
        tickEndAnimation = tickEndFlashing + TICKS_AFTER_FLASHING;
        Logger.info("Created: num-flashes={} single-flash-duration={} flash-start={} flash-after={} total={} index={}",
            level.data().numFlashes(), TICKS_ONE_FLASHING_CYCLE, TICK_START_FLASHING_CYCLES, TICKS_AFTER_FLASHING, tickEndAnimation + 1, flashingIndex);
    }

    public void start() {
        running = true;
    }

    public void tick() {
        if (!running) return;

        if (tick == tickEndAnimation) {
            running = false;
            if (actionOnFinished != null) actionOnFinished.run();
            return;
        }
        if (tick == TICK_HIDE_GHOSTS) {
            level.ghosts().forEach(Ghost::hide);
        }
        if (inFlashingPhase()) {
            int flashingTick = tick - TICK_START_FLASHING_CYCLES;
            if (flashingTick > 0 && flashingTick % TICKS_ONE_FLASHING_CYCLE == 0) {
                flashingIndex += 1;
                Logger.debug("Flashing index -> {} on tick {}", flashingIndex, tick);
            }
        }
        ++tick;
        Logger.debug("Tick {}: Level complete animation: {} {}", tick,
            inFlashingPhase() ? "flashing" : "",
            inHighlightPhase() ? "highlight" : "");
    }

    public void setActionOnFinished(Runnable action) {
        actionOnFinished = action;
    }

    public boolean inFlashingPhase() {
        return inClosedRange(tick, TICK_START_FLASHING_CYCLES, tickEndFlashing);
    }

    public boolean inHighlightPhase() {
        return inFlashingPhase() && (tick - TICK_START_FLASHING_CYCLES) % TICKS_ONE_FLASHING_CYCLE >= TICKS_ONE_FLASHING_CYCLE / 2;
    }

    public int flashingIndex() { return flashingIndex; }
}