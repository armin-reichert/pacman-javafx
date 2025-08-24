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
import de.amr.pacmanfx.lib.tilemap.WorldMapFormatter;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.model.actors.*;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.lib.tilemap.FoodTile.ENERGIZER;
import static de.amr.pacmanfx.lib.tilemap.FoodTile.PELLET;
import static de.amr.pacmanfx.lib.tilemap.TerrainTile.*;
import static de.amr.pacmanfx.model.WorldMapProperty.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

/**
 * A game level contains the world, the actors and the food management.
 */
public class GameLevel {

    //TODO should this be stored in world map instead of hardcoding?
    public static final int EMPTY_ROWS_OVER_MAZE  = 3;
    public static final int EMPTY_ROWS_BELOW_MAZE = 2;

    private static final byte[][] DEFAULT_HOUSE_CONTENT = {
        { ARC_NW.$, WALL_H.$, WALL_H.$, DOOR.$, DOOR.$, WALL_H.$, WALL_H.$, ARC_NE.$ },
        { WALL_V.$, EMPTY.$, EMPTY.$, EMPTY.$, EMPTY.$, EMPTY.$, EMPTY.$, WALL_V.$   },
        { WALL_V.$, EMPTY.$, EMPTY.$, EMPTY.$, EMPTY.$, EMPTY.$, EMPTY.$, WALL_V.$   },
        { WALL_V.$, EMPTY.$, EMPTY.$, EMPTY.$, EMPTY.$, EMPTY.$, EMPTY.$, WALL_V.$   },
        { ARC_SW.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, ARC_SE.$ }
    };

    private static final Vector2i DEFAULT_HOUSE_MIN_TILE = Vector2i.of(10, 15);
    private static final Vector2i DEFAULT_HOUSE_MAX_TILE = Vector2i.of(17, 19);

    private static Vector2f halfTileRightOf(Vector2i tile) { return Vector2f.of(tile.x() * TS + HTS, tile.y() * TS); }

    private final int number; // 1=first level
    private LevelData data;

    private final WorldMap worldMap;
    private final Vector2f pacStartPosition;
    private final Vector2f[] ghostStartPositions = new Vector2f[4];
    private final Vector2i[] ghostScatterTiles = new Vector2i[4];
    private final Direction[] ghostStartDirections = new Direction[4];
    private final Set<Vector2i> energizerPositions;
    private final Portal[] portals;

    private House house;

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

    private GameLevelMessage message;

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
        findHouse();

        currentBonusIndex = -1;
        energizerPositions = worldMap.tilesContaining(LayerID.FOOD, ENERGIZER.code()).collect(Collectors.toSet());
        totalFoodCount = (int) worldMap.tilesContaining(LayerID.FOOD, PELLET.code()).count() + energizerPositions.size();
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

        // Scatter tiles

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
            if (worldMap.content(LayerID.TERRAIN, row, firstColumn) == TUNNEL.$
                && worldMap.content(LayerID.TERRAIN, row, lastColumn) == TUNNEL.$) {
                portals.add(new Portal(leftBorderTile, rightBorderTile, 2));
            }
        }
        return portals.toArray(new Portal[0]);
    }

    private void findHouse() {
        Vector2i minTile = worldMap.getTerrainTileProperty(POS_HOUSE_MIN_TILE);
        Vector2i maxTile = worldMap.getTerrainTileProperty(POS_HOUSE_MAX_TILE);
        if (minTile != null && maxTile != null) {
            Vector2i leftDoorTile = minTile.plus(3, 0);
            Vector2i rightDoorTile = minTile.plus(4, 0);
            house = new House(minTile, maxTile, leftDoorTile, rightDoorTile);
        }
    }

    /**
     * The ghost house is stored in the world map using properties "pos_house_min" (left upper tile) and "pos_house_max"
     * (right lower tile). We add the corresponding map content here such that collision of actors with house walls
     * and doors is working. The obstacle detection algorithm will then also detect the house and create a closed obstacle.
     */
    public void addHouseContent() {
        if (!worldMap.properties(LayerID.TERRAIN).containsKey(POS_HOUSE_MIN_TILE)) {
            Logger.warn("No house min tile found in map!");
            worldMap.properties(LayerID.TERRAIN).put(
                    POS_HOUSE_MIN_TILE,
                    WorldMapFormatter.formatTile(DEFAULT_HOUSE_MIN_TILE)
            );
        }
        if (!worldMap.properties(LayerID.TERRAIN).containsKey(POS_HOUSE_MAX_TILE)) {
            Logger.warn("No house max tile found in map!");
            worldMap.properties(LayerID.TERRAIN).put(
                    POS_HOUSE_MAX_TILE,
                    WorldMapFormatter.formatTile(DEFAULT_HOUSE_MAX_TILE)
            );
        }
        Vector2i minTile = worldMap.getTerrainTileProperty(POS_HOUSE_MIN_TILE);
        Vector2i maxTile = worldMap.getTerrainTileProperty(POS_HOUSE_MAX_TILE);
        Vector2i size = maxTile.minus(minTile).plus(1, 1);

        for (int y = 0; y < size.y(); ++y) {
            for (int x = 0; x < size.x(); ++x) {
                byte content = DEFAULT_HOUSE_CONTENT[y][x];
                worldMap.setContent(LayerID.TERRAIN, minTile.y() + y, minTile.x() + x, content);
            }
        }

        setGhostStartDirection(RED_GHOST_SHADOW, Direction.LEFT);
        setGhostStartDirection(PINK_GHOST_SPEEDY, Direction.DOWN);
        setGhostStartDirection(CYAN_GHOST_BASHFUL, Direction.UP);
        setGhostStartDirection(ORANGE_GHOST_POKEY, Direction.UP);
    }

    public void getReadyToPlay() {
        pac.reset(); // initially invisible!
        pac.setPosition(pacStartPosition());
        pac.setMoveDir(Direction.LEFT);
        pac.setWishDir(Direction.LEFT);
        pac.powerTimer().resetIndefiniteTime();
        ghosts().forEach(ghost -> {
            ghost.reset(); // initially invisible!
            ghost.setPosition(ghostStartPosition(ghost.id().personality()));
            ghost.setMoveDir(ghostStartDirection(ghost.id().personality()));
            ghost.setWishDir(ghostStartDirection(ghost.id().personality()));
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

    public void setMessage(GameLevelMessage message) {
        this.message = message;
    }

    public void clearMessage() {
        message = null;
    }

    public Optional<GameLevelMessage> optMessage() {
        return Optional.ofNullable(message);
    }

    public Vector2f defaultMessagePosition() {
        if (house != null) {
            Vector2i houseSize = house.sizeInTiles();
            float cx = TS(house.minTile().x() + houseSize.x() * 0.5f);
            float cy = TS(house.minTile().y() + houseSize.y() + 1);
            return Vector2f.of(cx, cy);
        }
        else {
            Vector2f worldSize = worldSizePx();
            return Vector2f.of(worldSize.x() * 0.5f, worldSize.y() * 0.5f); // should not happen
        }
    }

    public void setPac(Pac pac) { this.pac = pac; }
    public Pac pac() { return pac; }

    public void setGhosts(Ghost... ghosts) {
        this.ghosts = requireNonNull(ghosts);
        for (Ghost ghost : ghosts) {
            byte personality = ghost.id().personality();
            Vector2i tile = switch (personality) {
                case RED_GHOST_SHADOW, PINK_GHOST_SPEEDY -> worldMap.getTerrainTileProperty(POS_PINK_GHOST);
                case CYAN_GHOST_BASHFUL -> worldMap.getTerrainTileProperty(POS_CYAN_GHOST);
                case ORANGE_GHOST_POKEY -> worldMap.getTerrainTileProperty(POS_ORANGE_GHOST);
                default -> throw new IllegalArgumentException("Illegal ghost personality: %d".formatted(personality));
            };
            house.setGhostRevivalTile(ghost.id(), tile);
        }
    }

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
    public boolean isBonusEdible() { return bonus != null && bonus.state() == BonusState.EDIBLE; }
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

    public Stream<Vector2i> tiles() {
        return worldMap.tiles();
    }

    public boolean isTileInsideWorld(Vector2i tile) { return !worldMap.outOfWorld(tile); }

    public Stream<Vector2i> neighborTilesOutsideWorld(Vector2i tile) {
        requireNonNull(tile);
        return Stream.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT)
            .map(dir -> tile.plus(dir.vector()))
            .filter(not(this::isTileInsideWorld));
    }

    public Stream<Vector2i> neighborTilesInsideWorld(Vector2i tile) {
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

    public boolean isTileBlocked(Vector2i tile) {
        return isTileInsideWorld(tile) && isBlocked(worldMap.content(LayerID.TERRAIN, tile));
    }

    public boolean isTunnel(Vector2i tile) {
        return isTileInsideWorld(tile) && worldMap.content(LayerID.TERRAIN, tile) == TUNNEL.$;
    }

    public boolean isIntersection(Vector2i tile) {
        if (worldMap.outOfWorld(tile) || isTileBlocked(tile)) {
            return false;
        }
        if (house != null && house.isTileInHouseArea(tile)) {
            return false;
        }
        long inaccessible = 0;
        inaccessible += neighborTilesOutsideWorld(tile).count();
        inaccessible += neighborTilesInsideWorld(tile).filter(this::isTileBlocked).count();
        if (house != null) {
            inaccessible += neighborTilesInsideWorld(tile).filter(house::isDoorAt).count();
        }
        return inaccessible <= 1;
    }

    public Optional<House> house() {
        return Optional.ofNullable(house);
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

    public Set<Vector2i> energizerPositions() { return Collections.unmodifiableSet(energizerPositions); }

    public boolean isEnergizerPosition(Vector2i tile) {
        return energizerPositions.contains(tile);
    }

    public boolean tileContainsFood(Vector2i tile) {
        return isFoodPosition(tile) && !tileContainsEatenFood(tile);
    }

    public boolean tileContainsEatenFood(Vector2i tile) {
        return eatenFoodBits.get(worldMap.indexInRowWiseOrder(tile));
    }

    public Stream<Vector2i> tilesContainingFood() {
        return worldMap.tiles().filter(this::tileContainsFood);
    }
}