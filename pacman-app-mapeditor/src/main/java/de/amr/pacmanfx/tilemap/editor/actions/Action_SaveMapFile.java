package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.MessageType;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import javafx.stage.FileChooser;
import org.tinylog.Logger;

import java.io.File;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.*;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.saveWorldMap;

public class Action_SaveMapFile extends AbstractEditorAction<Void> {

    public Action_SaveMapFile(TileMapEditor editor) {
        super(editor);
    }

    @Override
    public Void execute() {
        FileChooser fileChooser = createFileChooser();
        File file = fileChooser.showSaveDialog(editor.stage());
        if (file != null) {
            editor.setCurrentDirectory(file.getParentFile());
            if (file.getName().endsWith(".world")) {
                boolean saved = saveWorldMap(editor.currentWorldMap(), file);
                if (saved) {
                    editor.changeManager().setEdited(false);
                    editor.readWorldMapFile(file);
                    editor.messageManager().showMessage("Map saved as '%s'".formatted(file.getName()), 3, MessageType.INFO);
                } else {
                    editor.messageManager().showMessage("Map could not be saved!", 4, MessageType.ERROR);
                }
            } else {
                Logger.error("No .world file selected");
                editor.messageManager().showMessage("No .world file selected", 2, MessageType.WARNING);
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