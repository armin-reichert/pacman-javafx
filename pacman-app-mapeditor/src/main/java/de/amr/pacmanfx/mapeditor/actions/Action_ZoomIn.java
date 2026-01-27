/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.TileMapEditorUI;

import static de.amr.pacmanfx.mapeditor.EditorGlobals.MAX_GRID_SIZE;

public class Action_ZoomIn extends EditorUIAction<Void> {

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
