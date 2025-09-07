package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tilemap.editor.EditMode;
import de.amr.pacmanfx.tilemap.editor.EditorUI;
import de.amr.pacmanfx.tilemap.editor.MessageType;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.translated;
import static de.amr.pacmanfx.tilemap.editor.EditorUtil.parseSize;

public class Action_CreateNewMapInteractively extends AbstractEditorUIAction<Void> {

    private final boolean preconfigured;
    private final TextInputDialog dialog;
    private String errorMessage;

    public Action_CreateNewMapInteractively(EditorUI ui, boolean preconfigured) {
        super(ui);
        this.preconfigured = preconfigured;

        dialog = new TextInputDialog("28x36");
        dialog.setTitle(translated("new_dialog.title"));
        dialog.setHeaderText(translated("new_dialog.header_text"));
        dialog.setContentText(translated("new_dialog.content_text"));

        // Keep dialog open until valid input has been entered
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String input = dialog.getEditor().getText();
            if (!isValidInput(input)) {
                ui.messageDisplay().showMessage(errorMessage, 2, MessageType.ERROR);
                event.consume(); // Prevent dialog from closing
            }
        });
    }

    @Override
    public Void execute() {
        ui.afterCheckForUnsavedChanges(this::openSizeInputDialog);
        return null;
    }

    private void openSizeInputDialog() {
        dialog.showAndWait().ifPresent(input -> {
            Vector2i sizeInTiles = parseSize(input).orElse(null);
            if (sizeInTiles != null) {
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
        });
    }

    private boolean isValidInput(String input) {
        errorMessage = null;
        if (input.trim().isBlank()) {
            errorMessage = "Enter map size as cols x rows e.g. 28x36"; // TODO localize
            return false;
        }
        Vector2i sizeInTiles = parseSize(input).orElse(null);
        if (sizeInTiles == null) {
            errorMessage = "Enter map size as cols x rows e.g. 28x36"; //TODO localize
            return false;
        }
        else if (sizeInTiles.y() < 6) {
            errorMessage = "Map must have at least 6 rows"; // TODO localize
            return false;
        }
        return true;
    }
}