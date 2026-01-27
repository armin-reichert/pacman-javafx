/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.model.world.WorldMap;

import static de.amr.pacmanfx.model.world.WorldMapPropertyName.*;
import static java.util.Objects.requireNonNull;

public class Action_SetDefaultScatterPositions extends EditorAction<Void> {

    private final WorldMap worldMap;

    public Action_SetDefaultScatterPositions(TileMapEditor editor) {
        this(editor, editor.currentWorldMap());
    }

    public Action_SetDefaultScatterPositions(TileMapEditor editor, WorldMap worldMap) {
        super(editor);
        this.worldMap = requireNonNull(worldMap);
    }

    @Override
    public Void execute() {
        int numCols = worldMap.numCols(), numRows = worldMap.numRows();
        if (numCols >= 3 && numRows >= 2) {
            worldMap.terrainLayer().propertyMap().put(POS_SCATTER_RED_GHOST,    String.valueOf(Vector2i.of(numCols - 3, 0)));
            worldMap.terrainLayer().propertyMap().put(POS_SCATTER_PINK_GHOST,   String.valueOf(Vector2i.of(2, 0)));
            worldMap.terrainLayer().propertyMap().put(POS_SCATTER_CYAN_GHOST,   String.valueOf(Vector2i.of(numCols - 1, numRows - 2)));
            worldMap.terrainLayer().propertyMap().put(POS_SCATTER_ORANGE_GHOST, String.valueOf(Vector2i.of(0, numRows - 2)));
            editor.setTerrainMapChanged();
        }
        return null;
    }
}