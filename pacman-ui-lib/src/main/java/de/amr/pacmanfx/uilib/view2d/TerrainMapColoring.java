/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.view2d;

import javafx.scene.paint.Color;

public record TerrainMapColoring(
    Color floorColor,
    Color wallFillColor,
    Color wallStrokeColor,
    Color doorColor)
{}
