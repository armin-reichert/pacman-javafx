package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.tilemap.editor.AbstractEditorAction;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import org.tinylog.Logger;

import java.io.File;
import java.util.Arrays;

public class Action_SelectNextMapFile extends AbstractEditorAction {

    public void setForward(boolean forward) {
        setArg("forward", forward);
    }

    @Override
    public Object execute(TileMapEditor editor) {
        boolean forward = getArg("forward", Boolean.class);
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
            return mapFiles[next];
        }
        return null;
    }
}
