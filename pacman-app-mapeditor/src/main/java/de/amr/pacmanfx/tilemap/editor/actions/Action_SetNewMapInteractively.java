package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tilemap.editor.EditMode;
import de.amr.pacmanfx.tilemap.editor.EditorUI;
import de.amr.pacmanfx.tilemap.editor.EditorUtil;
import de.amr.pacmanfx.tilemap.editor.MessageType;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.translated;
import static de.amr.pacmanfx.tilemap.editor.EditorUtil.parseMapSize;

public class Action_SetNewMapInteractively extends AbstractEditorUIAction<Void> {

    record ValidationResult(boolean ok, String message) {}

    private static final int MIN_MAP_ROWS = 16;

    private static final ValidationResult VALIDATION_OK = new ValidationResult(true, "");

    private static final ValidationResult VALIDATION_ERR_MAP_FORMAT
        = new ValidationResult(false, "Enter map size as cols x rows e.g. 28x36"); // TODO localize

    private static final ValidationResult VALIDATION_ERR_MAP_TOO_SMALL
        = new ValidationResult(false, "Map must have at least %d rows".formatted(MIN_MAP_ROWS)); // TODO localize

    private static ValidationResult validate(String input) {
        if (input.trim().isBlank()) {
            return VALIDATION_ERR_MAP_FORMAT;
        }
        Vector2i sizeInTiles = parseMapSize(input).orElse(null);
        if (sizeInTiles == null) {
            return VALIDATION_ERR_MAP_FORMAT;
        }
        if (sizeInTiles.y() < MIN_MAP_ROWS) {
            return VALIDATION_ERR_MAP_TOO_SMALL;
        }
        return VALIDATION_OK;
    }

    private static TextInputDialog createSizeInputDialog(EditorUI ui) {
        var dialog = new TextInputDialog("28x36");
        dialog.setTitle(translated("new_dialog.title"));
        dialog.setHeaderText(translated("new_dialog.header_text"));
        dialog.setContentText(translated("new_dialog.content_text"));

        // Keep dialog open until valid input has been entered
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String input = dialog.getEditor().getText();
            ValidationResult validation = validate(input);
            if (!validation.ok()) {
                ui.messageDisplay().showMessage(validation.message(), 2, MessageType.ERROR);
                event.consume(); // Prevent dialog from closing
            }
        });
        return dialog;
    }

    private final boolean preconfigured;
    private final TextInputDialog dialog;

    public Action_SetNewMapInteractively(EditorUI ui, boolean preconfigured) {
        super(ui);
        this.preconfigured = preconfigured;
        this.dialog = createSizeInputDialog(ui);
    }

    @Override
    public Void execute() {
        ui.afterCheckForUnsavedChanges(() -> dialog.showAndWait()
            .flatMap(EditorUtil::parseMapSize)
            .ifPresent(this::createNewMap));

        return null;
    }

    private void createNewMap(Vector2i sizeInTiles) {
        int numCols = sizeInTiles.x(), numRows = sizeInTiles.y();
        WorldMap newMap = preconfigured
            ? new Action_CreatePreconfiguredMap(editor, numRows, numCols).execute()
            : new Action_CreateEmptyMap(editor, numRows, numCols).execute();
        editor.setCurrentWorldMap(newMap);
        editor.setCurrentFile(null);
        editor.setTemplateImage(null);
        if (ui.editModeIs(EditMode.INSPECT)) {
            ui.setEditMode(EditMode.EDIT);
            editor.setSymmetricEditMode(true);
        }
    }
}