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

public class LevelCompleteAnimation {

    private final GameLevel level;
    private int flashingStartTick = 120;
    private int ticksAfterFlashing = 60;
    private int ghostsHiddenTick = 90;
    private final int singleFlashDuration;
    private final int flashingEndTick;
    private final int lastTick;
    private int flashingIndex;

    private int t;
    private boolean running;
    private Runnable onFinished;

    public LevelCompleteAnimation(GameLevel level, int highlightDuration) {
        this.level = requireNonNull(level);
        singleFlashDuration = 2 * highlightDuration;
        flashingEndTick = flashingStartTick + level.data().numFlashes() * singleFlashDuration - 1;
        lastTick = flashingEndTick + ticksAfterFlashing;
        t = 0;
        flashingIndex = 0;
        running = false;
        Logger.info("Created: flashes={} f-duration={} f-start={} f-after={} total={} index={}",
            level.data().numFlashes(), 2*highlightDuration, flashingStartTick, ticksAfterFlashing, lastTick + 1, flashingIndex);
    }

    public void start() {
        running = true;
        Logger.info("Level complete animation started");
    }

    public void update() {
        if (running) {
            if (t == lastTick) {
                running = false;
                if (onFinished != null)  onFinished.run();
                return;
            }
            if (t == ghostsHiddenTick) {
                level.ghosts().forEach(Ghost::hide);
            }
            if (isFlashing()) {
                int flashingTick = t - flashingStartTick;
                if (flashingTick > 0 && flashingTick % singleFlashDuration == 0) {
                    flashingIndex += 1;
                    Logger.info("Flashing index -> {} on tick {}", flashingIndex, t);
                }
            }
            ++t;
            Logger.debug("Tick {}: Level complete animation: {} {}",  t,
                    isFlashing() ? "flashing" : "", isInHighlightPhase() ? "highlight" : "");
        }
    }

    public void setFlashingStartTick(int tick) { flashingStartTick = tick; }

    public void setTicksAfterFlashing(int ticks) { ticksAfterFlashing = ticks; }

    public void setGhostsHiddenTick(int tick) { ghostsHiddenTick = tick; }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public boolean isFlashing() {
        return inClosedRange(t, flashingStartTick, flashingEndTick);
    }

    public int flashingIndex() { return flashingIndex; }

    public boolean isInHighlightPhase() {
        return isFlashing() && (t - flashingStartTick) % singleFlashDuration >= singleFlashDuration / 2;
    }
}
