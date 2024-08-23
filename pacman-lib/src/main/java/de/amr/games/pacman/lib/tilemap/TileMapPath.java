/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class TileMapPath implements Iterable<Direction> {

    private static Direction newMoveDir(Direction moveDir, byte tileValue) {
        return switch (tileValue) {
            case Tiles.CORNER_NW, Tiles.DCORNER_NW -> moveDir == LEFT  ? DOWN  : RIGHT;
            case Tiles.CORNER_NE, Tiles.DCORNER_NE -> moveDir == RIGHT ? DOWN  : LEFT;
            case Tiles.CORNER_SE, Tiles.DCORNER_SE -> moveDir == DOWN  ? LEFT  : UP;
            case Tiles.CORNER_SW, Tiles.DCORNER_SW -> moveDir == DOWN  ? RIGHT : UP;
            default -> moveDir;
        };
    }

    private final TileMap map;
    private final Vector2i startTile;
    private final List<Direction> directions = new ArrayList<>();

    public TileMapPath(TileMap map, BitSet explored, Vector2i startTile, Direction startDir) {
        this.map = checkNotNull(map);
        checkNotNull(explored);
        checkNotNull(startTile);
        checkNotNull(startDir);
        if (map.outOfBounds(startTile)) {
            throw new IllegalArgumentException("Start tile must be inside map");
        }
        this.startTile = startTile;
        explored.set(map.index(startTile));
        var tile = startTile;
        var dir = startDir;
        while (true) {
            dir = newMoveDir(dir, map.get(tile));
            tile = tile.plus(dir.vector());
            if (map.outOfBounds(tile)) {
                break;
            }
            if (explored.get(map.index(tile))) {
                directions.add(dir);
                break;
            }
            directions.add(dir);
            explored.set(map.index(tile));
        }
    }

    public TileMap map() {
        return map;
    }

    public Vector2i startTile() {
        return startTile;
    }

    public int size() {
        return directions.size();
    }

    public Direction dir(int i) {
        return directions.get(i);
    }

    @Override
    public Iterator<Direction> iterator() {
        return directions.iterator();
    }
}