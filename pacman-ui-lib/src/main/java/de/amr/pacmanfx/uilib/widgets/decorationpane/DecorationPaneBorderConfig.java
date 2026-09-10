/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets.decorationpane;


import javafx.scene.paint.Color;

public record DecorationPaneBorderConfig(
    int arcDiameter,
    int cornerRadius,
    int minBorderWidth,
    double borderWidthRatio,
    Color borderColor) {
}
