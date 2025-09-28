/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.mapeditor.TileMapEditor;

import static de.amr.pacmanfx.mapeditor.EditorUtil.canPlaceFoodAtTile;
import static java.util.Objects.requireNonNull;

/**
 * This action should be used whenever a tile value has to be set and symmetric editing should be taken into account.
 */
public class Action_SetFoodTileCode extends EditorAction<Void> {

    private final Vector2i tile;
    private final byte code;

    public Action_SetFoodTileCode(TileMapEditor editor, Vector2i tile, byte code) {
        super(editor);
        this.tile = requireNonNull(tile);
        this.code = code;
    }

    public Action_SetFoodTileCode(TileMapEditor editor, int row, int col, byte code) {
        this(editor, Vector2i.of(col, row), code);
    }

    @Override
    public Void execute() {
        WorldMap worldMap = editor.currentWorldMap();
        if (!canPlaceFoodAtTile(worldMap, tile)) return null;
        worldMap.setContent(LayerID.FOOD, tile, code);
        if (editor.symmetricEditMode()) {
            Vector2i mirroredTile = worldMap.foodLayer().mirrorPosition(tile);
            if (canPlaceFoodAtTile(worldMap, mirroredTile)) {
                worldMap.setContent(LayerID.FOOD, mirroredTile, code);
            }
        }
        editor.setEdited(true);
        editor.setFoodMapChanged();
        return null;
    }
}