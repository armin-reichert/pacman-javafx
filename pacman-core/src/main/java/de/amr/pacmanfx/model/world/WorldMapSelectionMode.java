/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.world;

public enum WorldMapSelectionMode {
    /**
     * Standard maps only, after all standard maps have been used, in random order.
     */
    NO_CUSTOM_MAPS,
    /**
     * Custom maps in alphabetic order, then standard maps, then random standard maps
     */
    CUSTOM_MAPS_FIRST,
    /**
     * All maps (standard + custom) in random order.
     */
    ALL_RANDOM
}
