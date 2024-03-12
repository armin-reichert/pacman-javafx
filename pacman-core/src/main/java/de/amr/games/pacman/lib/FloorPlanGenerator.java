package de.amr.games.pacman.lib;

import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.FloorPlan;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FloorPlanGenerator {

    static final File DIR = new File(System.getProperty("user.dir"), "tmp");

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Paths.get(DIR.toURI()));
        int resolution = 4;
        var world = ArcadeWorld.createPacManWorld();
        var floorPlan = new FloorPlan(world, world.numCols() * resolution, world.numRows() * resolution, resolution);
        File file = new File(DIR, "pacman-floorPlan.txt");
        floorPlan.store(file);
        var floorPlan2 = FloorPlan.read(new FileInputStream(file));
        Logger.info("Floor plan has been read");
    }
}
