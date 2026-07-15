/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.mapeditor.TileMapEditor;

public class Action_CreateEmptyMap extends EditorAction<WorldMap> {

    private final int numRows;
    private final int numCols;

    public Action_CreateEmptyMap(TileMapEditor editor, int numCols, int numRows) {
        super(editor);
        this.numCols = numCols;
        this.numRows = numRows;
    }

    @Override
    public WorldMap execute() {
        var worldMap = new WorldMap(numCols, numRows);
        new Action_SetDefaultMapColors(editor, worldMap).execute();
        return worldMap;
    }
}