/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.TileMapEditorUI;

public class Action_QuitEditor extends EditorUIAction<Void> {

    public Action_QuitEditor(TileMapEditorUI ui) {
        super(ui);
    }

    @Override
    public Void execute() {
        ui.editor().quit();
        return null;
    }
}
