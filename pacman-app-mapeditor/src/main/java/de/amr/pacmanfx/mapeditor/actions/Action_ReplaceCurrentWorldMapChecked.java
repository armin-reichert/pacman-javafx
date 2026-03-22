/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import de.amr.pacmanfx.model.world.WorldMap;
import org.tinylog.Logger;

import java.io.File;
import java.util.Optional;

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
        if (file.getName().endsWith(".world")) {
            final Optional<WorldMap> worldMap = WorldMap.fromFile(file);
            if (worldMap.isPresent()) {
                ui.afterCheckForUnsavedChanges(() -> {
                    editor.setCurrentWorldMap(worldMap.get());
                    editor.setCurrentDirectory(file.getParentFile());
                    editor.setCurrentFile(file);
                });
                return true;
            }
            else {
                Logger.error("Could not load world map from file '{}'", file);
                return false;
            }
        }
        Logger.error("World map file '{}' has wrong extension", file);
        return false;
    }
}
