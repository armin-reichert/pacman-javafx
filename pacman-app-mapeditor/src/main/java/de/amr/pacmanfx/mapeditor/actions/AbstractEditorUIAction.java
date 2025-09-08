/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.EditorUI;

import static java.util.Objects.requireNonNull;

public abstract class AbstractEditorUIAction<R> extends AbstractEditorAction<R> {

    protected final EditorUI ui;

    protected AbstractEditorUIAction(EditorUI ui) {
        super(requireNonNull(ui).editor());
        this.ui = ui;
    }
}
