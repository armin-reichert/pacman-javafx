/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib;

import javafx.scene.paint.Color;

/**
 * These colors are taken from the MAME palettes.
 */
public enum ArcadeColor {
    BLACK  (0, 0, 0),
    BLUE   (33, 33, 255),
    BROWN  (222, 151, 81),
    CYAN   (0, 255, 255),
    ORANGE (255, 183, 81),
    PINK   (255, 183, 255),
    RED    (255, 0, 0),
    ROSE   (255, 183, 174),
    WHITE  (222, 222, 255),
    YELLOW (255, 255, 0);

    ArcadeColor(int r, int g, int b) {
        color = Color.rgb(r, g, b);
    }

    /**
     * Overridden to return a string that can be parsed by {@link Color#valueOf(String)}.
     *
     * @return String representation of RGB color of this palette entry
     */
    @Override
    public String toString() {
        return color.toString();
    }

    public Color color() {
        return color;
    }

    private final Color color;
}