/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.mapeditor.TileMapEditor;

import static de.amr.pacmanfx.model.DefaultWorldMapPropertyName.*;
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
            worldMap.layer(LayerID.TERRAIN).propertyMap().put(POS_SCATTER_RED_GHOST,    String.valueOf(Vector2i.of(numCols - 3, 0)));
            worldMap.layer(LayerID.TERRAIN).propertyMap().put(POS_SCATTER_PINK_GHOST,   String.valueOf(Vector2i.of(2, 0)));
            worldMap.layer(LayerID.TERRAIN).propertyMap().put(POS_SCATTER_CYAN_GHOST,   String.valueOf(Vector2i.of(numCols - 1, numRows - 2)));
            worldMap.layer(LayerID.TERRAIN).propertyMap().put(POS_SCATTER_ORANGE_GHOST, String.valueOf(Vector2i.of(0, numRows - 2)));
            editor.setTerrainMapChanged();
        }
        return null;
    }
}