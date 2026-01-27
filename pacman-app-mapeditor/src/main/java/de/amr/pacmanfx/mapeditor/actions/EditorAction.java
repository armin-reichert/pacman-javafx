/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.TileMapEditor;

import static java.util.Objects.requireNonNull;

/**
 * Editor action base class.
 *
 * @param <R> return type of action execution
 */
public abstract class EditorAction<R> {

    protected final TileMapEditor editor;

    protected EditorAction(TileMapEditor editor) {
        this.editor = requireNonNull(editor);
    }

    public abstract R execute();
}
