package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.Palette;
import de.amr.pacmanfx.tilemap.editor.TileMapEditorUI;

public class Action_SelectNextPaletteEntry extends AbstractEditorUIAction<Void> {

    public Action_SelectNextPaletteEntry(TileMapEditorUI ui) {
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
