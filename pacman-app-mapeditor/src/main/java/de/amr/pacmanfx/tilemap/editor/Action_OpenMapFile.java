package de.amr.pacmanfx.tilemap.editor;

import javafx.stage.FileChooser;

import java.io.File;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.*;

public class Action_OpenMapFile extends AbstractEditorAction {

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
        editor.ifNoUnsavedChangesDo(() -> {
            fileChooser.setTitle(translated("open_file"));
            fileChooser.setInitialDirectory(editor.currentDirectory());
            File file = fileChooser.showOpenDialog(editor.stage());
            if (file != null) {
                editor.readWorldMapFile(file);
            }
            editor.changeManager().setEdited(false);
        });
        return null;
    }
}
