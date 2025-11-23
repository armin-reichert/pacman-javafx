/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import de.amr.pacmanfx.mapeditor.palette.Palette;

import static java.util.Objects.requireNonNull;

public class Action_ApplySelectedPaletteTool extends EditorUIAction<Void> {

    private final Palette palette;
    private final Vector2i tile;

    public Action_ApplySelectedPaletteTool(TileMapEditorUI ui, Palette palette, Vector2i tile) {
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