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
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.tilemap.TerrainTiles.*;
import static de.amr.games.pacman.lib.tilemap.WorldMap.formatTile;
import static de.amr.games.pacman.model.actors.GhostState.LOCKED;
import static java.util.Objects.requireNonNull;

public class GameLevel {

    public static final int EMPTY_ROWS_OVER_MAZE  = 3;
    public static final int EMPTY_ROWS_BELOW_MAZE = 2;

    public enum Message { READY, GAME_OVER, TEST_LEVEL }

    private static boolean isInaccessible(byte content) {
        return content == WALL_H  || content == WALL_V
            || content == ARC_NE || content == ARC_NW || content == ARC_SE || content == ARC_SW
            || content == DCORNER_NE || content == DCORNER_NW || content == DCORNER_SE || content == DCORNER_SW;
    }

    /**
     * @param tile a tile coordinate
     * @return position in world (scaled by tile size) between given tile and right neighbor tile
     */
    private static Vector2f posHalfTileRightOf(Vector2i tile) {
        return tile.scaled(TS).plus(HTS, 0).toVector2f();
    }

    private final GameModel game;
    private final LevelData data;
    private final int number; // 1=first level

    private final WorldMap worldMap;
    private final Vector2f pacStartPosition;
    private final Vector2f[] ghostStartPositions = new Vector2f[4];
    private final Vector2i[] ghostScatterTiles = new Vector2i[4];
    private final Direction[] ghostStartDirections = new Direction[4];
    private final Vector2i[] energizerTiles;
    private final Portal[] portals;

    private Vector2i leftDoorTile;
    private Vector2i rightDoorTile;

    // instead of Set<Vector2i> we use a bit-set indexed by top-down-left-to-right tile index
    private final BitSet eatenFoodBits;
    private final int totalFoodCount;
    private int uneatenFoodCount;

    private boolean demoLevel;
    private int cutSceneNumber;

    private Pac pac;
    private Ghost[] ghosts;
    private final List<Ghost> victims = new ArrayList<>();
    private Bonus bonus;
    private final byte[] bonusSymbols = new byte[2];
    private int nextBonusIndex; // -1=no bonus, 0=first, 1=second
    private Message message;

    private final Pulse blinking;

    private int gameOverStateTicks;
    private long startTime;

    public GameLevel(GameModel game, int number, LevelData data, WorldMap worldMap) {
        this.game = requireNonNull(game);
        this.number = requireValidLevelNumber(number);
        this.data = requireNonNull(data);
        this.worldMap = requireNonNull(worldMap);

        blinking = new Pulse(10, Pulse.OFF);
        portals = findPortals(worldMap);

        nextBonusIndex = -1;
        energizerTiles = worldMap.tilesContaining(LayerID.FOOD, FoodTiles.ENERGIZER).toArray(Vector2i[]::new);
        totalFoodCount = (int) worldMap.tilesContaining(LayerID.FOOD, FoodTiles.PELLET).count() + energizerTiles.length;
        uneatenFoodCount = totalFoodCount;
        eatenFoodBits = new BitSet(worldMap.numCols() * worldMap.numRows());

        Vector2i pacTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_PAC, null);
        if (pacTile == null) {
            throw new IllegalArgumentException("No Pac position stored in map");
        }
        pacStartPosition = posHalfTileRightOf(pacTile);

        Vector2i redGhostTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_RED_GHOST, null);
        if (redGhostTile == null) {
            throw new IllegalArgumentException("No red ghost position stored in map");
        }
        ghostStartPositions[RED_GHOST_ID] = posHalfTileRightOf(redGhostTile);

        Vector2i pinkGhostTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_PINK_GHOST, null);
        if (pinkGhostTile == null) {
            throw new IllegalArgumentException("No pink ghost position stored in map");
        }
        ghostStartPositions[PINK_GHOST_ID] = posHalfTileRightOf(pinkGhostTile);

        Vector2i cyanGhostTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_CYAN_GHOST, null);
        if (cyanGhostTile == null) {
            throw new IllegalArgumentException("No cyan ghost position stored in map");
        }
        ghostStartPositions[CYAN_GHOST_ID] = posHalfTileRightOf(cyanGhostTile);

        Vector2i orangeGhostTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_ORANGE_GHOST, null);
        if (orangeGhostTile == null) {
            throw new IllegalArgumentException("No orange ghost position stored in map");
        }
        ghostStartPositions[ORANGE_GHOST_ID] = posHalfTileRightOf(orangeGhostTile);

        ghostScatterTiles[RED_GHOST_ID] = worldMap.getTerrainTileProperty(WorldMapProperty.POS_SCATTER_RED_GHOST,
            Vector2i.of(0, worldMap.numCols() - 3));

        ghostScatterTiles[PINK_GHOST_ID] = worldMap.getTerrainTileProperty(WorldMapProperty.POS_SCATTER_PINK_GHOST,
            Vector2i.of(0, 3));

        ghostScatterTiles[CYAN_GHOST_ID] = worldMap.getTerrainTileProperty(WorldMapProperty.POS_SCATTER_CYAN_GHOST,
            Vector2i.of(worldMap.numRows() - EMPTY_ROWS_BELOW_MAZE, worldMap.numCols() - 1));

        ghostScatterTiles[ORANGE_GHOST_ID] = worldMap.getTerrainTileProperty(WorldMapProperty.POS_SCATTER_ORANGE_GHOST,
            Vector2i.of(worldMap.numRows() - EMPTY_ROWS_BELOW_MAZE, 0));
    }

    private Portal[] findPortals(WorldMap worldMap) {
        var portals = new ArrayList<Portal>();
        int firstColumn = 0, lastColumn = worldMap.numCols() - 1;
        for (int row = 0; row < worldMap.numRows(); ++row) {
            Vector2i leftBorderTile = Vector2i.of(firstColumn, row), rightBorderTile = Vector2i.of(lastColumn, row);
            if (worldMap.get(LayerID.TERRAIN, row, firstColumn) == TUNNEL
                && worldMap.get(LayerID.TERRAIN, row, lastColumn) == TUNNEL) {
                portals.add(new Portal(leftBorderTile, rightBorderTile, 2));
            }
        }
        return portals.toArray(new Portal[0]);
    }

    public void makeReadyForPlaying() {
        pac.reset(); // initially invisible!
        pac.setPosition(pacStartPosition());
        pac.setMoveAndWishDir(Direction.LEFT);
        pac.powerTimer().resetIndefiniteTime();
        ghosts().forEach(ghost -> {
            ghost.reset(); // initially invisible!
            ghost.setPosition(ghostStartPosition(ghost.id()));
            ghost.setMoveAndWishDir(ghostStartDirection(ghost.id()));
            ghost.setState(LOCKED);
        });
        blinking.setStartPhase(Pulse.ON); // Energizers are visible when ON
        blinking.reset();
    }

    public void showPacAndGhosts() {
        pac.show();
        ghosts().forEach(Ghost::show);
    }

    public void hidePacAndGhosts() {
        pac().hide();
        ghosts().forEach(Ghost::hide);
    }

    public GameModel game() { return game; }

    public LevelData data() { return data; }

    public List<Ghost> victims() { return victims; }

    public boolean isDemoLevel() { return demoLevel; }
    public void setDemoLevel(boolean demoLevel) { this.demoLevel = demoLevel; }

    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long startTime() { return startTime; }

    public void setGameOverStateTicks(int ticks) { gameOverStateTicks = ticks; }
    public int gameOverStateTicks() { return gameOverStateTicks; }

    public void setCutSceneNumber(int number) { cutSceneNumber = number; }
    public int cutSceneNumber() { return cutSceneNumber; }

    public void showMessage(Message message) { this.message = requireNonNull(message); }
    public void clearMessage() { message = null; }
    public Message message() { return message; }

    public void setPac(Pac pac) { this.pac = pac; }
    public Pac pac() { return pac; }

    public void setGhosts(Ghost[] ghosts) { this.ghosts = requireNonNull(ghosts); }
    public Ghost ghost(byte id) { return ghosts != null ? ghosts[requireValidGhostID(id)] : null; }

    public Stream<Ghost> ghosts(GhostState... states) {
        requireNonNull(states);
        if (ghosts == null) {
            return Stream.empty();
        }
        return states.length == 0 ? Stream.of(ghosts) : Stream.of(ghosts).filter(ghost -> ghost.inState(states));
    }

    public void setBonus(Bonus bonus) { this.bonus = bonus; }

    public Optional<Bonus> bonus() { return Optional.ofNullable(bonus); }

    public boolean isBonusEdible() { return bonus != null && bonus.state() == Bonus.STATE_EDIBLE; }

    public int nextBonusIndex() { return nextBonusIndex; }

    public void selectNextBonus() { nextBonusIndex += 1; }

    public byte bonusSymbol(int i) { return bonusSymbols[i]; }

    public void setBonusSymbol(int i, byte symbol) {
        bonusSymbols[i] = symbol;
    }

    public Pulse blinking() {
        return blinking;
    }

    public Vector2i ghostScatterTile(byte ghostID) {
        return ghostScatterTiles[requireValidGhostID(ghostID)];
    }

    public int number() { return number; }

    public WorldMap worldMap() { return worldMap; }

    public boolean isInsideWorld(Vector2i tile) { return !worldMap.outOfBounds(tile); }

    public Stream<Vector2i> energizerTiles() { return Arrays.stream(energizerTiles); }

    public Stream<Portal> portals() { return Arrays.stream(portals); }

    public boolean isPortalAt(Vector2i tile) {
        requireNonNull(tile);
        return portals().anyMatch(portal -> portal.contains(tile));
    }

    public boolean isDoorAt(Vector2i tile) {
        requireNonNull(tile);
        return tile.equals(leftDoorTile) || tile.equals(rightDoorTile);
    }

    public boolean isBlockedTile(Vector2i tile) {
        return isInsideWorld(tile) && isInaccessible(worldMap.get(LayerID.TERRAIN, tile));
    }

    public boolean isTunnel(Vector2i tile) {
        return isInsideWorld(tile) && worldMap.get(LayerID.TERRAIN, tile) == TUNNEL;
    }

    public boolean isIntersection(Vector2i tile) {
        if (worldMap.outOfBounds(tile) || isPartOfHouse(tile)) {
            return false;
        }
        long numBlockedNeighbors = tile.neighbors().filter(this::isInsideWorld).filter(this::isBlockedTile).count();
        long numDoorNeighbors = tile.neighbors().filter(this::isInsideWorld).filter(this::isDoorAt).count();
        return numBlockedNeighbors + numDoorNeighbors < 2;
    }

    // House

    private void createArcadeHouse(int minX, int minY, int maxX, int maxY) {
        leftDoorTile = Vector2i.of(minX + 3, minY);
        rightDoorTile = Vector2i.of(minX + 4, minY);
        setGhostStartDirection(RED_GHOST_ID, Direction.LEFT);
        setGhostStartDirection(PINK_GHOST_ID, Direction.DOWN);
        setGhostStartDirection(CYAN_GHOST_ID, Direction.UP);
        setGhostStartDirection(ORANGE_GHOST_ID, Direction.UP);

        // Create an obstacle for the house!
        //TODO change attributes to min_tile and max_tiles
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                byte value = EMPTY;
                if      (x == minX && y == minY) value = ARC_NW;
                else if (x == minX && y == maxY) value = ARC_SW;
                else if (x == maxX && y == minY) value = ARC_NE;
                else if (x == maxX && y == maxY) value = ARC_SE;
                else if (y == minY && (x == minX + 3 || x == minX + 4)) value = DOOR;
                else if (x == minX || x == maxX) value = WALL_V;
                else if (y == minY || y == maxY) value = WALL_H;
                worldMap.set(LayerID.TERRAIN, Vector2i.of(x, y), value);
            }
        }
    }

    public void addArcadeHouse() {
        if (!worldMap.hasProperty(LayerID.TERRAIN, WorldMapProperty.POS_HOUSE_MIN_TILE)) {
            Logger.warn("No house min tile found in map!");
            worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.POS_HOUSE_MIN_TILE, formatTile(Vector2i.of(10, 15)));
        }
        if (!worldMap.hasProperty(LayerID.TERRAIN, WorldMapProperty.POS_HOUSE_MAX_TILE)) {
            Logger.warn("No house max tile found in map!");
            worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.POS_HOUSE_MAX_TILE, formatTile(Vector2i.of(17, 19)));
        }
        Vector2i minTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE, null);
        Vector2i maxTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MAX_TILE, null);
        createArcadeHouse(minTile.x(), minTile.y(), maxTile.x(), maxTile.y());
    }

    public Vector2i houseMinTile() {
        return worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE, null);
    }

    public Vector2i houseMaxTile() {
        return worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MAX_TILE, null);
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
        return Vector2f.of(TS * rightDoorTile.x() - HTS, TS * (rightDoorTile.y() - 1));
    }

    public Vector2f houseCenter() {
        return houseMinTile().toVector2f().scaled(TS).plus(houseSizeInTiles().toVector2f().scaled(HTS));
    }

    /**
     * @param tile some tile
     * @return tells if the given tile is part of the ghost house
     */
    public boolean isPartOfHouse(Vector2i tile) {
        requireNonNull(tile);
        return tile.x() >= houseMinTile().x() && tile.x() <= houseMaxTile().x()
                && tile.y() >= houseMinTile().y() && tile.y() <= houseMaxTile().y();
    }

    // Actor positions

    public Vector2f pacStartPosition() {
        return pacStartPosition;
    }

    public void setGhostStartPosition(byte ghostID, Vector2f position) {
        requireValidGhostID(ghostID);
        ghostStartPositions[ghostID] = position;
    }

    public Vector2f ghostStartPosition(byte ghostID) {
        requireValidGhostID(ghostID);
        return ghostStartPositions[ghostID];
    }

    public void setGhostStartDirection(byte ghostID, Direction dir) {
        requireValidGhostID(ghostID);
        requireNonNull(dir);
        ghostStartDirections[ghostID] = dir;
    }

    public Direction ghostStartDirection(byte ghostID) {
        requireValidGhostID(ghostID);
        return ghostStartDirections[ghostID];
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

    public void eatAllFood() {
        worldMap.tiles().filter(this::hasFoodAt).forEach(this::registerFoodEatenAt);
    }

    public boolean isFoodPosition(Vector2i tile) {
        return isInsideWorld(tile) && worldMap.get(LayerID.FOOD, tile) != FoodTiles.EMPTY;
    }

    public boolean isEnergizerPosition(Vector2i tile) {
        return isInsideWorld(tile) && worldMap.get(LayerID.FOOD, tile) == FoodTiles.ENERGIZER;
    }

    public boolean hasFoodAt(Vector2i tile) {
        return isFoodPosition(tile) && !hasEatenFoodAt(tile);
    }

    public boolean hasEatenFoodAt(Vector2i tile) {
        return isInsideWorld(tile) && eatenFoodBits.get(worldMap.index(tile));
    }
}