/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.worldmap;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.DefaultWorldMapPropertyName;
import de.amr.pacmanfx.model.HPortal;
import de.amr.pacmanfx.model.House;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.lib.worldmap.TerrainTile.TUNNEL;
import static de.amr.pacmanfx.lib.worldmap.TerrainTile.isBlocked;
import static de.amr.pacmanfx.model.DefaultWorldMapPropertyName.*;
import static de.amr.pacmanfx.model.GameLevel.EMPTY_ROWS_BELOW_MAZE;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class TerrainLayer extends WorldMapLayer {

    public static boolean isValidTerrainCode(byte code) {
        return Stream.of(TerrainTile.values()).anyMatch(tile -> tile.$ == code);
    }

    private static Vector2f halfTileRightOf(Vector2i tile) { return Vector2f.of(tile.x() * TS + HTS, tile.y() * TS); }

    private Set<Obstacle> obstacles; // uninitialized!

    private Vector2f pacStartPosition;
    private HPortal[] hPortals;
    private House house;
    private final Vector2i[] ghostScatterTiles = new Vector2i[4];

    public TerrainLayer(int numRows, int numCols) {
        super(numRows, numCols);
    }

    public TerrainLayer(WorldMapLayer layer) {
        super(layer);
        hPortals = findHorizontalPortals();
        Vector2i pacTile = getTileProperty(POS_PAC);
        if (pacTile == null) {
            Logger.error("No Pac position stored in map");
        } else {
            pacStartPosition = halfTileRightOf(pacTile);
        }
        ghostScatterTiles[RED_GHOST_SHADOW] = getTileProperty(POS_SCATTER_RED_GHOST,
                Vector2i.of(0, numCols() - 3));

        ghostScatterTiles[PINK_GHOST_SPEEDY] = getTileProperty(POS_SCATTER_PINK_GHOST,
                Vector2i.of(0, 3));

        ghostScatterTiles[CYAN_GHOST_BASHFUL] = getTileProperty(POS_SCATTER_CYAN_GHOST,
                Vector2i.of(numRows() - EMPTY_ROWS_BELOW_MAZE, numCols() - 1));

        ghostScatterTiles[ORANGE_GHOST_POKEY] = getTileProperty(POS_SCATTER_ORANGE_GHOST,
                Vector2i.of(numRows() - EMPTY_ROWS_BELOW_MAZE, 0));
    }

    public Vector2f pacStartPosition() {
        return pacStartPosition;
    }

    public Vector2i ghostScatterTile(byte personality) {
        return ghostScatterTiles[requireValidGhostPersonality(personality)];
    }

    public void setHouse(House house) {
        this.house = house;
    }

    public House house() {
        return house;
    }

    public Optional<House> optHouse() {
        return Optional.ofNullable(house);
    }

    /**
     * @return position where level messages ("READY!", "GAME OVER") are displayed.
     */
    public Vector2f messageCenterPosition() {
        if (house != null) {
            Vector2i houseSize = house.sizeInTiles();
            float cx = TS(house.minTile().x() + houseSize.x() * 0.5f);
            float cy = TS(house.minTile().y() + houseSize.y() + 1);
            return Vector2f.of(cx, cy);
        }
        else {
            Vector2i worldSize = sizeInPixel();
            return Vector2f.of(worldSize.x() * 0.5f, worldSize.y() * 0.5f); // should not happen
        }
    }

    public List<HPortal> horizontalPortals() { return Arrays.asList(hPortals); }

    private HPortal[] findHorizontalPortals() {
        var portals = new ArrayList<HPortal>();
        int firstColumn = 0, lastColumn = numCols() - 1;
        for (int row = 0; row < numRows(); ++row) {
            Vector2i leftBorderTile = Vector2i.of(firstColumn, row);
            Vector2i rightBorderTile = Vector2i.of(lastColumn, row);
            if (content(row, firstColumn) == TUNNEL.$ && content(row, lastColumn) == TUNNEL.$) {
                portals.add(new HPortal(leftBorderTile, rightBorderTile, 2));
            }
        }
        return portals.toArray(new HPortal[0]);
    }

    public List<Vector2i> buildObstacleList() {
        List<Vector2i> tilesWithErrors = new ArrayList<>();
        obstacles = ObstacleBuilder.buildObstacles(this, tilesWithErrors);

        Vector2i houseMinTile = getTileProperty(DefaultWorldMapPropertyName.POS_HOUSE_MIN_TILE);
        if (houseMinTile == null) {
            Logger.info("Could not remove house placeholder from obstacle list, house min tile not set");
        } else {
            Vector2i houseStartPoint = houseMinTile.scaled(TS).plus(TS, HTS);
            obstacles.stream()
                    .filter(obstacle -> obstacle.startPoint().equals(houseStartPoint))
                    .findFirst().ifPresent(houseObstacle -> {
                        Logger.debug("Removing house placeholder-obstacle starting at tile {}, point {}", houseMinTile, houseStartPoint);
                        obstacles.remove(houseObstacle);
                    });
        }
        Logger.info("{} obstacles found in map ", obstacles.size(), this);
        return tilesWithErrors;
    }

    public Set<Obstacle> obstacles() {
        if (obstacles == null) { // first access
            buildObstacleList();
        }
        return Collections.unmodifiableSet(obstacles);
    }

    public Stream<Vector2i> neighborTilesOutsideWorld(Vector2i tile) {
        requireNonNull(tile);
        return Stream.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT)
            .map(dir -> tile.plus(dir.vector()))
            .filter(this::outOfBounds);
    }

    public Stream<Vector2i> neighborTilesInsideWorld(Vector2i tile) {
        requireNonNull(tile);
        return Stream.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT)
            .map(dir -> tile.plus(dir.vector()))
            .filter(not(this::outOfBounds));
    }

    public boolean isTileInPortalSpace(Vector2i tile) {
        requireNonNull(tile);
        return horizontalPortals().stream().anyMatch(portal -> portal.contains(tile));
    }

    public boolean isTileBlocked(Vector2i tile) {
        return !outOfBounds(tile) && isBlocked(content(tile));
    }

    public boolean isTunnel(Vector2i tile) {
        return !outOfBounds(tile) && content(tile) == TUNNEL.$;
    }

    public boolean isIntersection(Vector2i tile) {
        if (outOfBounds(tile) || isTileBlocked(tile)) {
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

    /**
     * @return world size in pixels as (size-x, size-y)
     */
    public Vector2i sizeInPixel() {
        return new Vector2i(numCols() * TS, numRows() * TS);
    }

    /**
     * @param propertyName property name
     * @param defaultTile tile returned if property map does not contain property name (can be null)
     * @return tile value of property in terrain layer or default value
     */
    public Vector2i getTileProperty(String propertyName, Vector2i defaultTile) {
        requireNonNull(propertyName);
        String value = propertyMap().get(propertyName);
        return value != null
            ? WorldMapParser.parseTile(value).orElse(defaultTile)
            : defaultTile;
    }

    /**
     * @param propertyName property name
     * @return tile value of property in terrain layer or <code>null</code>
     */
    public Vector2i getTileProperty(String propertyName) {
        return getTileProperty(propertyName, null);
    }
}