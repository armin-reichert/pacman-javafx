package de.amr.pacmanfx.lib.worldmap;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.Portal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.lib.worldmap.TerrainTile.TUNNEL;
import static de.amr.pacmanfx.lib.worldmap.TerrainTile.isBlocked;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class TerrainLayer extends WorldMapLayer {

    private final Portal[] portals;
    private House house;

    public TerrainLayer(int numRows, int numCols) {
        super(numRows, numCols);
        portals = findPortals();
    }

    public TerrainLayer(WorldMapLayer layer) {
        super(layer);
        portals = findPortals();
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
}