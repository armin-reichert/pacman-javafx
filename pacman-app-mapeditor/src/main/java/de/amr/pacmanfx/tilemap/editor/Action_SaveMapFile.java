package de.amr.pacmanfx.tilemap.editor;

import javafx.stage.FileChooser;
import org.tinylog.Logger;

import java.io.File;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.*;

public class Action_SaveMapFile extends AbstractEditorAction {

    private FileChooser createFileChooser(File currentDirectory) {
        var fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(FILTER_WORLD_MAP_FILES, FILTER_ALL_FILES);
        fileChooser.setSelectedExtensionFilter(FILTER_WORLD_MAP_FILES);
        fileChooser.setInitialDirectory(currentDirectory);
        return fileChooser;
    }

    @Override
    public Object execute(TileMapEditor editor) {
        FileChooser fileChooser = createFileChooser(editor.currentDirectory());
        fileChooser.setTitle(translated("save_file"));
        File file = fileChooser.showSaveDialog(editor.stage());
        if (file != null) {
            editor.setCurrentDirectory(file.getParentFile());
            if (file.getName().endsWith(".world")) {
                boolean saved = editor.saveWorldMap(editor.currentWorldMap(), file);
                if (saved) {
                    editor.changeManager().setEdited(false);
                    editor.readMapFile(file);
                    editor.showMessage("Map saved as '%s'".formatted(file.getName()), 3, MessageType.INFO);
                } else {
                    editor.showMessage("Map could not be saved!", 4, MessageType.ERROR);
                }
            } else {
                Logger.error("No .world file selected");
                editor.showMessage("No .world file selected", 2, MessageType.WARNING);
            }
        }
        return null;
    }


}
