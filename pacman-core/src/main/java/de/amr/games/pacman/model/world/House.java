/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * @author Armin Reichert
 */
public class House {

    private Vector2i minTile;
    private Vector2i size;
    private Door door;
    private Map<String, Vector2f> seatMap = new HashMap<>();

    public void setMinTile(Vector2i minTile) {
        checkTileNotNull(minTile);
        this.minTile = minTile;
    }

    public void setSize(Vector2i size) {
        checkNotNull(size);
        if (size.x() < 1 || size.y() < 1) {
            throw new IllegalArgumentException("House size must be larger than one square tile but is: " + size);
        }
        this.size = size;
    }

    public void setDoor(Door door) {
        checkNotNull(door);
        this.door = door;
    }

    public Vector2i topLeftTile() {
        return minTile;
    }

    public Vector2i size() {
        return size;
    }

    public Door door() {
        return door;
    }

    public void addSeat(String id, Vector2f position) {
        seatMap.put(id, position);
    }

    public Vector2f seat(String id) {
        if (seatMap.containsKey(id)) {
            return seatMap.get(id);
        }
        throw new IllegalArgumentException("Illegal seat ID: " + id);
    }

    public Vector2f center() {
        return minTile.toFloatVec().scaled(TS).plus(size.toFloatVec().scaled(HTS));
    }

    /**
     * @param tile some tile
     * @return tells if the given tile is part of this house
     */
    public boolean contains(Vector2i tile) {
        Vector2i max = minTile.plus(size().minus(1, 1));
        return tile.x() >= minTile.x() && tile.x() <= max.x() //
            && tile.y() >= minTile.y() && tile.y() <= max.y();
    }
}