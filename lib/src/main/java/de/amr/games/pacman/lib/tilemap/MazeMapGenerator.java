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
import java.util.stream.IntStream;

public class MazeMapGenerator {

    public static void createMazeByWilsonAlgorithm(GridGraph grid) {
        List<Integer> vertices = new ArrayList<>(IntStream.range(0, grid.numVertices()).boxed().toList());
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

    // ---

    public static void createMazeByRecursiveDivision(GridGraph grid) {
        for (int row = 0; row < grid.numRows(); ++row) {
            for (int col = 0; col < grid.numCols(); ++col) {
                int vertex = grid.vertex(row, col);
                if (row > 0) {
                    grid.connect(vertex, Dir.N);
                }
                if (col > 0) {
                    grid.connect(vertex, Dir.W);
                }
            }
        }
        divide(grid, new Random(), 0, 0, grid.numCols(), grid.numRows());
    }

    private static void divide(GridGraph grid, Random rnd, int x0, int y0, int w, int h) {
        if (w <= 1 && h <= 1) {
            return;
        }
        if (w < h || (w == h && rnd.nextBoolean())) {
            // Build "horizontal wall" at random y from [y0 + 1, y0 + h - 1], keep random door
            int y = y0 + 1 + rnd.nextInt(h - 1);
            int door = x0 + rnd.nextInt(w);
            for (int x = x0; x < x0 + w; ++x) {
                if (x != door) {
                    grid.disconnect(grid.vertex(y - 1, x), Dir.S);
                }
            }
            divide(grid, rnd, x0, y0, w, y - y0);
            divide(grid, rnd, x0, y, w, h - (y - y0));
        } else {
            // Build "vertical wall" at random x from [x0 + 1, x0 + w - 1], keep random door
            int x = x0 + 1 + rnd.nextInt(w - 1);
            int door = y0 + rnd.nextInt(h);
            for (int y = y0; y < y0 + h; ++y) {
                if (y != door) {
                    grid.disconnect(grid.vertex(y, x - 1), Dir.E);
                }
            }
            divide(grid, rnd, x0, y0, x - x0, h);
            divide(grid, rnd, x, y0, w - (x - x0), h);
        }
    }


    private static final byte FREE = Tiles.EMPTY;
    private static final byte BLOCKED = Tiles.TUNNEL;
    private static final int EMPTY_ROWS_ABOVE = 3, EMPTY_ROWS_BELOW = 2;

    public WorldMap createMazeMap(int numRows, int numCols) {

        // create maze graph
        GridGraphImpl grid = new GridGraphImpl(numRows, numCols);
        createMazeByRecursiveDivision(grid);

        // create map from maze (3 times larger!)
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
        addFood(worldMap);
        return worldMap;
    }

    private Vector2i current;
    private Vector2i predecessor;
    private Direction moveDir;

    private boolean inRange(Vector2i tile, TileMap map) {
        return 0 <= tile.x() && tile.x() < map.numCols() && 0 <= tile.y() && tile.y() <= map.numRows();
    }

    private void computeWallContour(WorldMap map, Vector2i startTile) {
        TileMap terrain = map.terrain();
        predecessor = null;
        current = startTile;
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

    private void addFood(WorldMap map) {
        TileMap terrainMap = map.terrain();
        TileMap foodMap = map.food();
        for (int row = EMPTY_ROWS_ABOVE; row < foodMap.numRows() - EMPTY_ROWS_BELOW; ++row) {
            for (int col = 0; col < foodMap.numCols(); ++col) {
                if (terrainMap.get(row, col) == Tiles.EMPTY && new Random().nextInt(100) < 40) {
                    foodMap.set(row, col, Tiles.PELLET);
                }
            }
        }
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
        Logger.debug("x={} y={}: {} move {}", col, row, value, moveDir);
    }

    public static void main(String[] args)  {
        MazeMapGenerator gen = new MazeMapGenerator();
        //WorldMap mazeMap = new WorldMap(new File("maze.world"));
        for (int i = 0; i < 20; ++i) {
            try {
                WorldMap mazeMap = gen.createMazeMap(40, 20);
                gen.computeWallContour(mazeMap, new Vector2i(0, EMPTY_ROWS_ABOVE));
                File file = new File("maze_%d.world".formatted(i));
                mazeMap.save(file);
                Logger.info("Maze map {} saved", file);
            } catch (Exception x) {
                Logger.error(x);
            }
        }
    }
}