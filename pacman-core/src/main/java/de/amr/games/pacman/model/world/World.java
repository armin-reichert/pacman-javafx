/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Direction;
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

    private final WorldMap worldMap;

    private final BitSet eaten;
    private final int totalFoodCount;
    private int uneatenFoodCount;

    private House house;
    private final List<Vector2i> energizerTiles;
    private List<Portal> portals;
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
        this.worldMap = checkNotNull(worldMap);
        setScatterTiles();
        setPacPosition();
        setGhostPositions();
        setPortals();
        worldMap.terrain().computePaths();
        energizerTiles = tiles().filter(this::isEnergizerTile).toList();
        eaten = new BitSet(numCols() * numRows());
        totalFoodCount = (int) tiles().filter(this::isFoodTile).count();
        uneatenFoodCount = totalFoodCount;
    }

    private void setPacPosition() {
        Optional<Vector2i> pacHomeTile = worldMap.terrain().tiles(PAC_HOME).findFirst();
        if (pacHomeTile.isEmpty()) {
            Logger.error("No Pac home tile found in map");
        }
        pacPosition = pacHomeTile.orElse(new Vector2i(13, 26)).toFloatVec().scaled(TS).plus(HTS, 0);
    }

    private void setGhostPositions() {
        Optional<Vector2i> homeTileRed = worldMap.terrain().tiles(HOME_RED_GHOST).findFirst();
        if (homeTileRed.isEmpty()) {
            Logger.error("No home tile set for red ghost");
        }
        Optional<Vector2i> homeTilePink = worldMap.terrain().tiles(HOME_PINK_GHOST).findFirst();
        if (homeTilePink.isEmpty()) {
            Logger.error("No home tile set for pink ghost");
        }
        Optional<Vector2i> homeTileCyan = worldMap.terrain().tiles(HOME_CYAN_GHOST).findFirst();
        if (homeTileCyan.isEmpty()) {
            Logger.error("No home tile set for cyan ghost");
        }
        Optional<Vector2i> homeTileOrange = worldMap.terrain().tiles(HOME_ORANGE_GHOST).findFirst();
        if (homeTileOrange.isEmpty()) {
            Logger.error("No home tile set for orange ghost");
        }
        Vector2i[] tiles = new Vector2i[4];
        tiles[GameModel.RED_GHOST]    = homeTileRed.orElse(new Vector2i(13, 14));
        tiles[GameModel.PINK_GHOST]   = homeTilePink.orElse(new Vector2i(13, 17));
        tiles[GameModel.CYAN_GHOST]   = homeTileCyan.orElse(new Vector2i(11, 17));
        tiles[GameModel.ORANGE_GHOST] = homeTileOrange.orElse(new Vector2i(15, 17));

        // each position is half tile right of home tile
        ghostPositions = new Vector2f[4];
        for (int i = 0; i < 4; ++i) {
            ghostPositions[i] = tiles[i].scaled(TS).plus(HTS, 0).toFloatVec();
        }
    }

    private void setScatterTiles() {
        Optional<Vector2i> scatterTileRed = worldMap.terrain().tiles(SCATTER_TARGET_RED).findFirst();
        if (scatterTileRed.isEmpty()) {
            Logger.error("No scatter target set for red ghost");
        }
        Optional<Vector2i> scatterTilePink = worldMap.terrain().tiles(SCATTER_TARGET_PINK).findFirst();
        if (scatterTilePink.isEmpty()) {
            Logger.error("No scatter target set for pink ghost");
        }
        Optional<Vector2i> scatterTileCyan = worldMap.terrain().tiles(SCATTER_TARGET_CYAN).findFirst();
        if (scatterTileCyan.isEmpty()) {
            Logger.error("No scatter target set for cyan ghost");
        }
        Optional<Vector2i> scatterTileOrange = worldMap.terrain().tiles(SCATTER_TARGET_ORANGE).findFirst();
        if (scatterTileOrange.isEmpty()) {
            Logger.error("No scatter target set for orange ghost");
        }
        Vector2i[] tiles = new Vector2i[4];
        tiles[GameModel.RED_GHOST] = scatterTileRed.orElse(new Vector2i(0, numCols() - 3));
        tiles[GameModel.PINK_GHOST] = scatterTilePink.orElse(new Vector2i(0, 3));
        tiles[GameModel.CYAN_GHOST] = scatterTileCyan.orElse(new Vector2i(numRows()-1, numCols()-1));
        tiles[GameModel.ORANGE_GHOST] = scatterTileOrange.orElse(new Vector2i(numRows()-1, 0));
        setGhostScatterTiles(tiles);
    }

    private void setPortals() {
        portals = new ArrayList<>();
        int lastColumn = numCols() - 1;
        for (int row = 0; row < numRows(); ++row) {
            var leftBorderTile = v2i(0, row);
            var rightBorderTile = v2i(lastColumn, row);
            if (worldMap.terrain(row, 0) == TUNNEL && worldMap.terrain(row, lastColumn) == TUNNEL) {
                portals.add(new Portal(leftBorderTile, rightBorderTile, 2));
            }
        }
    }

    public void addHouse(House house, Vector2i houseTopLeftTile) {
        this.house = checkNotNull(house);
        house.setTopLeftTile(houseTopLeftTile);
    }

    public House house() {
        return house;
    }

    public int numCols() {
        return worldMap.numCols();
    }

    public int numRows() {
        return worldMap.numRows();
    }

    public Stream<Vector2i> tiles() {
        return worldMap.tiles();
    }

    public WorldMap map() {
        return worldMap;
    }

    public boolean insideBounds(Vector2i tile) {
        checkTileNotNull(tile);
        return !worldMap.terrain().outOfBounds(tile.y(), tile.x());
    }

    public boolean containsPoint(double x, double y) {
        return 0 <= x && x <= numCols() * TS && 0 <= y && y <= numRows() * TS;
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
        return insideBounds(tile) && isBlockedTerrain(worldMap.terrain(tile));
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
        return worldMap.terrain(tile) == TUNNEL;
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
            eaten.set(worldMap.food().index(tile));
            --uneatenFoodCount;
        }
    }

    public boolean isFoodTile(Vector2i tile) {
        if (!insideBounds(tile)) {
            return false;
        }
        return worldMap.food(tile) != EMPTY;
    }

    public boolean isEnergizerTile(Vector2i tile) {
        if (!insideBounds(tile)) {
            return false;
        }
        return worldMap.food(tile) == ENERGIZER;
    }

    public boolean hasFoodAt(Vector2i tile) {
        if (!insideBounds(tile)) {
            return false;
        }
        return worldMap.food(tile) != EMPTY && !eaten.get(worldMap.food().index(tile));
    }

    public boolean hasEatenFoodAt(Vector2i tile) {
        if (!insideBounds(tile)) {
            return false;
        }
        return eaten.get(worldMap.food().index(tile));
    }
}