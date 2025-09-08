/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.EditMode;
import de.amr.pacmanfx.mapeditor.EditorUI;

import static de.amr.pacmanfx.mapeditor.EditMode.INSPECT;

public class Action_SelectNextEditMode extends AbstractEditorUIAction<Void> {

    public Action_SelectNextEditMode(EditorUI ui) {
        super(ui);
    }

    @Override
    public Void execute() {
        switch (ui.editMode()) {
            case INSPECT -> {
                ui.setEditMode(EditMode.EDIT);
                editor.setSymmetricEditMode(false);
            }
            case EDIT -> {
                if (editor.symmetricEditMode()) {
                    ui.setEditMode(EditMode.ERASE);
                } else {
                    editor.setSymmetricEditMode(true);
                }
            }
            case ERASE -> ui.setEditMode(INSPECT);
        }
        return null;
    }
}
