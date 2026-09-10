/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets.decorationpane;


public record DecorationPaneConfig(
    float scalingX,
    float scalingY,
    float minScaling,
    float paddingX,
    float paddingY,
    DecorationPaneBorderConfig decorationPaneBorderConfig) {
}
