/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.rendering;

import de.amr.basics.math.Vector2i;
import de.amr.basics.ui.rendering.Renderer;

public interface TileRenderer extends Renderer {
    void drawTile(Vector2i tile, byte content);
}