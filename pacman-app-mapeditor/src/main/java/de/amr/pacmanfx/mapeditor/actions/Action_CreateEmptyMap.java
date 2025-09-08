/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.mapeditor.TileMapEditor;

public class Action_CreateEmptyMap extends AbstractEditorAction<WorldMap> {

    private final int numRows;
    private final int numCols;

    public Action_CreateEmptyMap(TileMapEditor editor, int numRows, int numCols) {
        super(editor);
        this.numRows = numRows;
        this.numCols = numCols;
    }

    @Override
    public WorldMap execute() {
        var worldMap = WorldMap.emptyMap(numCols, numRows);
        new Action_SetDefaultMapColors(editor, worldMap).execute();
        return worldMap;
    }
}