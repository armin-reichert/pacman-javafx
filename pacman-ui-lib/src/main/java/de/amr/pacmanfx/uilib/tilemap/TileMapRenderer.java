/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.tilemap;

import de.amr.pacmanfx.lib.Vector2i;
import javafx.scene.canvas.GraphicsContext;

public interface TileMapRenderer {
    void setScaling(double scaling);
    void drawTile(GraphicsContext g, Vector2i tile, byte content);
}