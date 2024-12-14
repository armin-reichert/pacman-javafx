/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.scene;

import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.inClosedRange;

public class LevelCompleteAnimation {

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
    private Runnable onHideGhosts;

    public LevelCompleteAnimation(int numFlashes, int highlightDuration) {
        singleFlashDuration = 2 * highlightDuration;
        flashingEndTick = flashingStartTick + numFlashes * singleFlashDuration - 1;
        lastTick = flashingEndTick + ticksAfterFlashing;
        t = 0;
        flashingIndex = 0;
        running = false;
        Logger.info("Created: flashes={} f-duration={} f-start={} f-after={} total={} index={}",
            numFlashes, 2*highlightDuration, flashingStartTick, ticksAfterFlashing, lastTick + 1, flashingIndex);
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
                if (onHideGhosts != null) onHideGhosts.run();
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

    public void setOnHideGhosts(Runnable onHideGhosts) {
        this.onHideGhosts = onHideGhosts;
    }

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
