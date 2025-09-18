/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.TileMapEditorUI;

import static java.util.Objects.requireNonNull;

public abstract class EditorUIAction<R> extends EditorAction<R> {

    protected final TileMapEditorUI ui;

    protected EditorUIAction(TileMapEditorUI ui) {
        super(requireNonNull(ui).editor());
        this.ui = ui;
    }
}
