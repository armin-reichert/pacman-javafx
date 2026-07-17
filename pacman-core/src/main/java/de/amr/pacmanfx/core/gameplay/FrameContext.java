/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;


public interface FrameContext {

    long tick();

    HuntingStepResult huntingStepResult();
}
