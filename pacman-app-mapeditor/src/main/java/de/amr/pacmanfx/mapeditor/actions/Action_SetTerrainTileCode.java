/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.FoodTile;
import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.TerrainTile;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.mapeditor.TileMapEditor;

import static java.util.Objects.requireNonNull;

/**
 * This action should be used whenever a tile value has to be set and symmetric editing should be taken into account.
 */
public class Action_SetTerrainTileCode extends EditorAction<Void> {

    private final WorldMap worldMap;
    private final Vector2i tile;
    private final byte code;

    public Action_SetTerrainTileCode(TileMapEditor editor, Vector2i tile, byte code) {
        this(editor, editor.currentWorldMap(), tile, code);
    }

    public Action_SetTerrainTileCode(TileMapEditor editor, int row, int col, byte code) {
        this(editor, Vector2i.of(col, row), code);
    }

    public Action_SetTerrainTileCode(TileMapEditor editor, WorldMap worldMap, Vector2i tile, byte code) {
        super(editor);
        this.worldMap = requireNonNull(worldMap);
        this.tile = requireNonNull(tile);
        this.code = code;
    }

    public Action_SetTerrainTileCode(TileMapEditor editor, WorldMap worldMap, int row, int col, byte code) {
        this(editor, worldMap, new Vector2i(col, row), code);
    }

    @Override
    public Void execute() {
        byte oldCode = worldMap.terrainLayer().get(tile);
        if (code == oldCode && !editor.symmetricEditMode()) return null;
        worldMap.setContent(LayerID.TERRAIN, tile, code);
        worldMap.setContent(LayerID.FOOD, tile, FoodTile.EMPTY.$);
        if (editor.symmetricEditMode()) {
            Vector2i mirroredTile = worldMap.mirrorPosition(tile);
            byte mirroredCode = TerrainTile.mirroredCode(code);
            byte oldMirroredCode = worldMap.terrainLayer().get(mirroredTile);
            if (mirroredCode != oldMirroredCode) {
                worldMap.setContent(LayerID.TERRAIN, mirroredTile, mirroredCode);
                worldMap.setContent(LayerID.FOOD, mirroredTile, FoodTile.EMPTY.$);
            }
        }
        editor.setEdited(true);
        editor.setWorldMapChanged();

        return null;
    }
}