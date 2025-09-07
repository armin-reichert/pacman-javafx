/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.EditorUI;

public class Action_SelectNextPaletteEntry extends AbstractEditorUIAction<Void> {

    public Action_SelectNextPaletteEntry(EditorUI ui) {
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