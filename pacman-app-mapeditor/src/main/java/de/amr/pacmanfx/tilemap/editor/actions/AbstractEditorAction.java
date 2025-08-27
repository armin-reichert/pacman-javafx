package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.EditorAction;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

import static java.util.Objects.requireNonNull;

public abstract class AbstractEditorAction<R> implements EditorAction<R> {

    protected final TileMapEditor editor;

    protected AbstractEditorAction(TileMapEditor editor) {
        this.editor = requireNonNull(editor);
    }
}
