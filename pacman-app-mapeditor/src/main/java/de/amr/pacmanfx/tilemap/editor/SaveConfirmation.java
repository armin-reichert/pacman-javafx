/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.translated;

public class SaveConfirmation extends Alert {
    public static final ButtonType SAVE_CHANGES = new ButtonType(translated("save_changes"));
    public static final ButtonType NO_SAVE_CHANGES = new ButtonType(translated("no_save_changes"));

    public SaveConfirmation() {
        super(AlertType.CONFIRMATION);
        setTitle(translated("save_dialog.title"));
        setHeaderText(translated("save_dialog.header_text"));
        setContentText(translated("save_dialog.content_text"));
        getButtonTypes().setAll(SAVE_CHANGES, NO_SAVE_CHANGES, ButtonType.CANCEL);
    }
}
