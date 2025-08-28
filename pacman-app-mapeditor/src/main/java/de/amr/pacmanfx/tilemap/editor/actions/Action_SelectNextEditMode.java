package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.EditMode;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

public class Action_SelectNextEditMode extends AbstractEditorAction<Void> {

    public Action_SelectNextEditMode(TileMapEditor editor) {
        super(editor);
    }

    @Override
    public Void execute() {
        switch (editor.editMode()) {
            case INSPECT -> {
                editor.setEditMode(EditMode.EDIT);
                editor.setSymmetricEditMode(false);
            }
            case EDIT -> {
                if (editor.symmetricEditMode()) {
                    editor.setEditMode(EditMode.ERASE);
                } else {
                    editor.setSymmetricEditMode(true);
                }
            }
            case ERASE -> editor.setEditMode(EditMode.INSPECT);
        }
        return null;
    }
}
