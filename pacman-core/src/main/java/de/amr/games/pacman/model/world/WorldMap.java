package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Stream;

public class WorldMap {

    static final String TERRAIN_SECTION_START = "!terrain";
    static final String FOOD_SECTION_START    = "!food";

    private TileMap terrain;
    private TileMap food;

    public static WorldMap copyOf(WorldMap other) {
        return new WorldMap(TileMap.copyOf(other.terrain), TileMap.copyOf(other.food));
    }

    public WorldMap(TileMap terrain, TileMap food) {
        this.terrain = terrain;
        this.food = food;
    }

    public WorldMap(URL url) throws IOException {
        var r = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
        parse(r.lines());
    }

    public WorldMap(Stream<String> lines) {
        parse(lines);
    }

    private void parse(Stream<String> lines) {
        var terrainSection = new ArrayList<String>();
        var foodSection = new ArrayList<String>();
        boolean inTerrainSection = false, inFoodSection = false;
        for (var line : lines.toList()) {
            if (TERRAIN_SECTION_START.equals(line)) {
                inTerrainSection = true;
            } else if (FOOD_SECTION_START.equals(line)) {
                inTerrainSection = false;
                inFoodSection = true;
            } else if (inTerrainSection) {
                terrainSection.add(line);
            } else if (inFoodSection) {
                foodSection.add(line);
            } else {
                Logger.error("Line skipped: '{}'", line);
            }
        }
        terrain = TileMap.parse(terrainSection, Tiles.TERRAIN_TILES_END);
        food = TileMap.parse(foodSection, Tiles.FOOD_TILES_END);
    }

    public int numRows() {
        return terrain.numRows();
    }

    public int numCols() {
        return terrain.numCols();
    }

    public Stream<Vector2i> tiles() {
        return terrain.tiles();
    }

    public TileMap terrain() {
        return terrain;
    }

    public byte terrain(Vector2i tile) {
        return terrain.get(tile);
    }

    public byte terrain(int row, int col) {
        return terrain.get(row, col);
    }

    public TileMap food() {
        return food;
    }

    public byte food(Vector2i tile) {
        return food.get(tile);
    }

    public byte food(int row, int col) {
        return food.get(row, col);
    }

    public void save(File file) {
        try (FileWriter w = new FileWriter(file, StandardCharsets.UTF_8)) {
            w.write("!terrain\r\n");
            terrain.write(w);
            w.write("!food\r\n");
            food.write(w);
            Logger.info("World map saved to file '{}'.", file);
        } catch (Exception x) {
            Logger.error(x);
        }
    }
}
