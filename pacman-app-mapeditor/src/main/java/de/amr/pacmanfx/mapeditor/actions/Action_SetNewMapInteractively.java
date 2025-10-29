/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.mapeditor.EditMode;
import de.amr.pacmanfx.mapeditor.SizeInputDialog;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;

import static de.amr.pacmanfx.mapeditor.EditorGlobals.translated;

public class Action_SetNewMapInteractively extends EditorUIAction<Void> {

    private static final int MIN_NUM_COLS = 10;
    private static final int MAX_NUM_COLS = 100;
    private static final int DEFAULT_NUM_COLS = 28;

    private static final int MIN_NUM_ROWS = 16;
    private static final int MAX_NUM_ROWS = 100;
    private static final int DEFAULT_NUM_ROWS = 36;

    private final boolean preconfigured;

    public Action_SetNewMapInteractively(TileMapEditorUI ui, boolean preconfigured) {
        super(ui);
        this.preconfigured = preconfigured;
    }

    @Override
    public Void execute() {
        var dialog = new SizeInputDialog(
            MIN_NUM_COLS, MAX_NUM_COLS, DEFAULT_NUM_COLS,
            MIN_NUM_ROWS, MAX_NUM_ROWS, DEFAULT_NUM_ROWS);
        dialog.setTitle(translated("new_dialog.title"));
        dialog.setHeaderText(translated("new_dialog.header_text"));
        dialog.setContentText(translated("new_dialog.content_text"));
        ui.afterCheckForUnsavedChanges(() -> dialog.showAndWait().ifPresent(this::createNewMap));
        return null;
    }

    private void createNewMap(Vector2i sizeInTiles) {
        int numCols = sizeInTiles.x(), numRows = sizeInTiles.y();
        WorldMap newMap = preconfigured
            ? new Action_CreatePreconfiguredMap(editor, numCols, numRows).execute()
            : new Action_CreateEmptyMap(editor, numCols, numRows).execute();
        editor.setCurrentWorldMap(newMap);
        editor.setCurrentFile(null);
        editor.setTemplateImage(null);
        if (ui.editModeIs(EditMode.INSPECT)) {
            ui.setEditMode(EditMode.EDIT);
            editor.setSymmetricEditMode(true);
        }
    }
}