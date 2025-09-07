/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tilemap.editor.EditorUI;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class Action_ReplaceCurrentWorldMapChecked extends AbstractEditorUIAction<Boolean> {

    private final File file;

    public Action_ReplaceCurrentWorldMapChecked(EditorUI ui, File file) {
        super(ui);
        this.file = file;
    }

    @Override
    public Boolean execute() {
        requireNonNull(file);
        boolean success = false;
        if (file.getName().endsWith(".world")) {
            try {
                WorldMap worldMap = WorldMap.mapFromFile(file);
                ui.afterCheckForUnsavedChanges(() -> {
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
