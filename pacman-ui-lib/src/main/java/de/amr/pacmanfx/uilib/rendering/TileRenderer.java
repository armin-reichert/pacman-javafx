/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.lib.math.Vector2i;

public interface TileRenderer extends Renderer {
    void drawTile(Vector2i tile, byte content);
}