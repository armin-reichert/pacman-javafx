/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.palette;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.uilib.rendering.CanvasRenderer;

import java.util.function.BiConsumer;

public interface EditorTool {

    String description();

    BiConsumer<LayerID, Vector2i> editor();

    void draw(CanvasRenderer renderer, int row, int col);
}
