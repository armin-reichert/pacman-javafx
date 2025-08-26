/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.uilib.tilemap.TileRenderer;
import javafx.scene.paint.Color;

public class TileValueEditorTool implements TileMapEditorTool {
    private final double size;
    private final byte value;
    private final String description;

    public TileValueEditorTool(double size, byte value, String description) {
        this.size = size;
        this.value = value;
        this.description = description;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public void apply(TileMapEditor editor, LayerID layerID, Vector2i tile) {
        editor.setTileValueRespectSymmetry(editor.editedWorldMap(), layerID, tile, value);
    }

    @Override
    public void draw(TileRenderer renderer, int row, int col) {
        renderer.ctx().setFill(Color.BLACK);
        renderer.ctx().fillRect(col * size, row * size, size, size);
        renderer.drawTile(new Vector2i(col, row), value);
    }
}
