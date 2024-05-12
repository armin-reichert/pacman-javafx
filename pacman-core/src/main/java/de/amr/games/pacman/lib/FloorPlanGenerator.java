package de.amr.games.pacman.lib;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.world.FloorPlan;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FloorPlanGenerator {

    static final String PACMAN_PATTERN    = "fp-pacman-map-%d-res-%d.txt";
    static final String MS_PACMAN_PATTERN = "fp-mspacman-map-%d-res-%d.txt";
    static final File DIR = new File(System.getProperty("user.dir"), "tmp");

    public static void main(String[] args) {
        try {
            Files.createDirectories(Paths.get(DIR.toURI()));
            Logger.info("Writing floorplans to output directory {}", DIR);
        } catch (Exception x) {
            Logger.error("Could not create output directory {}", DIR);
            return;
        }
        storeFloorPlan(GameVariant.PACMAN.createWorld(1), 1, 4, PACMAN_PATTERN);
        for (int mapNumber = 1; mapNumber <= 4; ++mapNumber) {
            storeFloorPlan(GameVariant.MS_PACMAN.createWorld(mapNumber), mapNumber, 4, MS_PACMAN_PATTERN);
        }
    }

    private static void storeFloorPlan(World world, int mapNumber, int resolution, String namePattern) {
        var floorPlan = new FloorPlan(world, world.numCols() * resolution, world.numRows() * resolution, resolution);
        String name = namePattern.formatted(mapNumber, resolution);
        File file = new File(DIR, name);
        floorPlan.write(file);
        Logger.info("Floorplan written to {}", file);
    }
}