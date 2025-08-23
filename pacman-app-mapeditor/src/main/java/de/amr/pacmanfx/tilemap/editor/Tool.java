/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.uilib.tilemap.TileMapRenderer;

public interface Tool {

    String description();

    void apply(WorldMap worldMap, LayerID layerID, Vector2i tile);

    TileMapRenderer renderer();

    void draw(int row, int col);
}
