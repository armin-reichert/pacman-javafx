/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.dashboard;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public record DashboardConfig(
    int labelWidth,
    int width,
    Color contentBackground,
    Color textColor,
    Font labelFont,
    Font contentFont) {
}
