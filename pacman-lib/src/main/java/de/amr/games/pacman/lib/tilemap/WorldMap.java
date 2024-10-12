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

    static final MapColorScheme ARCADE_PACMAN_COLOR_SCHEME = new MapColorScheme(
          "#000000", "#2121ff", "#fcb5ff", "febdb4"
    );

    public static final String TERRAIN_SECTION_START = "!terrain";
    public static final String FOOD_SECTION_START    = "!food";

    private static String tileMapSource(TileMap tileMap) throws IOException {
        StringWriter sw = new StringWriter();
        tileMap.print(sw);
        return sw.toString();
    }

    private URL url;
    private TileMap terrain;
    private TileMap food;
    private MapColorScheme defaultColorScheme = ARCADE_PACMAN_COLOR_SCHEME;
    private MapColorScheme colorScheme = ARCADE_PACMAN_COLOR_SCHEME;

    /**
     * Creates a world map consisting of copies of the other map's layers.
     *
     * @param other other map
     */
    public WorldMap(WorldMap other) {
        checkNotNull(other);
        terrain = new TileMap(other.terrain);
        food = new TileMap(other.food);
        url = other.url;
        defaultColorScheme = other.defaultColorScheme;
        colorScheme = other.colorScheme;
        terrain.computeTerrainPaths();
    }

    // Used by map editor
    public WorldMap(int numRows, int numCols) {
        terrain = new TileMap(numRows, numCols);
        food = new TileMap(numRows, numCols);
        url = null;
    }

    public WorldMap(URL url) {
        checkNotNull(url);
        try {
            this.url = url;
            var r = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            parse(r.lines());
        } catch (Exception x) {
            Logger.error(x);
        }
    }

    public WorldMap(File file)  {
        try {
            url = file.toURI().toURL();
            var r = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
            parse(r.lines());
        } catch (Exception x) {
            Logger.error(x);
        }
    }

    public String makeSource() throws IOException {
        return TERRAIN_SECTION_START + "\n" + tileMapSource(terrain) + FOOD_SECTION_START + "\n" + tileMapSource(food);
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
        terrain.computeTerrainPaths();
        food = TileMap.parseTileMap(foodSection, tv -> 0 <= tv && tv <= Tiles.ENERGIZER);
        defaultColorScheme = new MapColorScheme(
            terrain.hasProperty(PROPERTY_COLOR_WALL_FILL)
                ? terrain.getProperty(PROPERTY_COLOR_WALL_FILL) : ARCADE_PACMAN_COLOR_SCHEME.fill(),
            terrain.hasProperty(PROPERTY_COLOR_WALL_STROKE)
                ? terrain.getProperty(PROPERTY_COLOR_WALL_STROKE) : ARCADE_PACMAN_COLOR_SCHEME.stroke(),
            terrain.hasProperty(PROPERTY_COLOR_DOOR)
                ? terrain.getProperty(PROPERTY_COLOR_DOOR) : ARCADE_PACMAN_COLOR_SCHEME.door(),
            food.hasProperty(PROPERTY_COLOR_FOOD)
                ? food.getProperty(PROPERTY_COLOR_FOOD) : ARCADE_PACMAN_COLOR_SCHEME.pellet()
        );
        colorScheme = defaultColorScheme;
    }

    public void save(File file) {
        try (PrintWriter w = new PrintWriter(file, StandardCharsets.UTF_8)) {
            w.println(TERRAIN_SECTION_START);
            terrain.print(w);
            w.println(FOOD_SECTION_START);
            food.print(w);
            Logger.info("World map saved to file '{}'.", file);
        } catch (Exception x) {
            Logger.error(x);
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

    public MapColorScheme colorScheme() {
        return colorScheme;
    }

    public void setColorScheme(MapColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    public MapColorScheme defaultColorScheme() {
        return defaultColorScheme;
    }

    public void setDefaultColorScheme(MapColorScheme defaultColorScheme) {
        this.defaultColorScheme = defaultColorScheme;
    }

    public void resetColorScheme() {
        setColorScheme(defaultColorScheme);
    }
}