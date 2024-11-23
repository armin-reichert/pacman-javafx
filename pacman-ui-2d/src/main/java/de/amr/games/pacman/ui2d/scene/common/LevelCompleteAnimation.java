/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.common;

import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.inRange;

public class LevelCompleteAnimation {

    private int ticksBeforeFlashing = 120;
    private int ticksAfterFlashing = 60;
    private int tickGhostsHidden = 90;
    private final int highlightPhaseDuration;
    private final int flashingStartTick;
    private final int flashingEndTick;
    private final int lastTick;

    private int t;
    private boolean running;
    private Runnable onFinished = () -> {
    };
    private Runnable onHideGhosts = () -> {
    };

    public LevelCompleteAnimation(int numFlashes, int highlightPhaseDuration) {
        this.highlightPhaseDuration = highlightPhaseDuration;
        flashingStartTick = ticksBeforeFlashing + 1;
        int totalFlashingDuration = numFlashes * 2 * highlightPhaseDuration;
        flashingEndTick = flashingStartTick + totalFlashingDuration;
        lastTick = ticksBeforeFlashing + totalFlashingDuration + ticksAfterFlashing - 1;
        t = 0;
        running = false;
        Logger.info("Level complete animation created, num flashes={} total ticks={}", numFlashes, lastTick + 1);
    }

    public void start() {
        running = true;
    }

    public void setTicksBeforeFlashing(int ticksBeforeFlashing) {
        this.ticksBeforeFlashing = ticksBeforeFlashing;
    }

    public void setTicksAfterFlashing(int ticksAfterFlashing) {
        this.ticksAfterFlashing = ticksAfterFlashing;
    }

    public void setTickGhostsHidden(int tickGhostsHidden) {
        this.tickGhostsHidden = tickGhostsHidden;
    }

    public void setOnHideGhosts(Runnable onHideGhosts) {
        this.onHideGhosts = onHideGhosts;
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public boolean isFlashing() {
        return inRange(t, flashingStartTick, flashingEndTick);
    }

    public boolean isHighlightMaze() {
        return isFlashing() && (t - flashingStartTick) % (2 * highlightPhaseDuration) >= highlightPhaseDuration;
    }

    public void update() {
        if (running) {
            if (t == lastTick) {
                running = false;
                onFinished.run();
                return;
            }
            if (t == tickGhostsHidden) {
                onHideGhosts.run();
            }
            ++t;
            Logger.debug("Level complete animation: tick {}: {} {}",
                t, isFlashing() ? "flashing" : "", isHighlightMaze() ? "highlight" : "");
        }
    }
}
