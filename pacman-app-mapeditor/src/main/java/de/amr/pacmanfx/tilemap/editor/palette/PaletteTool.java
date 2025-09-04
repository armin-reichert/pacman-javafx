/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.palette;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.uilib.rendering.CanvasRenderer;

import java.util.function.BiConsumer;

public interface PaletteTool {

    /**
     * @return descriptive text, displayed e.g. as a tooltip
     */
    String description();

    /**
     * @return function that realizes the editing of the current tile
     */
    BiConsumer<LayerID, Vector2i> editor();

    /**
     * Draws the palette tile.
     *
     * @param renderer renderer used for drawing the tile
     * @param row grid row of the tile in the palette
     * @param col grid column of the tile in the palette
     */
    void draw(CanvasRenderer renderer, int row, int col);
}
