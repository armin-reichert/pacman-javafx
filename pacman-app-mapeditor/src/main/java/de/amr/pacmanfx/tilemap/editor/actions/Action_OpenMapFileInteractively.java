/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.EditorUI;
import javafx.stage.FileChooser;

import java.io.File;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.*;

public class Action_OpenMapFileInteractively extends AbstractEditorUIAction<File> {

    private File file;

    public Action_OpenMapFileInteractively(EditorUI ui) {
        super(ui);
    }

    @Override
    public File execute() {
        ui.afterCheckForUnsavedChanges(() -> {
            FileChooser fileChooser = createFileChooser();
            file = fileChooser.showOpenDialog(ui.stage());
            if (file != null) {
                new Action_ReplaceCurrentWorldMapChecked(ui, file).execute();
            }
            editor.setEdited(false);
        });
        return file;
    }

    private FileChooser createFileChooser() {
        var fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(FILTER_WORLD_MAP_FILES, FILTER_ALL_FILES);
        fileChooser.setSelectedExtensionFilter(FILTER_WORLD_MAP_FILES);
        fileChooser.setInitialDirectory(editor.currentDirectory());
        fileChooser.setTitle(translated("open_file"));
        return fileChooser;
    }
}