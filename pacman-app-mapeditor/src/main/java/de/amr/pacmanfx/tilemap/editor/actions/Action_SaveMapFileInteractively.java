package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.MessageType;
import de.amr.pacmanfx.tilemap.editor.TileMapEditorUI;
import javafx.stage.FileChooser;
import org.tinylog.Logger;

import java.io.File;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.*;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.saveWorldMap;

public class Action_SaveMapFileInteractively extends AbstractEditorUIAction<Void> {

    public Action_SaveMapFileInteractively(TileMapEditorUI ui) {
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