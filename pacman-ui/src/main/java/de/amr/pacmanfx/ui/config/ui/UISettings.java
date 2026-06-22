/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config.ui;

import javafx.scene.paint.Color;

public record UISettings(
    double flashMessageDuration,
    Color canvasBackgroundColor,
    boolean fontSmoothingOn,
    boolean debugModeOn,
    boolean keyboardMonitorOn,
    boolean muted,
    int numSimulationSteps,
    MiniViewSettings miniView,
    UISettings3D d3)
{}
