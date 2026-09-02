/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.ui.gamescene.common.GameSceneComponent;

public class CutSceneTimingComp implements GameSceneComponent {

    private final long animationStartTick;

    private long tick;

    public CutSceneTimingComp(long animationStartTick) {
        this.animationStartTick = animationStartTick;
    }

    public long animationStartTick() {
        return animationStartTick;
    }

    public long tick() {
        return tick;
    }

    public void setTick(long tick) {
        this.tick = tick;
    }
}
