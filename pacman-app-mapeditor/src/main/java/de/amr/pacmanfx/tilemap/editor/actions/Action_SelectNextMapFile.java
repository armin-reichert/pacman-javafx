package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import org.tinylog.Logger;

import java.io.File;
import java.util.Arrays;

public class Action_SelectNextMapFile extends AbstractEditorAction<File> {

    private final boolean forward;

    public Action_SelectNextMapFile(TileMapEditor editor, boolean forward) {
        super(editor);
        this.forward = forward;
    }

    @Override
    public File execute() {
        File currentFile = editor.currentFile();
        if (currentFile == null) {
            return null;
        }
        File dir = currentFile.getParentFile();
        if (dir == null) {
            Logger.error("Cannot load next map file for {}, parent is NULL", currentFile);
            return null;
        }
        File[] mapFiles = dir.listFiles((folder, name) -> name.endsWith(".world"));
        if (mapFiles == null) {
            Logger.warn("No map files found in directory {}", dir);
            return null;
        }
        Arrays.sort(mapFiles);
        int index = Arrays.binarySearch(mapFiles, currentFile);
        if (0 <= index && index < mapFiles.length) {
            int next;
            if (forward) {
                next = index == mapFiles.length - 1 ? 0 : index + 1;
            } else {
                next = index > 0 ? index - 1: mapFiles.length - 1;
            }
            File file = mapFiles[next];
            try {
                WorldMap worldMap = WorldMap.fromFile(file);
                editor.setCurrentWorldMap(worldMap);
                editor.setCurrentDirectory(file.getParentFile());
                editor.setCurrentFile(file);
                Logger.info("World map file changed to {}", file);
            } catch (Exception x) {
                Logger.error(x);
                Logger.error("Could not load map file '%s'".formatted(file));
            }
            return file;
        }
        return null;
    }
}
