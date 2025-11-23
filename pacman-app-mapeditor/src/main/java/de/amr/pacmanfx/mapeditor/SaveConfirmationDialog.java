/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import static de.amr.pacmanfx.mapeditor.EditorGlobals.translated;

public class SaveConfirmationDialog extends Alert {
    public static final ButtonType SAVE = new ButtonType(translated("save_changes"));
    public static final ButtonType DONT_SAVE = new ButtonType(translated("no_save_changes"));

    public SaveConfirmationDialog() {
        super(AlertType.CONFIRMATION);
        setTitle(translated("save_dialog.title"));
        setHeaderText(translated("save_dialog.header_text"));
        setContentText(translated("save_dialog.content_text"));
        getButtonTypes().setAll(SAVE, DONT_SAVE, ButtonType.CANCEL);
    }
}