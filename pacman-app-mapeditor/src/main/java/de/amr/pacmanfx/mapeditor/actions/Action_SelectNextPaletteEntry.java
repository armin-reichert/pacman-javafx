/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.TileMapEditorUI;

public class Action_SelectNextPaletteEntry extends EditorUIAction<Void> {

    public Action_SelectNextPaletteEntry(TileMapEditorUI ui) {
        super(ui);
    }

    @Override
    public Void execute() {
        ui.selectedPalette().ifPresent(palette -> {
            int next = palette.selectedToolIndex() + 1;
            if (next == palette.numTools()) {
                palette.setSelectedToolIndex(0);
            } else {
                palette.setSelectedToolIndex(next);
            }
        });
        return null;
    }
}