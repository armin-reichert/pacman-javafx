/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapParseException;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class Action_ReplaceCurrentWorldMapChecked extends EditorUIAction<Boolean> {

    private final File file;

    public Action_ReplaceCurrentWorldMapChecked(TileMapEditorUI ui, File file) {
        super(ui);
        this.file = file;
    }

    @Override
    public Boolean execute() {
        requireNonNull(file);
        boolean success = false;
        if (file.getName().endsWith(".world")) {
            try {
                WorldMap worldMap = WorldMap.loadFromFile(file);
                ui.afterCheckForUnsavedChanges(() -> {
                    editor.setCurrentWorldMap(worldMap);
                    editor.setCurrentDirectory(file.getParentFile());
                    editor.setCurrentFile(file);
                });
                success = true;
            } catch (IOException x) {
                Logger.error("Could not open world map");
                throw new RuntimeException(x);
            }
            catch (WorldMapParseException x) {
                Logger.error("Could not parse world map");
                throw new RuntimeException(x);
            }
        }
        return success;
    }
}
