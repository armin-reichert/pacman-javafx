/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.uilib.tilemap.TileRenderer;

import java.util.function.BiConsumer;

public interface EditorTool {

    String description();

    BiConsumer<LayerID, Vector2i> editor();

    void draw(TileRenderer renderer, int row, int col);
}
