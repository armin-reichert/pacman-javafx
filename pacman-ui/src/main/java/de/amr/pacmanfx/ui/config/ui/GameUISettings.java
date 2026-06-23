/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config.ui;

public record GameUISettings(
    double flashMessageDuration,
    boolean debugModeOn,
    boolean keyboardMonitorOn,
    boolean muted,
    int numSimulationSteps,
    MiniViewSettings miniView,
    Settings2D d2,
    Settings3D d3)
{}
