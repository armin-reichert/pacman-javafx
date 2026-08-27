/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.rules;


public record PacDyingTiming(
    int hideGhostsTick,
    int animationStartTick,
    int hidePacTick,
    int pacDeadTick) {
}
