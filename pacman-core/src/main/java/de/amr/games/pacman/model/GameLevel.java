/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.FoodTiles;
import de.amr.games.pacman.lib.tilemap.LayerID;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.tilemap.TerrainTiles.*;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.model.GameModel.*;

public class GameLevel {

    public static final int EMPTY_ROWS_OVER_MAZE  = 3;
    public static final int EMPTY_ROWS_BELOW_MAZE = 2;

    public enum Message { READY, GAME_OVER, TEST_LEVEL }

    private static boolean isInaccessible(byte content) {
        return content == WALL_H  || content == WALL_V
            || content == CORNER_NE  || content == CORNER_NW  || content == CORNER_SE  || content == CORNER_SW
            || content == DCORNER_ANGULAR_NE || content == DCORNER_ANGULAR_NW || content == DCORNER_ANGULAR_SE || content == DCORNER_ANGULAR_SW;
    }

    /**
     * @param tile a tile coordinate
     * @return position in world (scaled by tile size) between given tile and right neighbor tile
     */
    private static Vector2f posHalfTileRightOf(Vector2i tile) {
        return tile.scaled(TS).plus(HTS, 0).toVector2f();
    }

    private final int number; // 1=first level
    private final WorldMap worldMap;
    private final Vector2f pacPosition;
    private final Vector2f[] ghostPositions = new Vector2f[4];
    private final Vector2i[] ghostScatterTiles = new Vector2i[4];
    private final Direction[] ghostDirections = new Direction[4];
    private final Vector2i[] energizerTiles;
    private final Portal[] portals;

    private Vector2i leftDoorTile;
    private Vector2i rightDoorTile;

    // instead of Set<Vector2i> we use a bit-set indexed by top-down-left-to-right tile index
    private final BitSet eatenFoodBits;
    private final int totalFoodCount;
    private int uneatenFoodCount;

    private int victimsCount;
    private int numFlashes;
    private int cutSceneNumber;

    private Pac pac;
    private Ghost[] ghosts;
    private final List<Ghost> victims = new ArrayList<>();
    private Bonus bonus;
    private final byte[] bonusSymbols = new byte[2];
    private int nextBonusIndex; // -1=no bonus, 0=first, 1=second
    private Message message;

    private final Pulse blinking = new Pulse(10, Pulse.OFF);
    private final TickTimer powerTimer = new TickTimer("PacPowerTimer");

    public GameLevel(int number, WorldMap worldMap) {
        if (number < 1) {
            throw new IllegalArgumentException("Illegal level number: " + number);
        }
        this.number = number;
        this.worldMap = assertNotNull(worldMap);

        portals = findPortals(worldMap);
        nextBonusIndex = -1;
        energizerTiles = worldMap.tilesContaining(LayerID.FOOD, FoodTiles.ENERGIZER).toArray(Vector2i[]::new);
        totalFoodCount = (int) worldMap.tilesContaining(LayerID.FOOD, FoodTiles.PELLET).count() + energizerTiles.length;
        uneatenFoodCount = totalFoodCount;
        eatenFoodBits = new BitSet(worldMap.numCols() * worldMap.numRows());

        Vector2i pacTile = worldMap.getTerrainTileProperty(PROPERTY_POS_PAC, null);
        if (pacTile == null) {
            throw new IllegalArgumentException("No Pac position stored in map");
        }
        pacPosition = posHalfTileRightOf(pacTile);

        Vector2i redGhostTile = worldMap.getTerrainTileProperty(PROPERTY_POS_RED_GHOST, null);
        if (redGhostTile == null) {
            throw new IllegalArgumentException("No red ghost position stored in map");
        }
        ghostPositions[RED_GHOST] = posHalfTileRightOf(redGhostTile);

        Vector2i pinkGhostTile = worldMap.getTerrainTileProperty(PROPERTY_POS_PINK_GHOST, null);
        if (pinkGhostTile == null) {
            throw new IllegalArgumentException("No pink ghost position stored in map");
        }
        ghostPositions[PINK_GHOST] = posHalfTileRightOf(pinkGhostTile);

        Vector2i cyanGhostTile = worldMap.getTerrainTileProperty(PROPERTY_POS_CYAN_GHOST, null);
        if (cyanGhostTile == null) {
            throw new IllegalArgumentException("No cyan ghost position stored in map");
        }
        ghostPositions[CYAN_GHOST] = posHalfTileRightOf(cyanGhostTile);

        Vector2i orangeGhostTile = worldMap.getTerrainTileProperty(PROPERTY_POS_ORANGE_GHOST, null);
        if (orangeGhostTile == null) {
            throw new IllegalArgumentException("No orange ghost position stored in map");
        }
        ghostPositions[ORANGE_GHOST] = posHalfTileRightOf(orangeGhostTile);

        ghostScatterTiles[RED_GHOST] = worldMap.getTerrainTileProperty(PROPERTY_POS_SCATTER_RED_GHOST,
            vec_2i(0, worldMap.numCols() - 3));

        ghostScatterTiles[PINK_GHOST] = worldMap.getTerrainTileProperty(PROPERTY_POS_SCATTER_PINK_GHOST,
            vec_2i(0, 3));

        ghostScatterTiles[CYAN_GHOST] = worldMap.getTerrainTileProperty(PROPERTY_POS_SCATTER_CYAN_GHOST,
            vec_2i(worldMap.numRows() - EMPTY_ROWS_BELOW_MAZE, worldMap.numCols() - 1));

        ghostScatterTiles[ORANGE_GHOST] = worldMap.getTerrainTileProperty(PROPERTY_POS_SCATTER_ORANGE_GHOST,
            vec_2i(worldMap.numRows() - EMPTY_ROWS_BELOW_MAZE, 0));
    }

    private Portal[] findPortals(WorldMap worldMap) {
        var portals = new ArrayList<Portal>();
        int firstColumn = 0, lastColumn = worldMap.numCols() - 1;
        for (int row = 0; row < worldMap.numRows(); ++row) {
            Vector2i leftBorderTile = vec_2i(firstColumn, row), rightBorderTile = vec_2i(lastColumn, row);
            if (worldMap.get(LayerID.TERRAIN, row, firstColumn) == TUNNEL
                && worldMap.get(LayerID.TERRAIN, row, lastColumn) == TUNNEL) {
                portals.add(new Portal(leftBorderTile, rightBorderTile, 2));
            }
        }
        return portals.toArray(new Portal[0]);
    }

    public void addVictim(Ghost ghost) {
        victimsCount += 1;
        victims.add(ghost);
    }

    public int victimsCount() {
        return victimsCount;
    }

    public void setNumFlashes(int numFlashes) {
        this.numFlashes = numFlashes;
    }

    public int numFlashes() {
        return numFlashes;
    }

    public void setCutSceneNumber(int number) {
        this.cutSceneNumber = number;
    }

    public int cutSceneNumber() {
        return cutSceneNumber;
    }

    public void showMessage(Message message) {
        this.message = assertNotNull(message);
    }

    public void clearMessage() {
        message = null;
    }

    public Message message() {
        return message;
    }

    public void setPac(Pac pac) {
        this.pac = pac;
    }

    public Pac pac() { return pac; }

    public void setGhosts(Ghost[] ghosts) {
        this.ghosts = assertNotNull(ghosts);
    }

    public Ghost ghost(byte id) {
        assertLegalGhostID(id);
        return ghosts != null ? ghosts[id] : null;
    }

    public Stream<Ghost> ghosts(GhostState... states) {
        assertNotNull(states);
        if (ghosts == null) {
            return Stream.empty();
        }
        return states.length == 0 ? Stream.of(ghosts) : Stream.of(ghosts).filter(ghost -> ghost.inState(states));
    }

    public List<Ghost> victims() {
        return victims;
    }

    public void setBonus(Bonus bonus) {
        this.bonus = bonus;
    }

    public Optional<Bonus> bonus() {
        return Optional.ofNullable(bonus);
    }

    public int nextBonusIndex() {
        return nextBonusIndex;
    }

    public void advanceNextBonus() {
        nextBonusIndex += 1;
    }

    public byte bonusSymbol(int i) {
        return bonusSymbols[i];
    }

    public void setBonusSymbol(int i, byte symbol) {
        bonusSymbols[i] = symbol;
    }

    public Pulse blinking() {
        return blinking;
    }

    public TickTimer powerTimer() {
        return powerTimer;
    }

    public Vector2i ghostScatterTile(byte ghostID) {
        return ghostScatterTiles[assertLegalGhostID(ghostID)];
    }

    public int number() {
        return number;
    }

    public WorldMap worldMap() {
        return worldMap;
    }

    public boolean outOfWorld(Vector2i tile) {
        assertTileNotNull(tile);
        return worldMap.outOfBounds(tile.y(), tile.x());
    }

    public boolean isInsideWorld(Vector2i tile) {
        return !outOfWorld(tile);
    }

    public boolean containsPoint(double x, double y) {
        return 0 <= x && x <= worldMap.numCols() * TS && 0 <= y && y <= worldMap.numRows() * TS;
    }

    public Stream<Vector2i> energizerTiles() {
        return Arrays.stream(energizerTiles);
    }

    public Stream<Portal> portals() {
        return Arrays.stream(portals);
    }

    public boolean isPortalAt(Vector2i tile) {
        assertTileNotNull(tile);
        return portals().anyMatch(portal -> portal.contains(tile));
    }

    public boolean isDoorAt(Vector2i tile) {
        assertTileNotNull(tile);
        return tile.equals(leftDoorTile) || tile.equals(rightDoorTile);
    }

    public boolean isBlockedTile(Vector2i tile) {
        return !outOfWorld(tile) && isInaccessible(worldMap.get(LayerID.TERRAIN, tile));
    }

    public boolean isTunnel(Vector2i tile) {
        return !outOfWorld(tile) && worldMap.get(LayerID.TERRAIN, tile) == TUNNEL;
    }

    public boolean isIntersection(Vector2i tile) {
        if (outOfWorld(tile) || isPartOfHouse(tile)) {
            return false;
        }
        long numBlockedNeighbors = tile.neighbors().filter(this::isInsideWorld).filter(this::isBlockedTile).count();
        long numDoorNeighbors = tile.neighbors().filter(this::isInsideWorld).filter(this::isDoorAt).count();
        return numBlockedNeighbors + numDoorNeighbors < 2;
    }

    // House

    /**
     * @param minX tile-x of top left corner
     * @param minY tile-y of top left corner
     * @param maxX tile-x of bottom right corner
     * @param maxY tile-y of bottom right corner
     */
    public void createArcadeHouse(int minX, int minY, int maxX, int maxY) {
        leftDoorTile = vec_2i(minX + 3, minY);
        rightDoorTile = vec_2i(minX + 4, minY);
        setGhostDirection(RED_GHOST, Direction.LEFT);
        setGhostDirection(PINK_GHOST, Direction.DOWN);
        setGhostDirection(CYAN_GHOST, Direction.UP);
        setGhostDirection(ORANGE_GHOST, Direction.UP);

        // Create an obstacle for the house!
        //TODO change attributes to min_tile and max_tiles
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                byte value = EMPTY;
                if      (x == minX && y == minY) value = CORNER_NW;
                else if (x == minX && y == maxY) value = CORNER_SW;
                else if (x == maxX && y == minY) value = CORNER_NE;
                else if (x == maxX && y == maxY) value = CORNER_SE;
                else if (y == minY && (x == minX + 3 || x == minX + 4)) value = DOOR;
                else if (x == minX || x == maxX) value = WALL_V;
                else if (y == minY || y == maxY) value = WALL_H;
                worldMap.set(LayerID.TERRAIN, vec_2i(x, y), value);
            }
        }
    }

    public Vector2i houseMinTile() {
        return worldMap.getTerrainTileProperty(PROPERTY_POS_HOUSE_MIN_TILE, null);
    }

    public Vector2i houseMaxTile() {
        return worldMap.getTerrainTileProperty(PROPERTY_POS_HOUSE_MAX_TILE, null);
    }

    public Vector2i houseSizeInTiles() {
        return houseMaxTile().minus(houseMinTile()).plus(1, 1);
    }

    public Vector2i houseLeftDoorTile() {
        return leftDoorTile;
    }

    public Vector2i houseRightDoorTile() {
        return rightDoorTile;
    }

    /**
     * @return position where ghost can enter the door
     */
    public Vector2f houseEntryPosition() {
        return vec_2f(TS * rightDoorTile.x() - HTS, TS * (rightDoorTile.y() - 1));
    }

    public Vector2f houseCenter() {
        return houseMinTile().toVector2f().scaled(TS).plus(houseSizeInTiles().toVector2f().scaled(HTS));
    }

    /**
     * @param tile some tile
     * @return tells if the given tile is part of the ghost house
     */
    public boolean isPartOfHouse(Vector2i tile) {
        assertTileNotNull(tile);
        return tile.x() >= houseMinTile().x() && tile.x() <= houseMaxTile().x()
                && tile.y() >= houseMinTile().y() && tile.y() <= houseMaxTile().y();
    }

    // Actor positions

    public Vector2f pacPosition() {
        return pacPosition;
    }

    public void setGhostPosition(byte ghostID, Vector2f position) {
        assertLegalGhostID(ghostID);
        ghostPositions[ghostID] = position;
    }

    public Vector2f ghostPosition(byte ghostID) {
        assertLegalGhostID(ghostID);
        return ghostPositions[ghostID];
    }

    public void setGhostDirection(byte ghostID, Direction dir) {
        assertLegalGhostID(ghostID);
        assertNotNull(dir);
        ghostDirections[ghostID] = dir;
    }

    public Direction ghostDirection(byte ghostID) {
        assertLegalGhostID(ghostID);
        return ghostDirections[ghostID];
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

    public void registerFoodEatenAt(Vector2i tile) {
        if (hasFoodAt(tile)) {
            eatenFoodBits.set(worldMap.index(tile));
            --uneatenFoodCount;
        } else {
            Logger.warn("Attempt to eat foot at tile {} that has none", tile);
        }
    }

    public boolean isFoodPosition(Vector2i tile) {
        return !outOfWorld(tile) && worldMap.get(LayerID.FOOD, tile) != FoodTiles.EMPTY;
    }

    public boolean isEnergizerPosition(Vector2i tile) {
        return !outOfWorld(tile) && worldMap.get(LayerID.FOOD, tile) == FoodTiles.ENERGIZER;
    }

    public boolean hasFoodAt(Vector2i tile) {
        return isFoodPosition(tile) && !hasEatenFoodAt(tile);
    }

    public boolean hasEatenFoodAt(Vector2i tile) {
        return !outOfWorld(tile) && eatenFoodBits.get(worldMap.index(tile));
    }
}