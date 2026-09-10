/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;


/**
 * Additional property keys used inside world map files. Values are set at runtime by the map selector.
 */
public enum TengenMsPacMan_GameLevelRendererKey {
    /**
     * Map category. One of ARCADE, MINI, BIG, STRANGE.
     */
    MAP_CATEGORY,
    /**
     * ID of correctly recolored maze sprite set
     */
    MAP_ID,
    /**
     * The map image set (normal + flash images) used by the map renderer.
     */
    MAP_IMAGE_SET,
    /**
     * Boolean value defining if multiple (random) flash colors are used.
     */
    MULTIPLE_FLASH_COLORS,
}
