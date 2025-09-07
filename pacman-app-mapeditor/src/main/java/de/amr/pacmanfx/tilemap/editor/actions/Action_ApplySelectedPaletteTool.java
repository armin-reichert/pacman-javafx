/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.tilemap.editor.EditorUI;
import de.amr.pacmanfx.tilemap.editor.palette.Palette;

import static java.util.Objects.requireNonNull;

public class Action_ApplySelectedPaletteTool extends AbstractEditorUIAction<Void> {

    private final Palette palette;
    private final Vector2i tile;

    public Action_ApplySelectedPaletteTool(EditorUI ui, Palette palette, Vector2i tile) {
        super(ui);
        this.palette = requireNonNull(palette);
        this.tile = requireNonNull(tile);
    }

    @Override
    public Void execute() {
        palette.selectedTool().ifPresent(paletteTool -> paletteTool.editor().accept(tile));
        return null;
    }
}