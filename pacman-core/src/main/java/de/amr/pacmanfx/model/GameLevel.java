/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.model.actors.*;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.lib.worldmap.FoodTile.ENERGIZER;
import static de.amr.pacmanfx.lib.worldmap.TerrainTile.TUNNEL;
import static de.amr.pacmanfx.lib.worldmap.TerrainTile.isBlocked;
import static de.amr.pacmanfx.lib.worldmap.WorldMapFormatter.formatTile;
import static de.amr.pacmanfx.model.DefaultWorldMapProperties.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

/**
 * A game level contains the world, the actors and the food management.
 */
public class GameLevel {

    //TODO should this be stored in world map instead of hardcoding?
    public static final int EMPTY_ROWS_OVER_MAZE  = 3;
    public static final int EMPTY_ROWS_BELOW_MAZE = 2;

    private static Vector2f halfTileRightOf(Vector2i tile) { return Vector2f.of(tile.x() * TS + HTS, tile.y() * TS); }

    private final int number; // 1=first level
    private LevelData data;

    private final WorldMap worldMap;
    private final Vector2f pacStartPosition;
    private final Vector2i[] ghostScatterTiles = new Vector2i[4];
    private final Direction[] ghostStartDirections = new Direction[4];
    private final Set<Vector2i> energizerTiles;
    private final Portal[] portals;

    private House house;

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

    private final FoodStore foodStore;

    public GameLevel(int number, WorldMap worldMap, LevelData data) {
        this.number = requireValidLevelNumber(number);
        this.worldMap = requireNonNull(worldMap);
        this.data = requireNonNull(data);

        blinking = new Pulse(10, Pulse.OFF);
        portals = findPortals(worldMap);

        findHouse();

        currentBonusIndex = -1;
        energizerTiles = worldMap.tilesContaining(LayerID.FOOD, ENERGIZER.$).collect(Collectors.toSet());

        Vector2i pacTile = worldMap.getTerrainTileProperty(POS_PAC);
        if (pacTile == null) {
            throw new IllegalArgumentException("No Pac position stored in map");
        }
        pacStartPosition = halfTileRightOf(pacTile);

        setGhostStartDirection(RED_GHOST_SHADOW, Direction.LEFT);
        setGhostStartDirection(PINK_GHOST_SPEEDY, Direction.DOWN);
        setGhostStartDirection(CYAN_GHOST_BASHFUL, Direction.UP);
        setGhostStartDirection(ORANGE_GHOST_POKEY, Direction.UP);

        // Scatter tiles

        ghostScatterTiles[RED_GHOST_SHADOW] = worldMap.getTerrainTileProperty(POS_SCATTER_RED_GHOST,
            Vector2i.of(0, worldMap.numCols() - 3));

        ghostScatterTiles[PINK_GHOST_SPEEDY] = worldMap.getTerrainTileProperty(POS_SCATTER_PINK_GHOST,
            Vector2i.of(0, 3));

        ghostScatterTiles[CYAN_GHOST_BASHFUL] = worldMap.getTerrainTileProperty(POS_SCATTER_CYAN_GHOST,
            Vector2i.of(worldMap.numRows() - EMPTY_ROWS_BELOW_MAZE, worldMap.numCols() - 1));

        ghostScatterTiles[ORANGE_GHOST_POKEY] = worldMap.getTerrainTileProperty(POS_SCATTER_ORANGE_GHOST,
            Vector2i.of(worldMap.numRows() - EMPTY_ROWS_BELOW_MAZE, 0));

        foodStore = new FoodStore(worldMap, energizerTiles);
    }

    private Vector2f findGhostStartPosition(byte ghostPersonality) {
        String propertyName = switch (ghostPersonality) {
            case RED_GHOST_SHADOW ->  POS_GHOST_1_RED;
            case PINK_GHOST_SPEEDY -> POS_GHOST_2_PINK;
            case CYAN_GHOST_BASHFUL -> POS_GHOST_3_CYAN;
            case ORANGE_GHOST_POKEY -> POS_GHOST_4_ORANGE;
            default -> throw new IllegalArgumentException("Illegal ghost personality: %d".formatted(ghostPersonality));
        };
        Vector2i tile = worldMap.getTerrainTileProperty(propertyName);
        if (tile == null) {
            throw new IllegalArgumentException("Terrain property with name '%s' not found!".formatted(propertyName));
        }
        return halfTileRightOf(tile);
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

    /**
     * The ghost house is stored in the world map using property "pos_house_min" (left upper tile).
     * We add the corresponding map content here at runtime such that collision of actors with house walls
     * and doors is working. The obstacle detection algorithm will then also detect the house and create a
     * closed obstacle representing the house boundary.
     */
    private void findHouse() {
        Vector2i minTile = worldMap.getTerrainTileProperty(POS_HOUSE_MIN_TILE);
        if (minTile == null) {
            minTile = ArcadeHouse.ORIGINAL_MIN_TILE;
            Logger.warn("No house min tile found in map, using {}", minTile);
            worldMap.properties(LayerID.TERRAIN).put(POS_HOUSE_MIN_TILE, formatTile(minTile));
        }
        house = new ArcadeHouse(minTile);
        worldMap.setContentRect(LayerID.TERRAIN, minTile, house.content());
    }

    public void getReadyToPlay() {
        pac.reset(); // initially invisible!
        pac.setPosition(pacStartPosition());
        pac.setMoveDir(Direction.LEFT);
        pac.setWishDir(Direction.LEFT);
        pac.powerTimer().resetIndefiniteTime();
        ghosts().forEach(ghost -> {
            ghost.reset(); // initially invisible!
            ghost.setPosition(ghost.startPosition());
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

    public FoodStore foodStore() {
        return foodStore;
    }

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
            Vector2i worldSize = worldSizePx();
            return Vector2f.of(worldSize.x() * 0.5f, worldSize.y() * 0.5f); // should not happen
        }
    }

    public void setPac(Pac pac) { this.pac = pac; }
    public Pac pac() { return pac; }

    public void setGhosts(Ghost... ghosts) {
        this.ghosts = requireNonNull(ghosts);
        for (Ghost ghost : ghosts) {
            byte personality = ghost.id().personality();
            ghost.setStartPosition(findGhostStartPosition(personality));
            Vector2i tile = switch (personality) {
                case RED_GHOST_SHADOW, PINK_GHOST_SPEEDY -> worldMap.getTerrainTileProperty(POS_GHOST_2_PINK);
                case CYAN_GHOST_BASHFUL -> worldMap.getTerrainTileProperty(POS_GHOST_3_CYAN);
                case ORANGE_GHOST_POKEY -> worldMap.getTerrainTileProperty(POS_GHOST_4_ORANGE);
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
    public Vector2i worldSizePx() {
        return new Vector2i(worldMap.numCols() * TS, worldMap.numRows() * TS);
    }

    public Stream<Vector2i> tiles() {
        return worldMap.tiles();
    }

    public Stream<Vector2i> neighborTilesOutsideWorld(Vector2i tile) {
        requireNonNull(tile);
        return Stream.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT)
            .map(dir -> tile.plus(dir.vector()))
            .filter(worldMap::outOfWorld);
    }

    public Stream<Vector2i> neighborTilesInsideWorld(Vector2i tile) {
        requireNonNull(tile);
        return Stream.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT)
            .map(dir -> tile.plus(dir.vector()))
            .filter(not(worldMap::outOfWorld));
    }

    public List<Portal> portals() { return Arrays.asList(portals); }

    public boolean isTileInPortalSpace(Vector2i tile) {
        requireNonNull(tile);
        return portals().stream().anyMatch(portal -> portal.contains(tile));
    }

    public boolean isTileBlocked(Vector2i tile) {
        return !worldMap.outOfWorld(tile) && isBlocked(worldMap.content(LayerID.TERRAIN, tile));
    }

    public boolean isTunnel(Vector2i tile) {
        return !worldMap.outOfWorld(tile) && worldMap.content(LayerID.TERRAIN, tile) == TUNNEL.$;
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

    public Set<Vector2i> energizerPositions() { return Collections.unmodifiableSet(energizerTiles); }

    public boolean isEnergizerPosition(Vector2i tile) {
        return energizerTiles.contains(tile);
    }

    // Actor positions

    public Vector2f pacStartPosition() {
        return pacStartPosition;
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
}