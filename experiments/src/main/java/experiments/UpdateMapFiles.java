package experiments;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

public class UpdateMapFiles {

    public static void main(String[] args) {
        if (args.length == 0) {
            Logger.info("Usage: UpdateMapFiles <directory>");
            return;
        }
        updateMapFiles(new File(args[0]));
    }

    public static void updateMapFiles(File directory) {
        File[] mapFiles = directory.listFiles((File dir, String name) -> name.endsWith(".world"));
        if (mapFiles == null) {
            Logger.info("No map files found in directory {}", directory);
            return;
        }
        for (File mapFile : mapFiles) {
            try {
                WorldMap map = new WorldMap(mapFile);
                map.save(mapFile);
                Logger.info("Updated map file {}", mapFile);
            } catch (IOException x) {
                Logger.error(x);
            }
        }
    }

}
