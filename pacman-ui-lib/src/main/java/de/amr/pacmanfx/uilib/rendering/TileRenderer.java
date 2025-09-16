/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.lib.Vector2i;

public interface TileRenderer extends Renderer {
    void drawTile(Vector2i tile, byte content);
}