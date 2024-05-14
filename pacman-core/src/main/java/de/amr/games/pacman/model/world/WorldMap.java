package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class WorldMap {

    public static void main(String[] args) throws IOException {
        URL url = WorldMap.class.getResource("/maps/pacman.world");
        if (url != null) {
            var worldMap = new WorldMap(url);
            System.err.println(worldMap.terrainMap().numRows());
            System.err.println(worldMap.foodMap().numRows());
        }
    }

    private final TileMap terrainMap;
    private final TileMap foodMap;

    public static WorldMap copy(WorldMap other) {
        return new WorldMap(TileMap.copy(other.terrainMap), TileMap.copy(other.foodMap));
    }

    public WorldMap(TileMap terrainMap, TileMap foodMap) {
        this.terrainMap = terrainMap;
        this.foodMap = foodMap;
    }

    public WorldMap(URL url) throws IOException {
        this(new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)));
        Logger.info("World map read successfully, url={}", url);
    }

    public WorldMap(BufferedReader br) {
        List<String> lines = br.lines().toList();
        List<String> terrainMapLines = new ArrayList<>();
        List<String> foodMapLines = new ArrayList<>();
        boolean readTerrain = false, readFood = false;
        for (var line : lines) {
            if ("!terrain".equals(line)) {
                readTerrain = true;
            } else if ("!food".equals(line)) {
                readFood = true;
                readTerrain = false;
            } else if (readTerrain) {
                terrainMapLines.add(line);
            } else if (readFood) {
                foodMapLines.add(line);
            } else {
                Logger.info("Skip line: '{}'", line);
            }
        }
        terrainMap = TileMap.parse(terrainMapLines, Tiles.TERRAIN_TILES_END);
        foodMap = TileMap.parse(foodMapLines, Tiles.FOOD_TILES_END);
    }

    public int numRows() {
        return terrainMap.numRows();
    }

    public int numCols() {
        return terrainMap.numCols();
    }

    public Stream<Vector2i> tiles() {
        return terrainMap.tiles();
    }

    public TileMap terrainMap() {
        return terrainMap;
    }

    public TileMap foodMap() {
        return foodMap;
    }

    public byte terrain(Vector2i tile) {
        return terrainMap.get(tile);
    }

    public byte terrain(int row, int col) {
        return terrainMap.get(row, col);
    }

    public byte food(Vector2i tile) {
        return foodMap.get(tile);
    }

    public byte food(int row, int col) {
        return foodMap.get(row, col);
    }

    public void save(File file) {
        try (FileWriter w = new FileWriter(file, StandardCharsets.UTF_8)) {
            w.write("!terrain\r\n");
            terrainMap.write(w);
            w.write("!food\r\n");
            foodMap.write(w);
            w.close();
            Logger.info("World map saved to file '{}'.", file);
        } catch (Exception x) {
            Logger.error(x);
        }
    }
}
