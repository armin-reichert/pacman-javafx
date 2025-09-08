/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.EditorAction;
import de.amr.pacmanfx.mapeditor.TileMapEditor;

import static java.util.Objects.requireNonNull;

public abstract class AbstractEditorAction<R> implements EditorAction<R> {

    protected final TileMapEditor editor;

    protected AbstractEditorAction(TileMapEditor editor) {
        this.editor = requireNonNull(editor);
    }
}
