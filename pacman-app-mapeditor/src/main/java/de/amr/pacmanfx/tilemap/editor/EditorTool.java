/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.uilib.tilemap.TileRenderer;

public interface EditorTool {

    String description();

    void apply(TileMapEditor editor, LayerID layerID, Vector2i tile);

    void draw(TileRenderer renderer, int row, int col);
}
