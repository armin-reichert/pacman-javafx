/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.settings.ui;

import java.util.List;

public record GameUISettings(
    double flashMessageDuration,
    boolean testStatesIncluded,
    boolean debugModeOn,
    boolean keyboardMonitorOn,
    boolean muted,
    List<DashboardSectionSettings> dashboard,
    MiniViewSettings miniView,
    Game2DSettings common2D,
    Game3DSettings common3D)
{}
