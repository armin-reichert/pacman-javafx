package experiments;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;

public class ShapesApp {

    public static void main(String[] args) throws IOException {
        URL url = ShapesApp.class.getResource("/U-shapes.world");
        WorldMap worldMap = new WorldMap(url);
        worldMap.obstacles().forEach(Logger::info);
    }
}
