/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.rendering;

import javafx.scene.paint.Color;

public record TerrainMapColorScheme(
    Color backgroundColor,
    Color wallFillColor,
    Color wallStrokeColor,
    Color doorColor
) {}
