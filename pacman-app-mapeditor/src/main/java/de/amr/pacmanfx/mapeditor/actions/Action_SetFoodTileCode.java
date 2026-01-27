/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.model.world.WorldMap;

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
        worldMap.foodLayer().setContent(tile, code);
        if (editor.symmetricEditMode()) {
            Vector2i mirroredTile = worldMap.foodLayer().mirrorPosition(tile);
            if (canPlaceFoodAtTile(worldMap, mirroredTile)) {
                worldMap.foodLayer().setContent(mirroredTile, code);
            }
        }
        editor.setEdited(true);
        editor.setFoodMapChanged();
        return null;
    }
}