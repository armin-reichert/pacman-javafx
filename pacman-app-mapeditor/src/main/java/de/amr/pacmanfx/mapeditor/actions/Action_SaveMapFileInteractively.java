/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.EditorUI;
import de.amr.pacmanfx.mapeditor.MessageType;
import javafx.stage.FileChooser;
import org.tinylog.Logger;

import java.io.File;

import static de.amr.pacmanfx.mapeditor.EditorGlobals.*;
import static de.amr.pacmanfx.mapeditor.EditorUtil.saveWorldMap;

public class Action_SaveMapFileInteractively extends EditorUIAction<Void> {

    public Action_SaveMapFileInteractively(EditorUI ui) {
        super(ui);
    }

    @Override
    public Void execute() {
        FileChooser fileChooser = createFileChooser();
        File file = fileChooser.showSaveDialog(ui.stage());
        if (file != null) {
            editor.setCurrentDirectory(file.getParentFile());
            if (file.getName().endsWith(".world")) {
                boolean saveSuccess = saveWorldMap(editor.currentWorldMap(), file);
                if (saveSuccess) {
                    editor.setEdited(false);
                    boolean replaceSuccess = new Action_ReplaceCurrentWorldMapChecked(ui, file).execute();
                    if (replaceSuccess) {
                        ui.messageDisplay().showMessage("Map saved as '%s'".formatted(file.getName()), 3, MessageType.INFO);
                    } else {
                        ui.messageDisplay().showMessage("Current map could not be replaced!", 4, MessageType.ERROR);
                    }
                } else {
                    ui.messageDisplay().showMessage("Map could not be saved!", 4, MessageType.ERROR);
                }
            } else {
                Logger.error("No .world file selected");
                ui.messageDisplay().showMessage("No .world file selected", 2, MessageType.WARNING);
            }
        }
        return null;
    }

    private FileChooser createFileChooser() {
        var fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(FILTER_WORLD_MAP_FILES, FILTER_ALL_FILES);
        fileChooser.setSelectedExtensionFilter(FILTER_WORLD_MAP_FILES);
        fileChooser.setInitialDirectory(editor.currentDirectory());
        fileChooser.setTitle(translated("save_file"));
        return fileChooser;
    }
}