package de.amr.pacmanfx.lib.worldmap;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.DefaultWorldMapPropertyName;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.Portal;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.worldmap.TerrainTile.TUNNEL;
import static de.amr.pacmanfx.lib.worldmap.TerrainTile.isBlocked;
import static de.amr.pacmanfx.model.DefaultWorldMapPropertyName.POS_PAC;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class TerrainLayer extends WorldMapLayer {

    private static Vector2f halfTileRightOf(Vector2i tile) { return Vector2f.of(tile.x() * TS + HTS, tile.y() * TS); }

    private Set<Obstacle> obstacles; // uninitialized!

    private Vector2f pacStartPosition;
    private Portal[] portals;
    private House house;

    public TerrainLayer(int numRows, int numCols) {
        super(numRows, numCols);
    }

    public TerrainLayer(WorldMapLayer layer) {
        super(layer);
        portals = findPortals();
        Vector2i pacTile = getTileProperty(POS_PAC);
        if (pacTile == null) {
            throw new IllegalArgumentException("No Pac position stored in map");
        }
        pacStartPosition = halfTileRightOf(pacTile);
    }

    public Vector2f pacStartPosition() {
        return pacStartPosition;
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

    public List<Portal> portals() { return Arrays.asList(portals); }

    private Portal[] findPortals() {
        var portals = new ArrayList<Portal>();
        int firstColumn = 0, lastColumn = numCols() - 1;
        for (int row = 0; row < numRows(); ++row) {
            Vector2i leftBorderTile = Vector2i.of(firstColumn, row);
            Vector2i rightBorderTile = Vector2i.of(lastColumn, row);
            if (get(row, firstColumn) == TUNNEL.$ && get(row, lastColumn) == TUNNEL.$) {
                portals.add(new Portal(leftBorderTile, rightBorderTile, 2));
            }
        }
        return portals.toArray(new Portal[0]);
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
        return portals().stream().anyMatch(portal -> portal.contains(tile));
    }

    public boolean isTileBlocked(Vector2i tile) {
        return !outOfBounds(tile) && isBlocked(get(tile));
    }

    public boolean isTunnel(Vector2i tile) {
        return !outOfBounds(tile) && get(tile) == TUNNEL.$;
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