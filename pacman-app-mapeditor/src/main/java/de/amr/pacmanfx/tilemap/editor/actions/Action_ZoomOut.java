package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.EditorUI;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.MIN_GRID_SIZE;

public class Action_ZoomOut extends AbstractEditorUIAction<Void> {

    public Action_ZoomOut(EditorUI ui) {
        super(ui);
    }

    @Override
    public Void execute() {
        if (ui.gridSize() > MIN_GRID_SIZE) {
            ui.setGridSize(ui.gridSize() - 1);
        }
        return null;
    }
}
