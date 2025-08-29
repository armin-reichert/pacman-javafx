package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.EditMode;
import de.amr.pacmanfx.tilemap.editor.TileMapEditorUI;

import static de.amr.pacmanfx.tilemap.editor.EditMode.INSPECT;

public class Action_SelectNextEditMode extends AbstractEditorUIAction<Void> {

    public Action_SelectNextEditMode(TileMapEditorUI ui) {
        super(ui);
    }

    @Override
    public Void execute() {
        switch (ui.editMode()) {
            case INSPECT -> {
                ui.setEditMode(EditMode.EDIT);
                ui.setSymmetricEditMode(false);
            }
            case EDIT -> {
                if (ui.symmetricEditMode()) {
                    ui.setEditMode(EditMode.ERASE);
                } else {
                    ui.setSymmetricEditMode(true);
                }
            }
            case ERASE -> ui.setEditMode(INSPECT);
        }
        return null;
    }
}
