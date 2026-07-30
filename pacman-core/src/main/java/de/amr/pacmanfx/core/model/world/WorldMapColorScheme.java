/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.world;

/**
 * UI independent representation of world map colors.
 */
public interface WorldMapColorScheme {
    /** Color inside obstacles walls */
    String wallFill();
    /** Color of obstacle borders */
    String wallStroke();
    /** Color of ghost house doors */
    String door();
    /** Color of pellets and energizers */
    String pellet();
}
