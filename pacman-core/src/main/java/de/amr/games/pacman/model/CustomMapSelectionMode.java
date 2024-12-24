/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

public enum CustomMapSelectionMode {
    /**
     * Standard maps 1-8 in order, then randomly.
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
