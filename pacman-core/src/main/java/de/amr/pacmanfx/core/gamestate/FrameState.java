/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.gameplay.hunt.GamePlayStep;

public record FrameState(long tick, GamePlayStep gamePlayStep) {}
