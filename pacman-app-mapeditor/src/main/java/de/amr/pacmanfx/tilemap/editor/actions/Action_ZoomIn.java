package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.MAX_GRID_SIZE;

public class Action_ZoomIn extends AbstractEditorAction<Void> {

    public Action_ZoomIn(TileMapEditor editor) {
        super(editor);
    }

    @Override
    public Void execute() {
        if (editor.gridSize() < MAX_GRID_SIZE) {
            editor.setGridSize(editor.gridSize() + 1);
        }
        return null;
    }
}
