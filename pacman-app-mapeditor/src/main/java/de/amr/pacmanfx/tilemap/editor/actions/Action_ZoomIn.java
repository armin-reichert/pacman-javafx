package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import de.amr.pacmanfx.tilemap.editor.TileMapEditorUI;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.MAX_GRID_SIZE;

public class Action_ZoomIn extends AbstractEditorUIAction<Void> {

    public Action_ZoomIn(TileMapEditorUI ui) {
        super(ui);
    }

    @Override
    public Void execute() {
        if (ui.gridSize() < MAX_GRID_SIZE) {
            ui.setGridSize(ui.gridSize() + 1);
        }
        return null;
    }
}
