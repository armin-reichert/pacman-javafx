package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.TileMapEditorUI;

import static java.util.Objects.requireNonNull;

public abstract class AbstractEditorUIAction<R> extends AbstractEditorAction<R> {

    protected final TileMapEditorUI ui;

    protected AbstractEditorUIAction(TileMapEditorUI ui) {
        super(requireNonNull(ui).editor());
        this.ui = ui;
    }
}
