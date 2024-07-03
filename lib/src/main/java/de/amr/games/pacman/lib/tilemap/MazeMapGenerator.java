package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.graph.Dir;
import de.amr.games.pacman.lib.graph.DirMap;
import de.amr.games.pacman.lib.graph.GridGraph;
import de.amr.games.pacman.lib.graph.GridGraphImpl;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class MazeMapGenerator {

    public static void createMazeByWilsonAlgorithm(GridGraph grid) {
        List<Integer> vertices = new ArrayList<>(IntStream.range(0, grid.numVertices()).boxed().toList());
        Logger.info(vertices);
        Collections.shuffle(vertices);
        Logger.info(vertices);
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
        createMazeByWilsonAlgorithm(grid);
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
    private Vector2i predecessor;
    private Direction moveDir;

    private boolean inRange(Vector2i tile, TileMap map) {
        return 0 <= tile.x() && tile.x() < map.numCols() && 0 <= tile.y() && tile.y() <= map.numRows();
    }

    public void setContour(WorldMap map) {
        TileMap terrain = map.terrain();
        predecessor = null;
        current = new Vector2i(0, EMPTY_ROWS_ABOVE); // top-left tile of terrain
        set(terrain, current, Tiles.CORNER_NW);
        moveDir = Direction.RIGHT;
        move();
        while (inRange(current, terrain)) {
            Vector2i tileAtRightHand = current.plus(moveDir.nextClockwise().vector());
            if (terrain.get(tileAtRightHand) == FREE) {
                Vector2i tileAhead = current.plus(moveDir.vector());
                if (terrain.get(tileAhead) == BLOCKED) {
                    set(terrain, current, moveDir.isHorizontal() ? Tiles.WALL_H : Tiles.WALL_V);
                } else {
                    byte corner = switch (moveDir) {
                        case LEFT  -> Tiles.CORNER_NW;
                        case RIGHT -> Tiles.CORNER_SE;
                        case UP    -> Tiles.CORNER_NE;
                        case DOWN  -> Tiles.CORNER_SW;
                    };
                    set(terrain, current, corner);
                    turnCounterclockwise();
                }
            } else {
                byte corner = switch (moveDir) {
                    case RIGHT -> Tiles.CORNER_NE;
                    case DOWN  -> Tiles.CORNER_SE;
                    case LEFT  -> Tiles.CORNER_SW;
                    case UP    -> Tiles.CORNER_NW;
                };
                set(terrain, current, corner);
                turnClockwise();
            }
            move();
        }
        set(terrain, predecessor, Tiles.WALL_V); //TODO correct?
    }

    private void turnClockwise() {
        moveDir = moveDir.nextClockwise();
    }

    private void turnCounterclockwise() {
        moveDir = moveDir.nextCounterClockwise();
    }

    private void move() {
        predecessor = current;
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
        WorldMap mazeMap = gen.createMazeMap(40, 20);
        //WorldMap mazeMap = new WorldMap(new File("maze.world"));
        try {
            gen.setContour(mazeMap);
        } catch (Exception x) {
            Logger.error(x);
        }
        mazeMap.save(new File("maze_refined.world"));
    }
}