/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.TileMapEditorUI;

import static de.amr.pacmanfx.mapeditor.EditorGlobals.MIN_GRID_SIZE;

public class Action_ZoomOut extends EditorUIAction<Void> {

    public Action_ZoomOut(TileMapEditorUI ui) {
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
