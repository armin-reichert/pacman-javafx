package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.Palette;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

public class Action_SelectNextPaletteEntry extends AbstractEditorAction<Void> {

    public Action_SelectNextPaletteEntry(TileMapEditor editor) {
        super(editor);
    }

    @Override
    public Void execute() {
        Palette palette = editor.ui().selectedPalette();
        int next = palette.selectedIndex() + 1;
        if (next == palette.numTools()) { next = 0; }
        palette.selectTool(next);
        return null;
    }
}
