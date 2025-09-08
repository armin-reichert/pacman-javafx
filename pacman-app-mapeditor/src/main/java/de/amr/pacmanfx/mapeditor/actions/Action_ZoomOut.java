/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.EditorUI;

import static de.amr.pacmanfx.mapeditor.EditorGlobals.MIN_GRID_SIZE;

public class Action_ZoomOut extends EditorUIAction<Void> {

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
