/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.palette;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.mapeditor.actions.Action_SetFoodTileCode;
import de.amr.pacmanfx.mapeditor.actions.Action_SetTerrainTileCode;
import de.amr.pacmanfx.model.world.WorldMapLayerID;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import de.amr.pacmanfx.uilib.rendering.TileRenderer;

import java.util.function.Consumer;

public class TileCodeEditorTool implements PaletteTool {

    private final byte code;
    private final String description;

    private final Consumer<Vector2i> tileEditor;

    public TileCodeEditorTool(TileMapEditor editor, WorldMapLayerID layerID, byte code, String description) {
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
    public void draw(Renderer renderer, int row, int col) {
        if (renderer instanceof TileRenderer tileRenderer) {
            tileRenderer.drawTile(new Vector2i(col, row), code);
        }
    }
}