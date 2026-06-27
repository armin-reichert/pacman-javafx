/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.rendering;


public enum GradientAxis {
    /**
     * Left to Right
     */
    HORIZONTAL(1, 0),
    /**
     * Top to Bottom
     */
    VERTICAL(0, 1),
    /**
     * Top-Left to Bottom-Right
     */
    DIAGONAL(1, 1);

    public final int endX() {
        return endX;
    }

    public final int endY() {
        return endY;
    }

    private final byte endX;
    private final byte endY;

    GradientAxis(int endX, int endY) {
        this.endX = (byte) endX;
        this.endY = (byte) endY;
    }
}
