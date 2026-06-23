/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config.ui;

public record UISettings(
    double flashMessageDuration,
    boolean debugModeOn,
    boolean keyboardMonitorOn,
    boolean muted,
    int numSimulationSteps,
    MiniViewSettings miniView,
    UISettings2D d2,
    UISettings3D d3)
{}
