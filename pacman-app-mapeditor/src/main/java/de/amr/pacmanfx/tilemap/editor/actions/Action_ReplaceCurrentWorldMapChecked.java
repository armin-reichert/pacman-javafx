package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class Action_ReplaceCurrentWorldMapChecked extends AbstractEditorAction<Boolean> {

    private final File file;

    public Action_ReplaceCurrentWorldMapChecked(TileMapEditor editor, File file) {
        super(editor);
        this.file = file;
    }

    @Override
    public Boolean execute() {
        requireNonNull(file);
        boolean success = false;
        if (file.getName().endsWith(".world")) {
            try {
                WorldMap worldMap = WorldMap.fromFile(file);
                editor.ui().decideWithCheckForUnsavedChanges(() -> {
                    editor.setCurrentWorldMap(worldMap);
                    editor.setCurrentDirectory(file.getParentFile());
                    editor.setCurrentFile(file);
                });
                success = true;
            } catch (IOException x) {
                Logger.error(x);
            }
        }
        return success;
    }
}
