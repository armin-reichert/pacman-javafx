/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import java.util.ArrayList;
import java.util.BitSet;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class TileMapPath {

    private static Direction newMoveDir(Direction moveDir, byte tileValue) {
        return switch (tileValue) {
            case Tiles.CORNER_NW, Tiles.DCORNER_NW -> moveDir == LEFT  ? DOWN  : RIGHT;
            case Tiles.CORNER_NE, Tiles.DCORNER_NE -> moveDir == RIGHT ? DOWN  : LEFT;
            case Tiles.CORNER_SE, Tiles.DCORNER_SE -> moveDir == DOWN  ? LEFT  : UP;
            case Tiles.CORNER_SW, Tiles.DCORNER_SW -> moveDir == DOWN  ? RIGHT : UP;
            default -> moveDir;
        };
    }

    private static final Direction[] DIRECTION_VALUES = Direction.values();

    private final Vector2i startTile;
    private final byte[] dirOrdinals;

    public TileMapPath(TileMap map, BitSet explored, Vector2i startTile, Direction startDir) {
        checkNotNull(map);
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
        var directions = new ArrayList<Direction>();
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
        dirOrdinals = new byte[directions.size()];
        for (int i = 0; i < directions.size(); ++i) {
            dirOrdinals[i] = (byte) directions.get(i).ordinal();
        }
    }

    public Vector2i startTile() {
        return startTile;
    }

    public int size() {
        return dirOrdinals.length;
    }

    public Direction dir(int i) {
        return DIRECTION_VALUES[dirOrdinals[i]];
    }
}
