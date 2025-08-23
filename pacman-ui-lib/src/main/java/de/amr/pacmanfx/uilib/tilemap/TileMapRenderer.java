/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.tilemap;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.uilib.rendering.CanvasRenderer;

public interface TileMapRenderer extends CanvasRenderer {
    void drawTile(Vector2i tile, byte content);
}