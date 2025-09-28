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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.lib.worldmap.TerrainTile.TUNNEL;
import static de.amr.pacmanfx.lib.worldmap.TerrainTile.isBlocked;
import static de.amr.pacmanfx.model.DefaultWorldMapPropertyName.*;
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

    private final Game game;
    private final int number; // 1=first level

    private final WorldMap worldMap;
    private final House house;
    private final Vector2f pacStartPosition;
    private final Vector2i[] ghostScatterTiles = new Vector2i[4];
    private final Portal[] portals;

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

    public GameLevel(Game game, int number, WorldMap worldMap, House house) {
        this.game = requireNonNull(game);
        this.number = requireValidLevelNumber(number);
        this.worldMap = requireNonNull(worldMap);
        this.house = requireNonNull(house);

        blinking = new Pulse(10, Pulse.OFF);
        portals = findPortals();

        //TODO check if this is still needed:
        worldMap.setContent(LayerID.TERRAIN, house.minTile(), house.content());

        currentBonusIndex = -1;

        Vector2i pacTile = worldMap.getTerrainTileProperty(POS_PAC);
        if (pacTile == null) {
            throw new IllegalArgumentException("No Pac position stored in map");
        }
        pacStartPosition = halfTileRightOf(pacTile);

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

    private Portal[] findPortals() {
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

    public void getReadyToPlay() {
        pac.reset(); // initially invisible!
        pac.setPosition(pacStartPosition());
        pac.setMoveDir(Direction.LEFT);
        pac.setWishDir(Direction.LEFT);
        pac.powerTimer().resetIndefiniteTime();
        ghosts().forEach(ghost -> {
            ghost.reset(); // initially invisible!
            ghost.setPosition(ghost.startPosition());
            ghost.setMoveDir(house.ghostStartDirection(ghost.personality()));
            ghost.setWishDir(house.ghostStartDirection(ghost.personality()));
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

    public Game game() {
        return game;
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

    public void setGhosts(Ghost redGhost, Ghost pinkGhost, Ghost cyanGhost, Ghost orangeGhost) {
        ghosts = new Ghost[] {
            requireNonNull(redGhost), requireNonNull(pinkGhost), requireNonNull(cyanGhost), requireNonNull(orangeGhost)
        };
    }

    public Ghost ghost(byte id) {
        return ghosts[requireValidGhostPersonality(id)];
    }

    public Stream<Ghost> ghosts(GhostState... states) {
        requireNonNull(states);
        requireNonNull(ghosts);
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
            .filter(worldMap::outOfBounds);
    }

    public Stream<Vector2i> neighborTilesInsideWorld(Vector2i tile) {
        requireNonNull(tile);
        return Stream.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT)
            .map(dir -> tile.plus(dir.vector()))
            .filter(not(worldMap::outOfBounds));
    }

    public List<Portal> portals() { return Arrays.asList(portals); }

    public boolean isTileInPortalSpace(Vector2i tile) {
        requireNonNull(tile);
        return portals().stream().anyMatch(portal -> portal.contains(tile));
    }

    public boolean isTileBlocked(Vector2i tile) {
        return !worldMap.outOfBounds(tile) && isBlocked(worldMap.content(LayerID.TERRAIN, tile));
    }

    public boolean isTunnel(Vector2i tile) {
        return !worldMap.outOfBounds(tile) && worldMap.content(LayerID.TERRAIN, tile) == TUNNEL.$;
    }

    public boolean isIntersection(Vector2i tile) {
        if (worldMap.outOfBounds(tile) || isTileBlocked(tile)) {
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

    public Optional<House> optHouse() {
        return Optional.ofNullable(house);
    }

    // Actor positions

    public Vector2f pacStartPosition() {
        return pacStartPosition;
    }
}