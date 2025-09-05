package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.EditMode;
import de.amr.pacmanfx.tilemap.editor.EditorUI;

import static de.amr.pacmanfx.tilemap.editor.EditMode.INSPECT;

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
