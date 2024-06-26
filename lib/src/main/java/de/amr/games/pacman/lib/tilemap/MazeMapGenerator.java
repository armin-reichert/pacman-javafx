package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.graph.Dir;
import de.amr.games.pacman.lib.graph.DirMap;
import de.amr.games.pacman.lib.graph.GridGraph;
import de.amr.games.pacman.lib.graph.GridGraphImpl;
import org.tinylog.Logger;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MazeMapGenerator {

    public static void createMazeByWilson(GridGraph grid) {
        List<Integer> vertices = IntStream.range(0, grid.numVertices()).boxed()
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(vertices);
        BitSet inTree = new BitSet();
        inTree.set(vertices.get(0));
        DirMap lastWalkDir = new DirMap();
        for (int vertex : vertices) {
            loopErasedRandomWalk(grid, vertex, lastWalkDir, inTree);
        }
    }

    private static void loopErasedRandomWalk(GridGraph grid, int start, DirMap lastWalkDir, BitSet inTree) {
        // random walk until a tree vertex is touched
        int vertex = start;
        while (!inTree.get(vertex)) {
            Dir walkDir = Dir.random();
            int neighbor = grid.neighbor(vertex, walkDir);
            if (neighbor != -1) {
                lastWalkDir.set(vertex, walkDir);
                vertex = neighbor;
            }
        }
        // add the (loop-erased) random walk to the tree
        vertex = start;
        while (!inTree.get(vertex)) {
            Dir walkDir = lastWalkDir.get(vertex);
            int neighbor = grid.neighbor(vertex, walkDir);
            if (neighbor != -1) {
                inTree.set(vertex);
                grid.connect(vertex, walkDir);
                vertex = neighbor;
            }
        }
    }

    private static final byte FREE = Tiles.EMPTY;
    private static final byte BLOCKED = Tiles.TUNNEL;
    private static final int EMPTY_ROWS_ABOVE = 3, EMPTY_ROWS_BELOW = 2;

    public WorldMap createMazeMap(int numRows, int numCols) {
        GridGraphImpl grid = new GridGraphImpl(numRows, numCols);
        createMazeByWilson(grid);
        var worldMap = new WorldMap(3 * numRows + EMPTY_ROWS_ABOVE + EMPTY_ROWS_BELOW, 3 * numCols);
        var terrain = worldMap.terrain();
        for (int v = 0; v < grid.numVertices(); ++v) {
            // center of 3x3 "macro" cell
            int row = 3 * grid.row(v) + 1 + EMPTY_ROWS_ABOVE, col = 3 * grid.col(v) + 1;
            if (row < 3 || row > 3 * numRows + EMPTY_ROWS_BELOW) {
                continue;
            }
            set(terrain, row - 1, col - 1, BLOCKED);
            set(terrain, row - 1, col, grid.connected(v, Dir.N) ? FREE : BLOCKED);
            set(terrain, row - 1, col + 1, BLOCKED);
            set(terrain, row, col - 1, grid.connected(v, Dir.W) ? FREE : BLOCKED);
            set(terrain, row, col, FREE);
            set(terrain, row, col + 1, grid.connected(v, Dir.E) ? FREE : BLOCKED);
            set(terrain, row + 1, col - 1, BLOCKED);
            set(terrain, row + 1, col, grid.connected(v, Dir.S) ? FREE : BLOCKED);
            set(terrain, row + 1, col + 1, BLOCKED);
        }
        return worldMap;
    }

    private Vector2i current;
    private Direction moveDir;

    public void refine(WorldMap map) {
        TileMap terrain = map.terrain();
        current = new Vector2i(0, EMPTY_ROWS_ABOVE); //x=col, y=row
        set(terrain, current, Tiles.CORNER_NW);
        moveDir = Direction.RIGHT;
        move();
        int i = 0;
        while (i < 1000) {
            ++i;
            Vector2i rightHandedTile = current.plus(moveDir.nextClockwise().vector());
            if (terrain.get(rightHandedTile) == FREE) {
                if (terrain.get(current.plus(moveDir.vector())) == BLOCKED) {
                    set(terrain, current, moveDir.isHorizontal() ? Tiles.WALL_H : Tiles.WALL_V);
                    move();
                } else {
                    // turn counter-clockwise
                    switch (moveDir) {
                        case LEFT -> set(terrain, current, Tiles.CORNER_NW);
                        case RIGHT -> set(terrain, current, Tiles.CORNER_SE);
                        case UP -> set(terrain, current, Tiles.CORNER_NE);
                        case DOWN -> set(terrain, current, Tiles.CORNER_SW);
                    }
                    moveDir = moveDir.nextCounterClockwise();
                    move();
                }
            } else {
                byte corner = Tiles.EMPTY;
                switch (moveDir) {
                    case RIGHT -> {
                        corner = Tiles.CORNER_NE;
                        moveDir = Direction.DOWN;
                    }
                    case DOWN -> {
                        corner = Tiles.CORNER_SE;
                        moveDir = Direction.LEFT;
                    }
                    case LEFT -> {
                        corner = Tiles.CORNER_SW;
                        moveDir = Direction.UP;
                    }
                    case UP -> {
                        corner = Tiles.CORNER_NW;
                        moveDir = Direction.RIGHT;
                    }
                };
                set(terrain, current, corner);
                move();
            }
        }
    }

    private void move() {
        current = current.plus(moveDir.vector());
    }

    private void set(TileMap map, Vector2i tile, byte value) {
        set(map, tile.y(), tile.x(), value);
    }

    private void set(TileMap map, int row, int col, byte value) {
        if (row < 0 || row >= map.numRows()) {
            return;
        }
        if (col < 0 || col >= map.numCols()) {
            return;
        }
        map.set(row, col, value);
        Logger.info("x={} y={}: {} move {}", col, row, value, moveDir);
    }

    public static void main(String[] args)  {
        MazeMapGenerator gen = new MazeMapGenerator();
        //WorldMap mazeMap = createMazeMap(10, 10);
        WorldMap mazeMap = new WorldMap(new File("maze.world"));
        try {
            gen.refine(mazeMap);
        } catch (Exception x) {
            Logger.error(x);
        }
        mazeMap.save(new File("maze_refined.world"));
    }
}
