/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.palette;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.uilib.rendering.Renderer;

import java.util.function.Consumer;

public interface PaletteTool {

    /**
     * @return descriptive text, displayed e.g. as a tooltip
     */
    String description();

    /**
     * @return function that realizes the editing of the current tile
     */
    Consumer<Vector2i> editor();

    /**
     * Draws the palette tile.
     *
     * @param renderer renderer used for drawing the tile
     * @param row grid row of the tile in the palette
     * @param col grid column of the tile in the palette
     */
    void draw(Renderer renderer, int row, int col);
}
