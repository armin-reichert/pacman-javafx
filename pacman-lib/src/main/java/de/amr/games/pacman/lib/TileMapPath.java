package de.amr.games.pacman.lib;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

public class TileMapPath {

    public final Vector2i startTile;
    public final List<Direction> directions = new ArrayList<>();

    public TileMapPath(Vector2i startTile) {
        this.startTile = checkNotNull(startTile);
    }

    public void add(Direction dir) {
        directions.add(checkNotNull(dir));
    }

    public static TileMapPath build(TileMap map, BitSet explored, Vector2i startTile, Direction startDir) {
        checkNotNull(map);
        checkNotNull(explored);
        checkNotNull(startTile);
        checkNotNull(startDir);
        if (map.outOfBounds(startTile)) {
            throw new IllegalArgumentException("Start tile must be inside map");
        }

        var path = new TileMapPath(startTile);
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
                path.add(dir);
                break;
            }
            path.add(dir);
            explored.set(map.index(tile));
        }
        return path;
    }

    public List<Vector2i> toTileList() {
        List<Vector2i> tileList = new ArrayList<>();
        tileList.add(startTile);
        Vector2i tile = startTile;
        for (var dir : directions) {
            tile = tile.plus(dir.vector());
            tileList.add(tile);
        }
        return tileList;
    }

    static Direction newMoveDir(Direction moveDir, byte tileValue) {
        return switch (tileValue) {
            case Tiles.CORNER_NW, Tiles.DCORNER_NW -> moveDir == LEFT  ? DOWN  : RIGHT;
            case Tiles.CORNER_NE, Tiles.DCORNER_NE -> moveDir == RIGHT ? DOWN  : LEFT;
            case Tiles.CORNER_SE, Tiles.DCORNER_SE -> moveDir == DOWN  ? LEFT  : UP;
            case Tiles.CORNER_SW, Tiles.DCORNER_SW -> moveDir == DOWN  ? RIGHT : UP;
            default -> moveDir;
        };
    }
}
