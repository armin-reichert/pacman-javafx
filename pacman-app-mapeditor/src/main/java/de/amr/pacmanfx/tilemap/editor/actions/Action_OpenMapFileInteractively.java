package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.TileMapEditorUI;
import javafx.stage.FileChooser;

import java.io.File;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.*;

public class Action_OpenMapFileInteractively extends AbstractEditorUIAction<File> {

    private File file;

    public Action_OpenMapFileInteractively(TileMapEditorUI ui) {
        super(ui);
    }

    @Override
    public File execute() {
        ui.decideWithCheckForUnsavedChanges(() -> {
            FileChooser fileChooser = createFileChooser();
            file = fileChooser.showOpenDialog(ui.stage());
            if (file != null) {
                new Action_ReplaceCurrentWorldMapChecked(editor, file).execute();
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