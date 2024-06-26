/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Vector2i;
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

    public static final String PROPERTY_COLOR_WALL_STROKE = "color_wall_stroke";
    public static final String PROPERTY_COLOR_WALL_FILL = "color_wall_fill";
    public static final String PROPERTY_COLOR_DOOR = "color_door";
    public static final String PROPERTY_POS_PAC = "pos_pac";
    public static final String PROPERTY_POS_RED_GHOST = "pos_ghost_1_red";
    public static final String PROPERTY_POS_PINK_GHOST = "pos_ghost_2_pink";
    public static final String PROPERTY_POS_CYAN_GHOST = "pos_ghost_3_cyan";
    public static final String PROPERTY_POS_ORANGE_GHOST = "pos_ghost_4_orange";
    public static final String PROPERTY_POS_SCATTER_RED_GHOST = "pos_scatter_ghost_1_red";
    public static final String PROPERTY_POS_SCATTER_PINK_GHOST = "pos_scatter_ghost_2_pink";
    public static final String PROPERTY_POS_SCATTER_CYAN_GHOST = "pos_scatter_ghost_3_cyan";
    public static final String PROPERTY_POS_SCATTER_ORANGE_GHOST = "pos_scatter_ghost_4_orange";
    public static final String PROPERTY_POS_BONUS = "pos_bonus";

    public static final String PROPERTY_COLOR_FOOD = "color_food";

    public static final String DEFAULT_COLOR_WALL_STROKE = "rgb(0,0,255)";
    public static final String DEFAULT_COLOR_WALL_FILL   = "rgb(0,0,0)";
    public static final String DEFAULT_COLOR_DOOR        = "rgb(0,255,255)";

    public static final Vector2i DEFAULT_POS_PAC          = new Vector2i(13, 26);
    public static final Vector2i DEFAULT_POS_RED_GHOST    = new Vector2i(13, 14);
    public static final Vector2i DEFAULT_POS_PINK_GHOST   = new Vector2i(13, 17);
    public static final Vector2i DEFAULT_POS_CYAN_GHOST   = new Vector2i(11, 17);
    public static final Vector2i DEFAULT_POS_ORANGE_GHOST = new Vector2i(15, 17);
    public static final Vector2i DEFAULT_POS_BONUS        = new Vector2i(13, 20);

    public static final String DEFAULT_FOOD_COLOR         = "rgb(255,0,0)";

    private static final String TERRAIN_SECTION_START = "!terrain";
    private static final String FOOD_SECTION_START    = "!food";

    private URL url;
    private TileMap terrain;
    private TileMap food;

    /**
     * Creates a world map consisting of copies of the other map's layers.
     *
     * @param other other map
     */
    public WorldMap(WorldMap other) {
        checkNotNull(other);
        terrain = TileMap.copyOf(other.terrain);
        food = TileMap.copyOf(other.food);
        url = other.url; //TODO useful?
    }

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
        terrain = TileMap.parseTileMap(terrainSection, tileValue -> tileValue < Tiles.TERRAIN_TILES_END);
        terrain.computeTerrainPaths();
        food = TileMap.parseTileMap(foodSection, tileValue -> tileValue < Tiles.FOOD_TILES_END);
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

    public int numRows() {
        return terrain.numRows();
    }

    public int numCols() {
        return terrain.numCols();
    }

    public URL url() {
        return url;
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
}