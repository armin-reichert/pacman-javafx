/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.palette;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.mapeditor.actions.Action_SetFoodTileCode;
import de.amr.pacmanfx.mapeditor.actions.Action_SetTerrainTileCode;
import de.amr.pacmanfx.uilib.rendering.CanvasRenderer;
import de.amr.pacmanfx.uilib.tilemap.TileRenderer;

import java.util.function.Consumer;

public class TileCodeEditorTool implements PaletteTool {

    private final byte code;
    private final String description;

    private final Consumer<Vector2i> tileEditor;

    public TileCodeEditorTool(TileMapEditor editor, LayerID layerID, byte code, String description) {
        this.code = code;
        this.description = description;
        tileEditor = switch (layerID) {
            case TERRAIN -> tile -> new Action_SetTerrainTileCode(editor, tile, code).execute();
            case FOOD -> tile -> new Action_SetFoodTileCode(editor, tile, code).execute();
        };
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public Consumer<Vector2i> editor() {
        return tileEditor;
    }

    @Override
    public void draw(CanvasRenderer renderer, int row, int col) {
        if (renderer instanceof TileRenderer tileRenderer) {
            tileRenderer.drawTile(new Vector2i(col, row), code);
        }
    }
}