/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.model.actors.*;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.lib.tilemap.FoodTile.ENERGIZER;
import static de.amr.pacmanfx.lib.tilemap.FoodTile.PELLET;
import static de.amr.pacmanfx.lib.tilemap.TerrainTile.TUNNEL;
import static de.amr.pacmanfx.lib.tilemap.TerrainTile.isBlocked;
import static de.amr.pacmanfx.model.WorldMapProperty.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

/**
 * A game level contains the world, the actors and the food management.
 */
public class GameLevel {

    private static Vector2f halfTileRightOf(Vector2i tile) { return Vector2f.of(tile.x() * TS + HTS, tile.y() * TS); }

    public static final byte MESSAGE_NONE = -1;
    public static final byte MESSAGE_READY = 0;
    public static final byte MESSAGE_GAME_OVER = 1;
    public static final byte MESSAGE_TEST = 2;

    //TODO should this be stored in world map instead of hardcoding?
    public static final int EMPTY_ROWS_OVER_MAZE  = 3;
    public static final int EMPTY_ROWS_BELOW_MAZE = 2;

    private final int number; // 1=first level
    private LevelData data;

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

    private Pac pac;
    private Ghost[] ghosts;
    private final List<Ghost> victims = new ArrayList<>();
    private Bonus bonus;
    private final byte[] bonusSymbols = new byte[2];
    private int currentBonusIndex; // -1=no bonus, 0=first, 1=second
    private byte message = MESSAGE_NONE;

    private final Pulse blinking;

    private int numGhostsKilled;
    private int gameOverStateTicks;
    private long startTime;

    public GameLevel(int number, WorldMap worldMap, LevelData data) {
        this.number = requireValidLevelNumber(number);
        this.worldMap = requireNonNull(worldMap);
        this.data = requireNonNull(data);

        blinking = new Pulse(10, Pulse.OFF);
        portals = findPortals(worldMap);

        currentBonusIndex = -1;
        energizerTiles = worldMap.tilesContaining(LayerID.FOOD, ENERGIZER.code()).toArray(Vector2i[]::new);
        totalFoodCount = (int) worldMap.tilesContaining(LayerID.FOOD, PELLET.code()).count() + energizerTiles.length;
        uneatenFoodCount = totalFoodCount;
        eatenFoodBits = new BitSet(worldMap.numCols() * worldMap.numRows());

        Vector2i pacTile = worldMap.getTerrainTileProperty(POS_PAC);
        if (pacTile == null) {
            throw new IllegalArgumentException("No Pac position stored in map");
        }
        pacStartPosition = halfTileRightOf(pacTile);

        Vector2i redGhostTile = worldMap.getTerrainTileProperty(POS_RED_GHOST);
        if (redGhostTile == null) {
            throw new IllegalArgumentException("No red ghost position stored in map");
        }
        ghostStartPositions[RED_GHOST_SHADOW] = halfTileRightOf(redGhostTile);

        Vector2i pinkGhostTile = worldMap.getTerrainTileProperty(POS_PINK_GHOST);
        if (pinkGhostTile == null) {
            throw new IllegalArgumentException("No pink ghost position stored in map");
        }
        ghostStartPositions[PINK_GHOST_SPEEDY] = halfTileRightOf(pinkGhostTile);

        Vector2i cyanGhostTile = worldMap.getTerrainTileProperty(POS_CYAN_GHOST);
        if (cyanGhostTile == null) {
            throw new IllegalArgumentException("No cyan ghost position stored in map");
        }
        ghostStartPositions[CYAN_GHOST_BASHFUL] = halfTileRightOf(cyanGhostTile);

        Vector2i orangeGhostTile = worldMap.getTerrainTileProperty(POS_ORANGE_GHOST);
        if (orangeGhostTile == null) {
            throw new IllegalArgumentException("No orange ghost position stored in map");
        }
        ghostStartPositions[ORANGE_GHOST_POKEY] = halfTileRightOf(orangeGhostTile);

        ghostScatterTiles[RED_GHOST_SHADOW] = worldMap.getTerrainTileProperty(POS_SCATTER_RED_GHOST,
            Vector2i.of(0, worldMap.numCols() - 3));

        ghostScatterTiles[PINK_GHOST_SPEEDY] = worldMap.getTerrainTileProperty(POS_SCATTER_PINK_GHOST,
            Vector2i.of(0, 3));

        ghostScatterTiles[CYAN_GHOST_BASHFUL] = worldMap.getTerrainTileProperty(POS_SCATTER_CYAN_GHOST,
            Vector2i.of(worldMap.numRows() - EMPTY_ROWS_BELOW_MAZE, worldMap.numCols() - 1));

        ghostScatterTiles[ORANGE_GHOST_POKEY] = worldMap.getTerrainTileProperty(POS_SCATTER_ORANGE_GHOST,
            Vector2i.of(worldMap.numRows() - EMPTY_ROWS_BELOW_MAZE, 0));
    }

    private Portal[] findPortals(WorldMap worldMap) {
        var portals = new ArrayList<Portal>();
        int firstColumn = 0, lastColumn = worldMap.numCols() - 1;
        for (int row = 0; row < worldMap.numRows(); ++row) {
            Vector2i leftBorderTile = Vector2i.of(firstColumn, row), rightBorderTile = Vector2i.of(lastColumn, row);
            if (worldMap.content(LayerID.TERRAIN, row, firstColumn) == TUNNEL.code()
                && worldMap.content(LayerID.TERRAIN, row, lastColumn) == TUNNEL.code()) {
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
            ghost.setPosition(ghostStartPosition(ghost.personality()));
            ghost.setMoveAndWishDir(ghostStartDirection(ghost.personality()));
            ghost.setState(GhostState.LOCKED);
        });
        blinking.setStartPhase(Pulse.ON); // Energizers are visible when ON
        blinking.reset();
    }

    public void showPacAndGhosts() {
        pac.show();
        ghosts().forEach(Ghost::show);
    }

    public void hidePacAndGhosts() {
        pac.hide();
        ghosts().forEach(Ghost::hide);
    }

    public void setData(LevelData data) { this.data = data; }
    public LevelData data() { return data; }

    public List<Ghost> victims() { return victims; }

    public boolean isDemoLevel() { return demoLevel; }
    public void setDemoLevel(boolean demoLevel) { this.demoLevel = demoLevel; }

    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long startTime() { return startTime; }

    public void setGameOverStateTicks(int ticks) { gameOverStateTicks = ticks; }
    public int gameOverStateTicks() { return gameOverStateTicks; }

    public void showMessage(byte message) { this.message = message; }
    public void clearMessage() { message = MESSAGE_NONE; }
    public byte message() { return message; }

    public void setPac(Pac pac) { this.pac = pac; }
    public Pac pac() { return pac; }

    public void setGhosts(Ghost... ghosts) { this.ghosts = requireNonNull(ghosts); }
    public Ghost ghost(byte id) { return ghosts != null ? ghosts[requireValidGhostPersonality(id)] : null; }

    public Stream<Ghost> ghosts(GhostState... states) {
        requireNonNull(states);
        if (ghosts == null) {
            return Stream.empty();
        }
        return states.length == 0 ? Stream.of(ghosts) : Stream.of(ghosts).filter(ghost -> ghost.inAnyOfStates(states));
    }

    public void registerGhostKilled() { numGhostsKilled++; }
    public int numGhostsKilled() { return numGhostsKilled; }

    public Optional<Bonus> bonus() { return Optional.ofNullable(bonus); }
    public void setBonus(Bonus bonus) { this.bonus = bonus; }
    public boolean isBonusEdible() { return bonus != null && bonus.state() == Bonus.STATE_EDIBLE; }
    public int currentBonusIndex() { return currentBonusIndex; }
    public void selectNextBonus() { currentBonusIndex += 1; }
    public byte bonusSymbol(int i) { return bonusSymbols[i]; }
    public void setBonusSymbol(int i, byte symbol) {
        bonusSymbols[i] = symbol;
    }

    public Pulse blinking() {
        return blinking;
    }

    public Vector2i ghostScatterTile(byte personality) {
        return ghostScatterTiles[requireValidGhostPersonality(personality)];
    }

    public int number() { return number; }

    public WorldMap worldMap() { return worldMap; }

    /**
     * @return world size in pixels as (size-x, size-y)
     */
    public Vector2f worldSizePx() {
        return new Vector2f(worldMap.numCols() * TS, worldMap.numRows() * TS);
    }

    public boolean isTileInsideWorld(Vector2i tile) { return !worldMap.outOfWorld(tile); }

    public Stream<Vector2i> neighborsOutsideWorld(Vector2i tile) {
        requireNonNull(tile);
        return Stream.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT)
            .map(dir -> tile.plus(dir.vector()))
            .filter(not(this::isTileInsideWorld));
    }

    public Stream<Vector2i> neighborsInsideWorld(Vector2i tile) {
        requireNonNull(tile);
        return Stream.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT)
            .map(dir -> tile.plus(dir.vector()))
            .filter(this::isTileInsideWorld);
    }

    public List<Portal> portals() { return Arrays.asList(portals); }

    public boolean isTileInPortalSpace(Vector2i tile) {
        requireNonNull(tile);
        return portals().stream().anyMatch(portal -> portal.contains(tile));
    }

    public boolean isDoorAt(Vector2i tile) {
        requireNonNull(tile);
        return tile.equals(leftDoorTile) || tile.equals(rightDoorTile);
    }

    public boolean isTileBlocked(Vector2i tile) {
        return isTileInsideWorld(tile) && isBlocked(worldMap.content(LayerID.TERRAIN, tile));
    }

    public boolean isTunnel(Vector2i tile) {
        return isTileInsideWorld(tile) && worldMap.content(LayerID.TERRAIN, tile) == TUNNEL.code();
    }

    public boolean isIntersection(Vector2i tile) {
        if (worldMap.outOfWorld(tile) || isTileInHouseArea(tile) || isTileBlocked(tile)) {
            return false;
        }
        int freeNeighbors = 4;
        freeNeighbors -= (int) neighborsOutsideWorld(tile).count();
        freeNeighbors -= (int) neighborsInsideWorld(tile).filter(this::isTileBlocked).count();
        freeNeighbors -= (int) neighborsInsideWorld(tile).filter(this::isDoorAt).count();
        return freeNeighbors >= 3;
    }

    // House

    public void setLeftDoorTile(Vector2i tile) {
        leftDoorTile = requireNonNull(tile);
    }

    public void setRightDoorTile(Vector2i tile) {
        rightDoorTile = requireNonNull(tile);
    }

    public Vector2i houseMinTile() {
        return worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE);
    }

    public Vector2i houseMaxTile() {
        return worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MAX_TILE);
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
     * @return position at which ghosts can enter the house, one tile above and horizontally between the two door tiles
     */
    public Vector2f houseEntryPosition() {
        return Vector2f.of(TS * rightDoorTile.x() - HTS, TS * (rightDoorTile.y() - 1));
    }

    public Vector2f houseCenter() {
        return houseMinTile().toVector2f().scaled(TS).plus(houseSizeInTiles().toVector2f().scaled(HTS));
    }

    public boolean isTileInHouseArea(Vector2i tile) {
        requireNonNull(tile);
        return tile.x() >= houseMinTile().x() && tile.x() <= houseMaxTile().x()
            && tile.y() >= houseMinTile().y() && tile.y() <= houseMaxTile().y();
    }

    // Actor positions

    public Vector2f pacStartPosition() {
        return pacStartPosition;
    }

    public void setGhostStartPosition(byte personality, Vector2f position) {
        requireValidGhostPersonality(personality);
        ghostStartPositions[personality] = position;
    }

    public Vector2f ghostStartPosition(byte personality) {
        requireValidGhostPersonality(personality);
        return ghostStartPositions[personality];
    }

    public void setGhostStartDirection(byte personality, Direction dir) {
        requireValidGhostPersonality(personality);
        requireNonNull(dir);
        ghostStartDirections[personality] = dir;
    }

    public Direction ghostStartDirection(byte personality) {
        requireValidGhostPersonality(personality);
        return ghostStartDirections[personality];
    }

    /**
     * @param actor some actor
     * @return tells if the given actor is located inside the house
     */
    public boolean isActorInsideHouse(Actor actor) {
        return isTileInHouseArea(requireNonNull(actor).tile());
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
        if (tileContainsFood(tile)) {
            eatenFoodBits.set(worldMap.indexInRowWiseOrder(tile));
            --uneatenFoodCount;
        } else {
            Logger.warn("Attempt to eat foot at tile {} that has none", tile);
        }
    }

    public void eatAllPellets() {
        worldMap.tiles()
            .filter(this::tileContainsFood)
            .filter(not(this::isEnergizerPosition))
            .forEach(this::registerFoodEatenAt);
    }

    public void eatAllFood() {
        worldMap.tiles()
            .filter(this::tileContainsFood)
            .forEach(this::registerFoodEatenAt);
    }

    public boolean isFoodPosition(Vector2i tile) {
        return isTileInsideWorld(tile) && worldMap.content(LayerID.FOOD, tile) != FoodTile.EMPTY.code();
    }

    public Stream<Vector2i> energizerTiles() { return Arrays.stream(energizerTiles); }

    public boolean isEnergizerPosition(Vector2i tile) {
        return isTileInsideWorld(tile) && worldMap.content(LayerID.FOOD, tile) == ENERGIZER.code();
    }

    public boolean tileContainsFood(Vector2i tile) {
        return isFoodPosition(tile) && !tileContainsEatenFood(tile);
    }

    public boolean tileContainsEatenFood(Vector2i tile) {
        return isTileInsideWorld(tile) && eatenFoodBits.get(worldMap.indexInRowWiseOrder(tile));
    }

    public Stream<Vector2i> tilesContainingFood() {
        return worldMap.tiles().filter(this::tileContainsFood);
    }
}