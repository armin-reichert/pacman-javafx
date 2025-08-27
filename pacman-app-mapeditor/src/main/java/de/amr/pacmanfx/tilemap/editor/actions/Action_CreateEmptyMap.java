package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

public class Action_CreateEmptyMap extends AbstractEditorAction<WorldMap> {

    private final int numRows;
    private final int numCols;

    public Action_CreateEmptyMap(TileMapEditor editor, int numRows, int numCols) {
        super(editor);
        this.numCols = numCols;
        this.numRows = numRows;
    }

    @Override
    public WorldMap execute() {
        var worldMap = WorldMap.emptyMap(numRows, numCols);
        new Action_SetDefaultMapColors(editor, worldMap).execute();
        new Action_SetDefaultScatterPositions(editor, worldMap).execute();
        return worldMap;
    }
}
