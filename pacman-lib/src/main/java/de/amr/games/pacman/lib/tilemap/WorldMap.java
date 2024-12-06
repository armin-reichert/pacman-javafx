/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class WorldMap {

    public static final String PROPERTY_COLOR_FOOD         = "color_food";
    public static final String PROPERTY_COLOR_WALL_STROKE  = "color_wall_stroke";
    public static final String PROPERTY_COLOR_WALL_FILL    = "color_wall_fill";
    public static final String PROPERTY_COLOR_DOOR         = "color_door";

    public static final String TERRAIN_SECTION_START = "!terrain";
    public static final String FOOD_SECTION_START    = "!food";

    private static String toConfigNamespace(String key) {
        return "_config." + key;
    }

    private TileMap terrain;
    private List<Obstacle> obstacles;
    private TileMap food;
    private final URL url;

    /**
     * Creates a world map consisting of copies of the other map's layers.
     *
     * @param other other map
     */
    public WorldMap(WorldMap other) {
        checkNotNull(other);
        terrain = new TileMap(other.terrain);
        obstacles = new ArrayList<>(other.obstacles);
        food = new TileMap(other.food);
        url = other.url;
    }

    // Used by map editor
    public WorldMap(int numRows, int numCols) {
        terrain = new TileMap(numRows, numCols);
        food = new TileMap(numRows, numCols);
        url = null;
    }

    public WorldMap(URL url) throws IOException {
        this.url = checkNotNull(url);
        var r = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
        parse(r.lines());
        updateObstacleList();
    }

    public WorldMap(File file) throws IOException {
        url = file.toURI().toURL();
        var r = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
        parse(r.lines());
        updateObstacleList();
    }

    public String sourceCode() {
        return TERRAIN_SECTION_START + "\n" + terrain.sourceCode() + FOOD_SECTION_START + "\n" + food.sourceCode();
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
        terrain = TileMap.parseTileMap(terrainSection, tv -> 0 <= tv && tv <= Tiles.LAST_TERRAIN_VALUE);
        food = TileMap.parseTileMap(foodSection, tv -> 0 <= tv && tv <= Tiles.ENERGIZER);
    }

    public void updateObstacleList() {
        obstacles = new ObstacleBuilder(terrain).buildObstacles();
    }

    public List<Obstacle> obstacles() {
        return Collections.unmodifiableList(obstacles);
    }

    public boolean save(File file) {
        try (PrintWriter w = new PrintWriter(file, StandardCharsets.UTF_8)) {
            w.println(TERRAIN_SECTION_START);
            terrain.print(w);
            w.println(FOOD_SECTION_START);
            food.print(w);
            return true;
        } catch (Exception x) {
            Logger.error(x);
            return false;
        }
    }

    public URL url() {
        return url;
    }

    public TileMap terrain() {
        return terrain;
    }

    public TileMap food() {
        return food;
    }

    // store non-string configuration data used by UI in own "namespace"
    public void setConfigValue(String key, Object value) {
        terrain.setProperty(toConfigNamespace(key), value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(String key) {
        return (T) terrain.getProperty(toConfigNamespace(key));
    }

    public boolean hasConfigValue(String key) {
        return terrain.hasProperty(toConfigNamespace(key));
    }
}