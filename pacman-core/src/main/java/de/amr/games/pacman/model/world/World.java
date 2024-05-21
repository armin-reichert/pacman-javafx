/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.NavPoint;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.world.Tiles.*;
import static java.util.Collections.unmodifiableList;

/**
 * @author Armin Reichert
 */
public class World {

    private final WorldMap map;

    private final BitSet eaten;
    private final int totalFoodCount;
    private int uneatenFoodCount;

    private House house;
    private final List<Vector2i> energizerTiles;
    private List<Portal> portals;
    private List<NavPoint> demoLevelRoute = List.of();
    private Vector2f pacPosition;
    private Vector2f[] ghostPositions;
    private Direction[] ghostDirections;
    private Vector2i[] ghostScatterTiles;
    private Vector2f bonusPosition;

    private Map<Vector2i, List<Direction>> forbiddenPassages = Map.of();

    /**
     * @param worldMap terrain+food map
     */
    public World(WorldMap worldMap) {
        this.map = checkNotNull(worldMap);
        setScatterTiles(map);
        setPacPosition(map);
        setGhostPositions(map);
        setPortals(map);
        energizerTiles = tiles().filter(this::isEnergizerTile).toList();
        eaten = new BitSet(numCols() * numRows());
        totalFoodCount = (int) tiles().filter(this::isFoodTile).count();
        uneatenFoodCount = totalFoodCount;
    }

    private void setPacPosition(WorldMap map) {
        var pacHomeTiles = map.terrain().tiles(PAC_HOME).toList();
        if (pacHomeTiles.isEmpty()) {
            Logger.error("No Pac home tile found in map");
        } else {
            if (pacHomeTiles.size() != 2 || !pacHomeTiles.getFirst().equals(pacHomeTiles.getLast().minus(1, 0))) {
                Logger.error("Pac home must consist of two tiles side-by-side");
            } else {
                var pacHome = pacHomeTiles.getFirst().toFloatVec().scaled(TS).plus(0.5f, 0);
                setPacPosition(pacHome);
                Logger.info("Pac home position found in map: {}", pacHome);
            }
        }
    }

    private void setGhostPositions(WorldMap map) {
        Vector2i[] tiles = new Vector2i[4];
        tiles[0] = map.terrain().tiles(HOME_RED_GHOST).findFirst().orElse(null);
        tiles[1] = map.terrain().tiles(HOME_PINK_GHOST).findFirst().orElse(null);
        tiles[2] = map.terrain().tiles(HOME_CYAN_GHOST).findFirst().orElse(null);
        tiles[3] = map.terrain().tiles(HOME_ORANGE_GHOST).findFirst().orElse(null);
        for (int id = 0; id < 4; ++id) {
            if (tiles[id] == null) {
                Logger.error("Ghost position for ghost ID {} not set in map", id);
                tiles[id] =  Vector2i.ZERO;
            }
        }
        Vector2f[] positions = Stream.of(tiles).map(tile -> tile.toFloatVec().scaled(TS).plus(0.5f, 0)).toArray(Vector2f[]::new);
        setGhostPositions(positions);
    }

    private void setScatterTiles(WorldMap map) {
        Vector2i[] tiles = new Vector2i[4];
        tiles[0] = map.terrain().tiles(SCATTER_TARGET_RED).findFirst().orElse(null);
        tiles[1] = map.terrain().tiles(SCATTER_TARGET_PINK).findFirst().orElse(null);
        tiles[2] = map.terrain().tiles(SCATTER_TARGET_CYAN).findFirst().orElse(null);
        tiles[3] = map.terrain().tiles(SCATTER_TARGET_ORANGE).findFirst().orElse(null);
        for (int id = 0; id < 4; ++id) {
            if (tiles[id] == null) {
                Logger.error("Scatter tile for ghost ID {} not set in map", id);
                tiles[id] = switch (id) {
                    case GameModel.RED_GHOST -> new Vector2i(0, numCols() - 3);
                    case GameModel.PINK_GHOST -> new Vector2i(0, 3);
                    case GameModel.CYAN_GHOST -> new Vector2i(numRows()-1, numCols()-1);
                    case GameModel.ORANGE_GHOST -> new Vector2i(numRows()-1, 0);
                    default -> Vector2i.ZERO;
                };
            }
        }
        setGhostScatterTiles(tiles);
    }

    private void setPortals(WorldMap map) {
        portals = new ArrayList<Portal>();
        int lastColumn = numCols() - 1;
        for (int row = 0; row < numRows(); ++row) {
            var leftBorderTile = v2i(0, row);
            var rightBorderTile = v2i(lastColumn, row);
            if (map.terrain(row, 0) == TUNNEL && map.terrain(row, lastColumn) == TUNNEL) {
                portals.add(new Portal(leftBorderTile, rightBorderTile, 2));
            }
        }
    }

    public void setHouse(House house) {
        this.house = house;
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

    public void setDemoLevelRoute(List<NavPoint> demoLevelRoute) {
        this.demoLevelRoute = checkNotNull(demoLevelRoute);
    }

    public List<NavPoint> getDemoLevelRoute() {
        return demoLevelRoute;
    }

    public void setPacPosition(Vector2f tile) {
        pacPosition = tile;
    }

    public Vector2f pacPosition() {
        return pacPosition;
    }

    public void setGhostPositions(Vector2f[] tiles) {
        ghostPositions = tiles;
    }

    public Vector2f ghostPosition(byte ghostID) {
        checkGhostID(ghostID);
        return ghostPositions[ghostID];
    }

    public void setGhostDirections(Direction[] dirs) {
        ghostDirections = dirs;
    }

    public Direction ghostDirection(byte ghostID) {
        checkGhostID(ghostID);
        return ghostDirections[ghostID];
    }

    public void setGhostScatterTiles(Vector2i[] tiles) {
        ghostScatterTiles = tiles;
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
        return insideBounds(tile) && isTerrainBlocked(map.terrain(tile));
    }

    private boolean isTerrainBlocked(byte content) {
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