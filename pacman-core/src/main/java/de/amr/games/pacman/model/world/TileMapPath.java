package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

public class TileMapPath {

    public final Vector2i startTile;
    public final List<Direction> directions = new ArrayList<>();
    public boolean closed = false;

    public TileMapPath(Vector2i startTile) {
        this.startTile = checkNotNull(startTile);
    }

    public void add(Direction dir) {
        directions.add(checkNotNull(dir));
    }

    public static List<Vector2i> buildPath(TileMap map, Set<Vector2i> explored, Vector2i startTile, Direction startDir) {
        var path = new ArrayList<Vector2i>();
        var tile = startTile;
        var dir = newMoveDir(startDir, map.get(startTile));
        while (true) {
            path.add(tile);
            explored.add(tile);
            tile = tile.plus(dir.vector());
            if (map.outOfBounds(tile)) {
                break;
            }
            if (explored.contains(tile)) {
                path.add(tile); // close path
                break;
            }
            dir = newMoveDir(dir, map.get(tile));
        }
        return path;
    }

    public static Direction newMoveDir(Direction moveDir, byte tileValue) {
        return switch (tileValue) {
            case Tiles.CORNER_NW, Tiles.DCORNER_NW -> moveDir == LEFT  ? DOWN  : RIGHT;
            case Tiles.CORNER_NE, Tiles.DCORNER_NE -> moveDir == RIGHT ? DOWN  : LEFT;
            case Tiles.CORNER_SE, Tiles.DCORNER_SE -> moveDir == DOWN  ? LEFT  : UP;
            case Tiles.CORNER_SW, Tiles.DCORNER_SW -> moveDir == DOWN  ? RIGHT : UP;
            default -> moveDir;
        };
    }
}
