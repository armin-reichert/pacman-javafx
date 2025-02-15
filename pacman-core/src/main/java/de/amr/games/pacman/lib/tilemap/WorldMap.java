/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class WorldMap {

    public static final String PROPERTY_POS_BONUS                = "pos_bonus";
    public static final String PROPERTY_POS_PAC                  = "pos_pac";
    public static final String PROPERTY_POS_RED_GHOST            = "pos_ghost_1_red";
    public static final String PROPERTY_POS_PINK_GHOST           = "pos_ghost_2_pink";
    public static final String PROPERTY_POS_CYAN_GHOST           = "pos_ghost_3_cyan";
    public static final String PROPERTY_POS_ORANGE_GHOST         = "pos_ghost_4_orange";
    public static final String PROPERTY_POS_SCATTER_RED_GHOST    = "pos_scatter_ghost_1_red";
    public static final String PROPERTY_POS_SCATTER_PINK_GHOST   = "pos_scatter_ghost_2_pink";
    public static final String PROPERTY_POS_SCATTER_CYAN_GHOST   = "pos_scatter_ghost_3_cyan";
    public static final String PROPERTY_POS_SCATTER_ORANGE_GHOST = "pos_scatter_ghost_4_orange";
    public static final String PROPERTY_POS_HOUSE_MIN_TILE       = "pos_house_min";
    public static final String PROPERTY_POS_HOUSE_MAX_TILE       = "pos_house_max";

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
    private Set<Obstacle> obstacles = Collections.emptySet();
    private TileMap food;
    private final URL url;

    /**
     * Creates a world map consisting of copies of the other map's layers.
     *
     * @param other other map
     */
    public WorldMap(WorldMap other) {
        Globals.assertNotNull(other);
        terrain = new TileMap(other.terrain);
        obstacles = new HashSet<>(other.obstacles);
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
        this.url = Globals.assertNotNull(url);
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

    public WorldMap insertRowBeforeIndex(int rowIndex) {
        if (rowIndex < 0 || rowIndex > terrain.numRows()) {
            throw new IllegalArgumentException("Illegal row index for inserting row: " + rowIndex);
        }
        WorldMap newMap = new WorldMap(terrain.numRows() + 1, terrain.numCols());
        newMap.terrain.replaceProperties(terrain.getProperties());
        newMap.food().replaceProperties(food().getProperties());
        for (int row = 0; row < newMap.terrain.numRows(); ++row) {
            for (int col = 0; col < newMap.terrain.numCols(); ++col) {
                byte terrainValue = TerrainTiles.EMPTY;
                byte foodValue = FoodTiles.EMPTY;
                if (row < rowIndex) {
                    terrainValue = terrain.get(row, col);
                    foodValue = food.get(row, col);
                } else if (row > rowIndex) {
                    terrainValue = terrain.get(row - 1, col);
                    foodValue = food.get(row - 1, col);
                } else {
                    if ((col == 0 || col == terrain.numCols() - 1)
                            && terrain.get(row, col) == TerrainTiles.WALL_V) {
                        terrainValue = TerrainTiles.WALL_V; // keep vertical border wall
                    }
                }
                newMap.terrain.set(row, col, terrainValue);
                newMap.food.set(row, col, foodValue);
            }
        }
        return newMap;
    }

    public WorldMap deleteRowAtIndex(int rowIndexToDelete) {
        if (rowIndexToDelete < 0 || rowIndexToDelete > terrain.numRows() - 1) {
            throw new IllegalArgumentException("Illegal row index for deleting row: " + rowIndexToDelete);
        }
        if (terrain.numRows() == 0) {
            return this;
        }
        WorldMap newMap = new WorldMap(terrain.numRows() - 1, terrain.numCols());
        newMap.terrain.replaceProperties(terrain.getProperties());
        newMap.food.replaceProperties(food.getProperties());
        for (int row = 0; row < newMap.terrain.numRows(); ++row) {
            for (int col = 0; col < newMap.terrain.numCols(); ++col) {
                if (row < rowIndexToDelete) {
                    newMap.terrain.set(row, col, terrain.get(row, col));
                    newMap.food.set(row, col, food.get(row, col));
                } else {
                    newMap.terrain.set(row, col, terrain.get(row + 1, col));
                    newMap.food.set(row, col, food.get(row + 1, col));
                }
            }
        }
        return newMap;
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
        terrain = TileMap.parseTileMap(terrainSection,
            value -> 0 <= value && value <= TerrainTiles.LAST_TERRAIN_VALUE, TerrainTiles.EMPTY);

        food = TileMap.parseTileMap(foodSection,
            value -> 0 <= value && value <= FoodTiles.ENERGIZER, FoodTiles.EMPTY);


        // Replace obsolete terrain tile values
        terrain.tiles().forEach(tile -> {
            byte content = terrain.get(tile);
            byte newContent = switch (content) {
                case TerrainTiles.OBSOLETE_DWALL_H -> TerrainTiles.WALL_H;
                case TerrainTiles.OBSOLETE_DWALL_V -> TerrainTiles.WALL_V;
                case TerrainTiles.OBSOLETE_DCORNER_NW -> TerrainTiles.CORNER_NW;
                case TerrainTiles.OBSOLETE_DCORNER_SW -> TerrainTiles.CORNER_SW;
                case TerrainTiles.OBSOLETE_DCORNER_SE -> TerrainTiles.CORNER_SE;
                case TerrainTiles.OBSOLETE_DCORNER_NE -> TerrainTiles.CORNER_NE;
                default -> content;
            };
            terrain.set(tile, newContent);
        });
    }

    public List<Vector2i> updateObstacleList() {
        List<Vector2i> tilesWithErrors = new ArrayList<>();
        obstacles = ObstacleBuilder.buildObstacles(terrain, tilesWithErrors);
        return tilesWithErrors;
    }

    public Set<Obstacle> obstacles() {
        return Collections.unmodifiableSet(obstacles);
    }

    public boolean save(File file) {
        try (PrintWriter w = new PrintWriter(file, StandardCharsets.UTF_8)) {
            w.println(TERRAIN_SECTION_START);
            terrain.print(w);
            w.println(FOOD_SECTION_START);
            food.print(w);
            return true;
        } catch (IOException x) {
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