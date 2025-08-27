package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import javafx.stage.FileChooser;

import java.io.File;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.*;

public class Action_OpenMapFile extends AbstractEditorAction<File> {

    private File file;

    public Action_OpenMapFile(TileMapEditor editor) {
        super(editor);
    }

    @Override
    public File execute() {
        editor.ifNoUnsavedChangesDo(() -> {
            FileChooser fileChooser = createFileChooser();
            file = fileChooser.showOpenDialog(editor.stage());
            if (file != null) {
                editor.readWorldMapFile(file);
            }
            editor.changeManager().setEdited(false);
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
