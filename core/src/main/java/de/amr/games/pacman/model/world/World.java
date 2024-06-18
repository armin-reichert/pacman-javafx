/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.tilemap.Tiles.*;
import static de.amr.games.pacman.model.GameModel.*;
import static java.util.Collections.unmodifiableList;

/**
 * @author Armin Reichert
 */
public class World {

    //TODO put these methods elsewhere

    public static Vector2i parseVector2i(String text) {
        Pattern pattern = Pattern.compile("\\((\\d+),(\\d+)\\)");
        Matcher m = pattern.matcher(text);
        if (m.matches()) {
            return new Vector2i(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
        }
        Logger.error("Invalid Vector2i format: {}", text);
        return null;
    }

    public static Vector2i getTilePropertyFromMap(TileMap map, String key, Vector2i defaultTile) {
        if (map.hasProperty(key)) {
            Vector2i tile = parseVector2i(map.getProperty(key));
            return tile != null ? tile : defaultTile;
        }
        return defaultTile;
    }

    private final WorldMap map;

    private final BitSet eaten;
    private final int totalFoodCount;
    private int uneatenFoodCount;

    private House house;
    private final List<Vector2i> energizerTiles;
    private List<Portal> portals;
    private Vector2i[] ghostScatterTiles;
    private Vector2f bonusPosition;

    private Map<Vector2i, List<Direction>> forbiddenPassages = Map.of();

    /**
     * @param map terrain+food map
     */
    public World(WorldMap map) {
        this.map = checkNotNull(map);
        setScatterTiles();
        setPortals();
        map.terrain().computePaths();
        energizerTiles = tiles().filter(this::isEnergizerTile).toList();
        eaten = new BitSet(map.numCols() * map.numRows());
        totalFoodCount = (int) tiles().filter(this::isFoodTile).count();
        uneatenFoodCount = totalFoodCount;
    }

    private void setScatterTiles() {
        ghostScatterTiles = new Vector2i[4];

        ghostScatterTiles[RED_GHOST] = getTilePropertyFromMap(map.terrain(),
            WorldMap.PROPERTY_POS_SCATTER_RED_GHOST, v2i(0, numCols() - 3));

        ghostScatterTiles[PINK_GHOST] = getTilePropertyFromMap(map.terrain(),
            WorldMap.PROPERTY_POS_SCATTER_PINK_GHOST, v2i(0, 3));

        ghostScatterTiles[CYAN_GHOST] = getTilePropertyFromMap(map.terrain(),
            WorldMap.PROPERTY_POS_SCATTER_CYAN_GHOST, new Vector2i(numRows()-1, numCols()-1));

        ghostScatterTiles[ORANGE_GHOST] = getTilePropertyFromMap(map.terrain(),
             WorldMap.PROPERTY_POS_SCATTER_ORANGE_GHOST, new Vector2i(numRows()-1, 0));
    }

    private void setPortals() {
        portals = new ArrayList<>();
        int lastColumn = numCols() - 1;
        for (int row = 0; row < numRows(); ++row) {
            var leftBorderTile = v2i(0, row);
            var rightBorderTile = v2i(lastColumn, row);
            if (map.terrain(row, 0) == TUNNEL && map.terrain(row, lastColumn) == TUNNEL) {
                portals.add(new Portal(leftBorderTile, rightBorderTile, 2));
            }
        }
    }

    public void addHouse(House house) {
        this.house = checkNotNull(house);
    }

    public House house() {
        return house;
    }

    public int numCols() {
        return map.numCols();
    }

    public int numRows() {
        return map.numRows();
    }

    public Stream<Vector2i> tiles() {
        return map.tiles();
    }

    public WorldMap map() {
        return map;
    }

    public boolean insideBounds(Vector2i tile) {
        checkTileNotNull(tile);
        return !map.terrain().outOfBounds(tile.y(), tile.x());
    }

    public boolean containsPoint(double x, double y) {
        return 0 <= x && x <= numCols() * TS && 0 <= y && y <= numRows() * TS;
    }

    public Vector2i ghostScatterTile(byte ghostID) {
        checkGhostID(ghostID);
        return ghostScatterTiles[ghostID];
    }

    public void setForbiddenPassages(Map<Vector2i, List<Direction>> forbiddenPassages) {
        this.forbiddenPassages = forbiddenPassages;
    }

    public Map<Vector2i, List<Direction>> forbiddenPassages() {
        return forbiddenPassages;
    }

    public void setBonusPosition(Vector2f position) {
        bonusPosition = position;
    }

    public Vector2f bonusPosition() {
        return bonusPosition;
    }

    /**
     * @return tiles in order top-to-bottom, left-to-right
     */
    public Stream<Vector2i> energizerTiles() {
        return energizerTiles.stream();
    }

    /**
     * @return Unmodifiable list of portals
     */
    public List<Portal> portals() {
        return unmodifiableList(portals);
    }

    public boolean belongsToPortal(Vector2i tile) {
        checkTileNotNull(tile);
        return portals.stream().anyMatch(portal -> portal.contains(tile));
    }

    public boolean isBlockedTile(Vector2i tile) {
        return insideBounds(tile) && isBlockedTerrain(map.terrain(tile));
    }

    private boolean isBlockedTerrain(byte content) {
        return content == WALL_H  || content == WALL_V
            || content == DWALL_H || content == DWALL_V
            || content == CORNER_NE  || content == CORNER_NW  || content == CORNER_SE  || content == CORNER_SW
            || content == DCORNER_NE || content == DCORNER_NW || content == DCORNER_SE || content == DCORNER_SW;
    }

    public boolean isTunnel(Vector2i tile) {
        if (!insideBounds(tile)) {
            return false;
        }
        return map.terrain(tile) == TUNNEL;
    }

    public boolean isIntersection(Vector2i tile) {
        checkTileNotNull(tile);
        if (!insideBounds(tile) || house.contains(tile)) {
            return false;
        }
        long numBlockedNeighbors = tile.neighbors().filter(this::insideBounds).filter(this::isBlockedTile).count();
        long numDoorNeighbors = tile.neighbors().filter(this::insideBounds).filter(house.door()::occupies).count();
        return numBlockedNeighbors + numDoorNeighbors < 2;
    }

    // Food

    public int totalFoodCount() {
        return totalFoodCount;
    }

    public int uneatenFoodCount() {
        return uneatenFoodCount;
    }

    public int eatenFoodCount() {
        return totalFoodCount - uneatenFoodCount;
    }

    public void eatFoodAt(Vector2i tile) {
        if (!insideBounds(tile)) {
            return; // raise error?
        }
        if (hasFoodAt(tile)) {
            eaten.set(map.food().index(tile));
            --uneatenFoodCount;
        }
    }

    public boolean isFoodTile(Vector2i tile) {
        if (!insideBounds(tile)) {
            return false;
        }
        return map.food(tile) != EMPTY;
    }

    public boolean isEnergizerTile(Vector2i tile) {
        if (!insideBounds(tile)) {
            return false;
        }
        return map.food(tile) == ENERGIZER;
    }

    public boolean hasFoodAt(Vector2i tile) {
        if (!insideBounds(tile)) {
            return false;
        }
        return map.food(tile) != EMPTY && !eaten.get(map.food().index(tile));
    }

    public boolean hasEatenFoodAt(Vector2i tile) {
        if (!insideBounds(tile)) {
            return false;
        }
        return eaten.get(map.food().index(tile));
    }
}