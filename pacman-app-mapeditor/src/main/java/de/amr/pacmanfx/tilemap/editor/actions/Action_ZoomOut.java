package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.MIN_GRID_SIZE;

public class Action_ZoomOut extends AbstractEditorAction<Void> {

    public Action_ZoomOut(TileMapEditor editor) {
        super(editor);
    }

    @Override
    public Void execute() {
        if (editor.gridSize() > MIN_GRID_SIZE) {
            editor.setGridSize(editor.gridSize() - 1);
        }
        return null;
    }
}
