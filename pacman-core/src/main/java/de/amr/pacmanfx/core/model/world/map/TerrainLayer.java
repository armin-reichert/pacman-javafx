/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.world.map;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.entities.HPortal;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.obstacle.Obstacle;
import de.amr.pacmanfx.core.model.world.obstacle.ObstacleBuilder;
import org.tinylog.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.amr.basics.math.Vector2f.vec2_float;
import static de.amr.pacmanfx.core.model.world.map.TerrainTile.TUNNEL;
import static de.amr.pacmanfx.core.model.world.map.TerrainTile.isBlocked;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tile;
import static de.amr.pacmanfx.core.model.world.map.WorldMapPropertyName.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public final class TerrainLayer extends WorldMapLayer {

    private static Vector2f halfTileRightOf(Vector2i tile) {
        return vec2_float(tile.x() * WorldMap.TS + WorldMap.HTS, tile.y() * WorldMap.TS);
    }

    private final Vector2i[] scatterTiles = new Vector2i[4];
    private Vector2f pacStartPosition;
    private HPortal[] hPortals;
    private Set<Obstacle> obstacleSet; // uninitialized!

    public TerrainLayer(int numRows, int numCols) {
        super(numRows, numCols);
    }

    public TerrainLayer(TerrainLayer layer) {
        super(layer);
        hPortals = findHorizontalPortals();
        Vector2i pacTile = getTileProperty(POS_PAC);
        if (pacTile == null) {
            //TODO use default position but where?
            Logger.error("No Pac position stored in map");
        } else {
            pacStartPosition = halfTileRightOf(pacTile);
        }
        scatterTiles[0] = getTilePropertyOrDefault(POS_SCATTER_RED_GHOST,    tile(0, numCols() - 3));
        scatterTiles[1] = getTilePropertyOrDefault(POS_SCATTER_PINK_GHOST,   tile(0, 3));
        scatterTiles[2] = getTilePropertyOrDefault(POS_SCATTER_CYAN_GHOST,   tile(numRows() - emptyRowsBelowMaze(), numCols() - 1));
        scatterTiles[3] = getTilePropertyOrDefault(POS_SCATTER_ORANGE_GHOST, tile(numRows() - emptyRowsBelowMaze(), 0));

        if (layer.obstacleSet != null) {
            this.obstacleSet = Set.copyOf(layer.obstacleSet);
        }
    }

    public Vector2f pacStartPosition() {
        return pacStartPosition;
    }

    public Vector2i ghostScatterTile(GhostPersonality personality) {
        return scatterTiles[requireNonNull(personality).ordinal()];
    }

    /**
     * Computes if the given tile is a "real" intersection in the terrain. The predicate for example could define
     * tiles covered by the ghost house as inaccessible tiles.
     *
     * @param tile a tile
     * @param inaccessibleCondition an additional condition marking tiles as inaccessible
     * @return if the tile is a real intersection i.e. an accessible tile with 3 or 4 accessible neighbor tiles
     */
    public boolean isRealIntersectionTile(Vector2i tile, Predicate<Vector2i> inaccessibleCondition) {
        requireNonNull(tile);

        if (outOfBounds(tile) || isInaccessibleTile(tile) || inaccessibleCondition.test(tile)) {
            return false;
        }

        long inaccessibleNeighbors = 0;
        inaccessibleNeighbors += neighborTilesOutsideWorld(tile).count();
        inaccessibleNeighbors += neighborTilesInsideWorld(tile).filter(this::isInaccessibleTile).count();
        inaccessibleNeighbors += neighborTilesInsideWorld(tile).filter(inaccessibleCondition).count();
        return inaccessibleNeighbors <= 1; // 3 or 4 accessible neighbors
    }


    public List<HPortal> horizontalPortals() { return Arrays.asList(hPortals); }

    private HPortal[] findHorizontalPortals() {
        var portals = new ArrayList<HPortal>();
        int firstColumn = 0, lastColumn = numCols() - 1;
        for (int row = 0; row < numRows(); ++row) {
            Vector2i leftBorderTile = tile(firstColumn, row);
            Vector2i rightBorderTile = tile(lastColumn, row);
            if (content(row, firstColumn) == TUNNEL.$ && content(row, lastColumn) == TUNNEL.$) {
                portals.add(new HPortal(leftBorderTile, rightBorderTile, 2));
            }
        }
        return portals.toArray(new HPortal[0]);
    }

    public List<Vector2i> createObstacles() {
        List<Vector2i> tilesWithErrors = new ArrayList<>();
        obstacleSet = ObstacleBuilder.buildObstacleSet(this, tilesWithErrors);

        Vector2i houseMinTile = getTileProperty(WorldMapPropertyName.POS_HOUSE_MIN_TILE);
        if (houseMinTile == null) {
            Logger.info("Could not remove house placeholder from obstacle list, house min tile not set");
        } else {
            Vector2i houseStartPoint = houseMinTile.scaled(WorldMap.TS).plus(WorldMap.TS, WorldMap.HTS);
            obstacleSet.stream()
                .filter(obstacle -> obstacle.startPoint().equals(houseStartPoint))
                .findFirst().ifPresent(houseObstacle -> {
                    Logger.debug("Removing house placeholder-obstacle starting at tile {}, point {}", houseMinTile, houseStartPoint);
                    obstacleSet.remove(houseObstacle);
                });
        }
        return tilesWithErrors;
    }

    public Set<Obstacle> obstacles() {
        return obstacleSet == null ? Set.of() : Collections.unmodifiableSet(obstacleSet);
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

    public Optional<HPortal> hPortalContainingTile(Vector2i tile) {
        requireNonNull(tile);
        return horizontalPortals().stream().filter(portal -> portal.contains(tile)).findFirst();
    }

    public boolean isTileInPortalSpace(Vector2i tile) {
        requireNonNull(tile);
        return horizontalPortals().stream().anyMatch(portal -> portal.contains(tile));
    }

    public boolean isInaccessibleTile(Vector2i tile) {
        return !outOfBounds(tile) && isBlocked(content(tile));
    }

    public boolean isTunnel(Vector2i tile) {
        return !outOfBounds(tile) && content(tile) == TUNNEL.$;
    }

    /**
     * @return world size in pixels as (width, height)
     */
    public Vector2i sizeInPixel() {
        return new Vector2i(numCols() * WorldMap.TS, numRows() * WorldMap.TS);
    }

    /**
     * @param propertyName property name
     * @param defaultTile tile returned if property map does not contain property name (can be null)
     * @return tile value of property in terrain layer or default value
     */
    public Vector2i getTilePropertyOrDefault(String propertyName, Vector2i defaultTile) {
        requireNonNull(propertyName);
        String value = propertyMap().get(propertyName);
        if (value == null) return defaultTile;
        try {
            return WorldMapParser.parseTile(value);
        } catch (IllegalArgumentException x) {
            return defaultTile;
        }
    }

    /**
     * @param propertyName property name
     * @return tile value of property in terrain layer or <code>null</code>
     */
    public Vector2i getTileProperty(String propertyName) {
        return getTilePropertyOrDefault(propertyName, null);
    }
}