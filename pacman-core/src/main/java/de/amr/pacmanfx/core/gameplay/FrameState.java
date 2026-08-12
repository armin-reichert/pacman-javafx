/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;

import de.amr.pacmanfx.core.gameplay.hunt.HuntingStep;

public record FrameState(long tick, HuntingStep huntingStep) {}
