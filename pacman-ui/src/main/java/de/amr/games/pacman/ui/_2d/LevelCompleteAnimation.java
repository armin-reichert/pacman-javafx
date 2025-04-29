/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Ghost;
import org.tinylog.Logger;

import static de.amr.games.pacman.Globals.inClosedRange;
import static java.util.Objects.requireNonNull;

//TODO use Timeline?
public class LevelCompleteAnimation {

    private final GameLevel level;

    private final int flashingStartTick = 120;
    private final int ticksAfterFlashing = 60;
    private final int ghostsHiddenTick = 90;
    private final int singleFlashTicks = 20;

    private final int flashingEndTick;
    private final int lastTick;

    private int t;
    private int flashingIndex;
    private boolean running;
    private Runnable onFinished;

    public LevelCompleteAnimation(GameLevel level) {
        this.level = requireNonNull(level);
        t = 0;
        flashingIndex = 0;
        running = false;
        flashingEndTick = flashingStartTick + level.data().numFlashes() * singleFlashTicks - 1;
        lastTick = flashingEndTick + ticksAfterFlashing;
        Logger.info("Created: num-flashes={} single-flash-duration={} flash-start={} flash-after={} total={} index={}",
            level.data().numFlashes(), singleFlashTicks, flashingStartTick, ticksAfterFlashing, lastTick + 1, flashingIndex);
    }

    public void start() {
        running = true;
    }

    public void tick() {
        if (!running) return;

        if (t == lastTick) {
            running = false;
            if (onFinished != null) onFinished.run();
            return;
        }
        if (t == ghostsHiddenTick) {
            level.ghosts().forEach(Ghost::hide);
        }
        if (isFlashing()) {
            int flashingTick = t - flashingStartTick;
            if (flashingTick > 0 && flashingTick % singleFlashTicks == 0) {
                flashingIndex += 1;
                Logger.debug("Flashing index -> {} on tick {}", flashingIndex, t);
            }
        }
        ++t;
        Logger.debug("Tick {}: Level complete animation: {} {}", t,
            isFlashing() ? "flashing" : "",
            isInHighlightPhase() ? "highlight" : "");
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public boolean isFlashing() {
        return inClosedRange(t, flashingStartTick, flashingEndTick);
    }

    public int flashingIndex() { return flashingIndex; }

    public boolean isInHighlightPhase() {
        return isFlashing() && (t - flashingStartTick) % singleFlashTicks >= singleFlashTicks / 2;
    }
}