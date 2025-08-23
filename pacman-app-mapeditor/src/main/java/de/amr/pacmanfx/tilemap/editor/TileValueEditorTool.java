/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.uilib.tilemap.TileMapRenderer;
import javafx.scene.paint.Color;

public class TileValueEditorTool implements Tool {
    private final TileMapEditor editor;
    private final TileMapRenderer renderer;
    private final double size;
    private final byte value;
    private final String description;

    public TileValueEditorTool(TileMapEditor editor, TileMapRenderer renderer, double size, byte value, String description) {
        this.editor = editor;
        this.renderer = renderer;
        this.size = size;
        this.value = value;
        this.description = description;
    }

    @Override
    public TileMapRenderer renderer() {
        return renderer;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public void apply(WorldMap worldMap, LayerID layerID, Vector2i tile) {
        editor.setTileValueRespectSymmetry(worldMap, layerID, tile, value);
    }

    @Override
    public void draw(int row, int col) {
        renderer.ctx().setFill(Color.BLACK);
        renderer.ctx().fillRect(col * size, row * size, size, size);
        renderer.drawTile(new Vector2i(col, row), value);
    }
}
