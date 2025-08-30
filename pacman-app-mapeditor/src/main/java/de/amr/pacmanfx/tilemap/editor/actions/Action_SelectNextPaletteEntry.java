package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.Palette;
import de.amr.pacmanfx.tilemap.editor.EditorUI;

public class Action_SelectNextPaletteEntry extends AbstractEditorUIAction<Void> {

    public Action_SelectNextPaletteEntry(EditorUI ui) {
        super(ui);
    }

    @Override
    public Void execute() {
        Palette palette = ui.selectedPalette();
        int next = palette.selectedIndex() + 1;
        if (next == palette.numTools()) { next = 0; }
        palette.selectTool(next);
        return null;
    }
}
