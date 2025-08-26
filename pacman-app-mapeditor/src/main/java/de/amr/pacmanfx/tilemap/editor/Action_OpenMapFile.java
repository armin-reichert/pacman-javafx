package de.amr.pacmanfx.tilemap.editor;

import javafx.stage.FileChooser;

import java.io.File;

import static de.amr.pacmanfx.tilemap.editor.TileMapEditor.translated;

public class Action_OpenMapFile extends AbstractEditorAction {

    private FileChooser createFileChooser(File currentDirectory) {
        var fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(TileMapEditor.FILTER_WORLD_MAP_FILES, TileMapEditor.FILTER_ALL_FILES);
        fileChooser.setSelectedExtensionFilter(TileMapEditor.FILTER_WORLD_MAP_FILES);
        fileChooser.setInitialDirectory(currentDirectory);
        return fileChooser;
    }

    @Override
    public void execute(TileMapEditor editor) {
        FileChooser fileChooser = createFileChooser(editor.currentDirectory());
        editor.executeWithCheckForUnsavedChanges(() -> {
            fileChooser.setTitle(translated("open_file"));
            fileChooser.setInitialDirectory(editor.currentDirectory());
            File file = fileChooser.showOpenDialog(editor.stage());
            if (file != null) {
                editor.readMapFile(file);
            }
            editor.changeManager().setEdited(false);
        });
    }


}
